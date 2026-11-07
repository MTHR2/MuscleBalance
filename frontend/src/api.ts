export type MuscleGroupCard = {
  id: number
  name: string
  imageUrl: string | null
}

export type LoadType = 'weighted' | 'bodyweight'
export type MuscleRole = 'primary' | 'secondary'

export type ExerciseListItem = {
  id: number
  name: string
  imageUrl: string | null
  loadType: LoadType
  favourite: boolean
  custom: boolean
  workingWeight?: number | null
  muscles?: ExerciseMuscle[]
}

export type Muscle = {
  id: number
  name: string
  regionKey: string
  imageUrl: string | null
  groupId: number
  groupName: string
}

export type ExerciseFilter = {
  q?: string
  muscleGroupId?: number | null
  favourite?: boolean
  custom?: boolean
}

export type CreateExerciseRequest = {
  name: string
  description: string | null
  loadType: LoadType
  muscles: { muscleId: number; role: MuscleRole }[]
}

export type ExerciseMuscle = {
  muscleId: number
  muscleName: string
  role: MuscleRole
}

export type ExerciseDetails = {
  id: number
  name: string
  description: string | null
  imageUrl: string | null
  loadType: 'weighted' | 'bodyweight'
  custom: boolean
  favourite: boolean
  workingWeight: number | null
  muscles: ExerciseMuscle[]
}

export type WorkoutTemplateListItem = {
  id: number
  name: string
  exerciseCount: number
}

export type WorkoutTemplateExercise = {
  exerciseId: number
  exerciseName: string
  imageUrl: string | null
  sortOrder: number
  targetSets: number
  repsMin: number | null
  repsMax: number | null
}

export type WorkoutTemplateDetails = {
  id: number
  name: string
  exercises: WorkoutTemplateExercise[]
}

// тренировка активного плана на сегодня; completedToday — по ней сегодня уже есть завершённая тренировка
export type TodayWorkout = WorkoutTemplateDetails & { completedToday: boolean }

export type CreateWorkoutTemplateRequest = {
  name: string
  exercises: { exerciseId: number; targetSets: number }[]
}

export type PlanListItem = {
  id: number
  name: string
  active: boolean
  workoutCount: number // число тренировочных дней
}

export type PlanDay = {
  dayOfWeek: number // 1 = Пн ... 7 = Вс
  workoutTemplateId: number
  workoutTemplateName: string
}

// цель на конкретную мышцу; перекрывает общую, 0 — мышцу в плане не качаем
export type MuscleTarget = {
  muscleId: number
  muscleName: string
  weeklySets: number
}

export type PlanDetails = {
  id: number
  name: string
  description: string | null
  active: boolean
  days: PlanDay[]
  defaultWeeklySets: number | null // общая цель подходов на мышцу в неделю
  muscleTargets: MuscleTarget[]
}

export type SavePlanRequest = {
  name: string
  description: string | null
  days: { workoutTemplateId: number; dayOfWeek: number }[]
  defaultWeeklySets: number | null
  muscleTargets: { muscleId: number; weeklySets: number }[]
}

export type WeekSummary = {
  workoutsDone: number
  workoutsPlanned: number | null // null — нет активного плана
  workingSets: number
  targetPercent: number | null // null — в плане нет целей
}

export type WorkoutSet = {
  id: number
  setNumber: number
  reps: number
  weight: number // кг; для bodyweight — доп. вес (0 = свой вес)
  warmup: boolean
}

export type WorkoutExercise = {
  id: number // workoutExerciseId — для адресов
  exerciseId: number // id упражнения из каталога
  exerciseName: string
  imageUrl: string | null
  loadType: 'weighted' | 'bodyweight'
  sortOrder: number
  plannedSets: number | null // null = добавлено по ходу тренировки
  targetSets: number
  repsMin: number | null
  repsMax: number | null
  workingWeight: number | null // подсказка веса для новых подходов
  sets: WorkoutSet[]
}

export type WorkoutDetails = {
  id: number
  name: string
  startedAt: string
  finishedAt: string | null
  exercises: WorkoutExercise[]
}

export type WorkoutSummary = {
  id: number
  name: string
  startedAt: string
  finishedAt: string
  durationSeconds: number
  completionPercent: number | null // null для свободной тренировки
  completedSets: number
  volume: number | null
}

export type StartWorkoutRequest = { workoutTemplateId?: number | null; name?: string }
export type SetValues = { reps: number; weight: number; warmup: boolean }

export type FieldViolation = { field: string; message: string }

export type ApiErrorBody = {
  status: number
  code: string
  message: string
  path?: string
  timestamp?: string
  fieldErrors?: FieldViolation[]
}

export class ApiRequestError extends Error {
  readonly status: number
  readonly code: string
  readonly fieldErrors: FieldViolation[]

