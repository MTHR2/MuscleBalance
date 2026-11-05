import { useCallback, useEffect, useState } from 'react'
import {
  addWorkoutExercise,
  cancelWorkout,
  finishWorkout,
  type ExerciseListItem,
  type WorkoutDetails,
  type WorkoutExercise,
  type WorkoutSummary,
} from '../api'
import { ExercisePicker } from '../CreateWorkoutModal'
import Modal from '../Modal'
import ExerciseBlock, { workingSets } from './ExerciseBlock'
import { formatClock, useElapsedSeconds, useNow } from './time'

// отдых показываем, пока с последнего подхода прошло меньше 15 минут
const REST_LIMIT_MS = 15 * 60 * 1000

function ActiveWorkout({
  workout,
  onChange,
  onFinished,
  onCancelled,
}: {
  workout: WorkoutDetails
  onChange: (w: WorkoutDetails) => void
  onFinished: (s: WorkoutSummary) => void
  onCancelled: () => void
}) {
  const elapsed = useElapsedSeconds(workout.startedAt)
  const now = useNow()
  const [picking, setPicking] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  // время последнего записанного подхода — для таймера отдыха (только в этой вкладке)
  const [lastSetAt, setLastSetAt] = useState<number | null>(null)

  // текущее — первое упражнение, где рабочих подходов меньше цели
  const current =
    workout.exercises.find((e) => workingSets(e) < e.targetSets) ?? workout.exercises[workout.exercises.length - 1]
  const [openId, setOpenId] = useState<number | null>(current?.id ?? null)

  // когда текущее упражнение выполнено, раскрываем следующее
  useEffect(() => {
    if (current) setOpenId(current.id)
  }, [current?.id])

  const closePicker = useCallback(() => setPicking(false), [])

  const replaceExercise = (ex: WorkoutExercise) =>
    onChange({ ...workout, exercises: workout.exercises.map((e) => (e.id === ex.id ? ex : e)) })

  const hasWorkingSets = workout.exercises.some((e) => workingSets(e) > 0)
  const doneSets = workout.exercises.reduce((n, e) => n + workingSets(e), 0)
  const totalSets = workout.exercises.reduce((n, e) => n + e.targetSets, 0)
  const rest = lastSetAt !== null && now - lastSetAt < REST_LIMIT_MS ? (now - lastSetAt) / 1000 : null

  async function run(action: () => Promise<void>) {
    setBusy(true)
    setError(null)
    try {
      await action()
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setBusy(false)
    }
  }

  const addExercise = (ex: ExerciseListItem) =>
    run(async () => {
      const added = await addWorkoutExercise(workout.id, ex.id)
      onChange({ ...workout, exercises: [...workout.exercises, added] })
      setOpenId(added.id)
      setPicking(false)
    })

  const finish = () => {
    if (!hasWorkingSets) {
      setError('Нет ни одного рабочего подхода — такую тренировку можно только отменить.')
      return
    }
    run(async () => onFinished(await finishWorkout(workout.id)))
  }

  const cancel = () => {
    if (!window.confirm('Отменить тренировку? Все подходы будут удалены.')) return
    run(async () => {
      await cancelWorkout(workout.id)
      onCancelled()
    })
  }

  return (
    <div className="active">
      <section className="active__main" aria-label="Упражнения">
        {workout.exercises.length === 0 && (
          <p className="muted active__empty">Упражнений пока нет — добавьте первое.</p>
        )}
        {workout.exercises.map((ex) => (
          <ExerciseBlock
            key={ex.id}
            workoutId={workout.id}
            exercise={ex}
            open={ex.id === openId}
            current={ex.id === current?.id}
            onToggle={() => setOpenId((id) => (id === ex.id ? null : ex.id))}
            onChange={replaceExercise}
            onSetLogged={() => setLastSetAt(Date.now())}
            onRemoved={() => onChange({ ...workout, exercises: workout.exercises.filter((e) => e.id !== ex.id) })}
          />
        ))}
        <button type="button" className="gray-button active__add" onClick={() => setPicking(true)}>
          + Добавить упражнение
        </button>
      </section>

      <aside className="active__side">
        <section className="clock" aria-label="Время и прогресс">
          <p className="clock__name">{workout.name}</p>
          <div className="clock__time" aria-label="Время тренировки">
            {formatClock(elapsed)}
          </div>
          <div className="clock__progress-label">
            <span>Подходы</span>
            <span>
              {doneSets} из {totalSets}
            </span>
          </div>
          <div className="clock__progress" aria-hidden="true">
            <div style={{ width: `${totalSets ? Math.min((doneSets / totalSets) * 100, 100) : 0}%` }} />
          </div>
          <div className="clock__rest">
            <span>Отдых</span>
            <span className="clock__rest-time">{rest === null ? '—' : formatClock(rest)}</span>
          </div>
        </section>

        <div className="active__actions">
          <button type="button" className="accent-button" onClick={finish} disabled={busy}>
            Завершить<span className="hide-narrow"> тренировку</span>
          </button>
          <button type="button" className="outline-button" onClick={cancel} disabled={busy}>
            Отменить
          </button>
        </div>
        {error && <p className="error active__error">{error}</p>}
        <p className="active__hint">
          Разминочные подходы не идут в итоги и рабочий вес. Удалить можно только последний подход.
        </p>
      </aside>

      {picking && (
        <Modal title="Добавить упражнение" onClose={closePicker}>
          <p className="muted active__picker-hint">Нажмите на упражнение, чтобы добавить его в тренировку.</p>
          <ExercisePicker
            selectedIds={workout.exercises.map((e) => e.exerciseId)}
            onToggle={(ex) => {
              // одно упражнение один раз в тренировке: уже добавленные не трогаем
              if (!busy && !workout.exercises.some((e) => e.exerciseId === ex.id)) addExercise(ex)
            }}
          />
          {error && <p className="error">{error}</p>}
        </Modal>
      )}
    </div>
  )
}

export default ActiveWorkout
