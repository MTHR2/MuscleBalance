import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  createPlan,
  getPlan,
  getWorkoutTemplates,
  updatePlan,
  type PlanDay,
  type WorkoutTemplateListItem,
} from '../api'
import { DEFAULT_TARGET_SETS } from '../heatmap/stats'
import Modal from '../Modal'
import TargetsEditor, { parseTarget, type Targets } from '../TargetsEditor'
import WeekSchedule from '../WeekSchedule'
import { WEEKDAYS } from '../weekdays'
import './CreatePlanPage.css'

// создание плана и его изменение (/plans/:planId/edit — те же поля, уже заполненные)
function CreatePlanPage() {
  const navigate = useNavigate()
  const params = useParams()
  const editId = params.planId ? Number(params.planId) : null
  const [loading, setLoading] = useState(editId !== null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [days, setDays] = useState<PlanDay[]>([])
  const [targets, setTargets] = useState<Targets>({ defaultWeeklySets: String(DEFAULT_TARGET_SETS), perMuscle: {} })
  const [adding, setAdding] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const closePicker = useCallback(() => setAdding(false), [])

  useEffect(() => {
    if (editId === null) return
    getPlan(editId)
      .then((p) => {
        setName(p.name)
        setDescription(p.description ?? '')
        setDays(p.days)
        setTargets({
          defaultWeeklySets: p.defaultWeeklySets !== null ? String(p.defaultWeeklySets) : '',
          perMuscle: Object.fromEntries(p.muscleTargets.map((t) => [t.muscleId, String(t.weeklySets)])),
        })
      })
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))
      .finally(() => setLoading(false))
  }, [editId])

  // в день одна тренировка: новая заменяет то, что стояло в этот день
  function addDay(day: PlanDay) {
    setDays((list) => [...list.filter((d) => d.dayOfWeek !== day.dayOfWeek), day].sort((a, b) => a.dayOfWeek - b.dayOfWeek))
    setAdding(false)
  }

  const defaultTarget = parseTarget(targets.defaultWeeklySets, 1)
  const muscleTargets = Object.entries(targets.perMuscle).map(([id, raw]) => ({
    muscleId: Number(id),
    weeklySets: parseTarget(raw),
  }))
  const targetsInvalid = defaultTarget === 'invalid' || muscleTargets.some((t) => t.weeklySets === 'invalid')

  async function save(e: FormEvent) {
    e.preventDefault()
    if (targetsInvalid) return
    setSaving(true)
    setError(null)
    try {
      const body = {
        name: name.trim(),
        description: description.trim() || null,
        days: days.map((d) => ({ workoutTemplateId: d.workoutTemplateId, dayOfWeek: d.dayOfWeek })),
        defaultWeeklySets: defaultTarget as number | null,
        muscleTargets: muscleTargets
          .filter((t) => t.weeklySets !== null)
          .map((t) => ({ muscleId: t.muscleId, weeklySets: t.weeklySets as number })),
      }
      const plan = editId !== null ? await updatePlan(editId, body) : await createPlan(body)
      navigate(`/plans/${plan.id}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err))
      setSaving(false)
    }
  }

  const canSave = name.trim() !== '' && days.length > 0 && !targetsInvalid && !saving

  if (loading) {
    return (
      <main className="page plan-form-page">
        <p className="muted">Загрузка…</p>
      </main>
    )
  }

  return (
    <main className="page plan-form-page">
      <Link to={editId !== null ? `/plans/${editId}` : '/plans'} className="back-link">
        ← {editId !== null ? 'К плану' : 'Мои планы'}
      </Link>
      <h1 className="page-title">{editId !== null ? 'Изменить план' : 'Новый план'}</h1>
      {editId !== null && (
        <p className="page-subtitle">Изменения коснутся только будущих тренировок, проведённые останутся как были.</p>
      )}

      <form className="plan-form" onSubmit={save}>
        <label className="field">
          <span className="field__label">Название</span>
          <input
            className="field__input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Например, Сплит на 3 дня"
            autoFocus
          />
        </label>

        <label className="field">
          <span className="field__label">
            Описание <span className="field__optional">необязательно</span>
          </span>
          <textarea
            className="field__input field__textarea"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
          />
        </label>

        <div className="field">
          <span className="field__label">Неделя</span>
          <div className="plan-form__week">
            <WeekSchedule
              days={days}
              onRemove={(dayOfWeek) => setDays((list) => list.filter((d) => d.dayOfWeek !== dayOfWeek))}
            />
          </div>
          <button type="button" className="gray-button" onClick={() => setAdding(true)}>
            + Добавить тренировку из списка
          </button>
        </div>

        <TargetsEditor value={targets} onChange={setTargets} />
        {targetsInvalid && <p className="error">Цель — целое число от 0 до 100 (общая — от 1).</p>}

        {error && <p className="error">{error}</p>}

        <div className="plan-form__footer">
          <button type="submit" className="primary-button" disabled={!canSave}>
            {saving ? 'Сохранение…' : 'Сохранить'}
          </button>
        </div>
      </form>

      {adding && <AddWorkoutModal busyDays={days} onAdd={addDay} onClose={closePicker} />}
    </main>
  )
}

// выбор тренировки из библиотеки и дня недели
function AddWorkoutModal({
  busyDays,
  onAdd,
  onClose,
}: {
  busyDays: PlanDay[]
  onAdd: (day: PlanDay) => void
  onClose: () => void
}) {
  const [templates, setTemplates] = useState<WorkoutTemplateListItem[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [templateId, setTemplateId] = useState<number | null>(null)
  // по умолчанию первый свободный день
  const [day, setDay] = useState<number>(
    () => WEEKDAYS.find((w) => !busyDays.some((d) => d.dayOfWeek === w.day))?.day ?? 1,
  )

  useEffect(() => {
    getWorkoutTemplates()
      .then(setTemplates)
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))
  }, [])

  const busy = busyDays.find((d) => d.dayOfWeek === day)
  const template = templates?.find((t) => t.id === templateId)

  return (
    <Modal title="Добавить тренировку" onClose={onClose}>
      <div className="add-workout">
        <div className="field">
          <span className="field__label">Тренировка</span>
          {error && <p className="error">Ошибка: {error}</p>}
          {!error && templates === null && <p className="muted">Загрузка…</p>}
          {templates?.length === 0 && (
            <p className="muted">
              В библиотеке пока нет тренировок. <Link to="/workouts">Создайте первую</Link>.
            </p>
          )}
          {templates && templates.length > 0 && (
            <div className="template-options">
              {templates.map((t) => (
                <button
                  key={t.id}
                  type="button"
                  className={`template-option${t.id === templateId ? ' template-option--selected' : ''}`}
                  onClick={() => setTemplateId(t.id)}
                  aria-pressed={t.id === templateId}
                >
                  <span className="template-option__name">{t.name}</span>
                  <span className="template-option__count">{t.exerciseCount} упр.</span>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="field">
          <span className="field__label">День недели</span>
          <div className="day-options">
            {WEEKDAYS.map((w) => {
              const taken = busyDays.some((d) => d.dayOfWeek === w.day)
              return (
                <button
                  key={w.day}
                  type="button"
                  className={`chip-button day-option${w.day === day ? ' chip-button--active' : ''}${taken ? ' day-option--taken' : ''}`}
                  onClick={() => setDay(w.day)}
                  title={taken ? 'В этот день уже есть тренировка' : w.full}
                >
                  {w.short}
                </button>
              )
            })}
          </div>
          {busy && <p className="muted add-workout__hint">Заменит «{busy.workoutTemplateName}» в этот день.</p>}
        </div>

        <div className="plan-form__footer">
          <button
            type="button"
            className="primary-button"
            disabled={!template}
            onClick={() => template && onAdd({ dayOfWeek: day, workoutTemplateId: template.id, workoutTemplateName: template.name })}
          >
            Добавить
          </button>
        </div>
      </div>
    </Modal>
  )
}

export default CreatePlanPage