  constructor(status: number, code: string, message: string, fieldErrors: FieldViolation[] = []) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
    this.code = code
    this.fieldErrors = fieldErrors
  }

  fieldMessage(field: string) {
    return this.fieldErrors.find((f) => f.field === field)?.message
  }
}

const isApiErrorBody = (b: unknown): b is ApiErrorBody =>
  typeof b === 'object' && b !== null && typeof (b as ApiErrorBody).code === 'string' &&
  typeof (b as ApiErrorBody).message === 'string'

async function toRequestError(res: Response): Promise<ApiRequestError> {
  const body: unknown = await res.json().catch(() => null)
  if (isApiErrorBody(body)) {
    return new ApiRequestError(res.status, body.code, body.message, body.fieldErrors ?? [])
  }
  if (res.status >= 502 && res.status <= 504) {
    return new ApiRequestError(res.status, 'SERVER_UNAVAILABLE', 'Сервер не отвечает')
  }
  return new ApiRequestError(res.status, 'UNKNOWN', `${res.status} ${res.statusText}`.trim())
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, init)
  if (!res.ok) throw await toRequestError(res)
  if (res.status === 204) return undefined as T
  return res.json()
}

const get = <T,>(url: string) => request<T>(url)

const send = <T,>(method: string, url: string, body?: unknown) =>
  request<T>(url, {
    method,
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })

const post = <T,>(url: string, body: unknown) => send<T>('POST', url, body)

export const getMuscleGroups = () =>
  get<MuscleGroupCard[]>('/api/v1/muscle-groups')

export const getMuscleGroup = (id: number) =>
  get<MuscleGroupCard>(`/api/v1/muscle-groups/${id}`)

export const getMuscles = () => get<Muscle[]>('/api/v1/muscles')

// каталог; все фильтры необязательные, группа — по основной мышце
export function getExercises(filter: ExerciseFilter = {}) {
  const params = new URLSearchParams()
  if (filter.q?.trim()) params.set('q', filter.q.trim())
  if (filter.muscleGroupId != null) params.set('muscleGroupId', String(filter.muscleGroupId))
  if (filter.favourite) params.set('favourite', 'true')
  if (filter.custom) params.set('custom', 'true')
  const query = params.toString()
  return get<ExerciseListItem[]>(`/api/v1/exercises${query ? `?${query}` : ''}`)
}

export const getExercisesByGroup = (muscleGroupId: number) => getExercises({ muscleGroupId })

export const getExercise = (id: number) =>
  get<ExerciseDetails>(`/api/v1/exercises/${id}`)

export const createExercise = (body: CreateExerciseRequest) => post<ExerciseDetails>('/api/v1/exercises', body)

export const setFavourite = (id: number, favourite: boolean) =>
  send<ExerciseDetails>('PUT', `/api/v1/exercises/${id}/favourite`, { favourite })

export const getWorkoutTemplates = () =>
  get<WorkoutTemplateListItem[]>('/api/v1/workout-templates')

export const createWorkoutTemplate = (body: CreateWorkoutTemplateRequest) =>
  post<WorkoutTemplateDetails>('/api/v1/workout-templates', body)

export const getWorkoutTemplate = (id: number) =>
  get<WorkoutTemplateDetails>(`/api/v1/workout-templates/${id}`)

export const getPlans = () => get<PlanListItem[]>('/api/v1/plans')

export const activatePlan = (id: number) => send<PlanDetails>('PATCH', `/api/v1/plans/${id}/activate`)

export const deletePlan = (id: number) => send<void>('DELETE', `/api/v1/plans/${id}`)

// тренировка на сегодня из активного плана; 204 — плана нет или сегодня отдых
export const getTodayWorkout = () =>
  request<TodayWorkout | undefined>('/api/v1/plans/active/today').then((t) => t ?? null)

// weekStart — любой день недели в формате 2026-11-02; без него — текущая неделя
export const getWeekSummary = (weekStart?: string) =>
  get<WeekSummary>(`/api/v1/stats/week${weekStart ? `?weekStart=${weekStart}` : ''}`)

export const getPlan = (id: number) => get<PlanDetails>(`/api/v1/plans/${id}`)

export const createPlan = (body: SavePlanRequest) => post<PlanDetails>('/api/v1/plans', body)

export const updatePlan = (id: number, body: SavePlanRequest) => send<PlanDetails>('PUT', `/api/v1/plans/${id}`, body)

// --- активная тренировка ---

const W = '/api/v1/workouts'

// событие окна: тренировка началась, завершилась или отменена (слушает шапка)
export const WORKOUT_CHANGED = 'gymtracker:workout-changed'

export async function getActiveWorkout(): Promise<WorkoutDetails | null> {
  const workout = await get<WorkoutDetails | undefined>(`${W}/active`)
  return workout ?? null
}

export const startWorkout = (body: StartWorkoutRequest) => post<WorkoutDetails>(W, body)

// история завершённых тренировок, новые сверху
export const getWorkoutHistory = (limit = 20) => get<WorkoutSummary[]>(`${W}?finished=true&limit=${limit}`)

export const removeWorkoutExercise = (id: number, workoutExerciseId: number) =>
  send<void>('DELETE', `${W}/${id}/exercises/${workoutExerciseId}`)

export const cancelWorkout = (id: number) => send<void>('DELETE', `${W}/${id}`)

export const finishWorkout = (id: number) => send<WorkoutSummary>('POST', `${W}/${id}/finish`)

export const addWorkoutExercise = (id: number, exerciseId: number) =>
  post<WorkoutExercise>(`${W}/${id}/exercises`, { exerciseId })

export const updateTargetSets = (id: number, workoutExerciseId: number, targetSets: number) =>
  send<WorkoutExercise>('PATCH', `${W}/${id}/exercises/${workoutExerciseId}`, { targetSets })

export const addSet = (id: number, workoutExerciseId: number, values: SetValues) =>
  post<WorkoutSet>(`${W}/${id}/exercises/${workoutExerciseId}/sets`, values)

export const updateSet = (id: number, workoutExerciseId: number, setId: number, values: SetValues) =>
  send<WorkoutSet>('PATCH', `${W}/${id}/exercises/${workoutExerciseId}/sets/${setId}`, values)

export const deleteSet = (id: number, workoutExerciseId: number, setId: number) =>
  send<void>('DELETE', `${W}/${id}/exercises/${workoutExerciseId}/sets/${setId}`)

export type StatsPeriod = 'MONTH' | 'QUARTER' | 'YEAR'

export type StatsOverview = {
  workoutsThisMonth: number
  workoutsPlannedThisMonth: number | null
  weekStreak: number | null
  volumeTonnes30d: number
  volumeTonnesPrev30d: number
  volumeChangePercent: number | null
  avgDurationSeconds: number | null
  avgSets: number | null
}

export type MuscleGroupStats = {
  groupId: number
  name: string
  sets: number
  targetSets: number
}

export type MonthlyWorkouts = {
  month: string
  count: number
  planned: number | null
}

export type ExerciseStatsItem = {
  id: number
  name: string
  imageUrl: string | null
  loadType: LoadType
  workingWeight: number | null
}

export type ProgressPoint = {
  weekStart: string
  weight: number
  reps: number
  record: boolean
}

export type ExerciseProgress = {
  exerciseId: number
  name: string
  loadType: LoadType
  points: ProgressPoint[]
  current: number | null
  delta: number | null
  deltaPercent: number | null
  estimatedMax: number | null
}

export type PersonalRecord = {
  exerciseId: number
  name: string
  loadType: LoadType
  date: string
  weight: number
  reps: number
  delta: number | null
}

export type PersonalRecords = {
  records: PersonalRecord[]
  totalInPeriod: number
}

export type BodyWeightPoint = {
  id: number
  date: string
  weight: number
  avg7: number | null
}

export type BodyWeightChart = {
  points: BodyWeightPoint[]
  current: number | null
  change30: number | null
  target: number | null
  toTarget: number | null
}

export type BodyWeight = { id: number; date: string; weight: number }

export type Profile = { targetBodyWeight: number | null }

const S = '/api/v1/stats'

export const getStatsOverview = (period: StatsPeriod) => get<StatsOverview>(`${S}/overview?period=${period}`)

export const getMuscleGroupStats = (period: StatsPeriod) =>
  get<MuscleGroupStats[]>(`${S}/muscle-groups?period=${period}`)

export const getMonthlyWorkouts = (months = 12) => get<MonthlyWorkouts[]>(`${S}/workouts/monthly?months=${months}`)

export const getStatsExercises = (period: StatsPeriod) => get<ExerciseStatsItem[]>(`${S}/exercises?period=${period}`)

export const getExerciseProgress = (id: number, period: StatsPeriod) =>
  get<ExerciseProgress>(`${S}/exercises/${id}/progress?period=${period}`)

export const getPersonalRecords = (period: StatsPeriod, limit = 5) =>
  get<PersonalRecords>(`${S}/records?period=${period}&limit=${limit}`)

export const getBodyWeightChart = (period: StatsPeriod) => get<BodyWeightChart>(`/api/v1/body-weights?period=${period}`)

export const saveBodyWeight = (body: { date?: string; weight: number }) => post<BodyWeight>('/api/v1/body-weights', body)

export const getProfile = () => get<Profile>('/api/v1/profile')

export const updateProfile = (body: Profile) => send<Profile>('PUT', '/api/v1/profile', body)
