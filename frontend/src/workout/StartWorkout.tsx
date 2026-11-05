import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import {
  getPlan,
  getPlans,
  getWorkoutTemplates,
  startWorkout,
  type PlanDay,
  type StartWorkoutRequest,
  type WorkoutDetails,
  type WorkoutTemplateListItem,
} from '../api'

// 1 = Пн ... 7 = Вс, как в плане
const todayDayOfWeek = () => ((new Date().getDay() + 6) % 7) + 1

function StartWorkout({ onStarted }: { onStarted: (w: WorkoutDetails) => void }) {
  const [templates, setTemplates] = useState<WorkoutTemplateListItem[] | null>(null)
  const [today, setToday] = useState<PlanDay | null>(null)
  const [freeName, setFreeName] = useState('')
  const [starting, setStarting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getWorkoutTemplates()
      .then(setTemplates)
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))

    // тренировка на сегодня из активного плана — подсказка сверху; если плана нет, просто не показываем
    getPlans()
      .then((plans) => plans.find((p) => p.active))
      .then((active) => (active ? getPlan(active.id) : null))
      .then((plan) => setToday(plan?.days.find((d) => d.dayOfWeek === todayDayOfWeek()) ?? null))
      .catch(() => setToday(null))
  }, [])

  async function start(body: StartWorkoutRequest) {
    setStarting(true)
    setError(null)
    try {
      onStarted(await startWorkout(body))
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
      setStarting(false)
    }
  }

  function startFree(e: FormEvent) {
    e.preventDefault()
    const name = freeName.trim()
    start(name ? { name } : {})
  }

  return (
    <section className="start">
      <h1 className="page-title">Начать тренировку</h1>
      {error && <p className="error">{error}</p>}

      {today && (
        <div className="start__today">
          <div>
            <span className="start__today-label">Сегодня по плану</span>
            <span className="start__today-name">{today.workoutTemplateName}</span>
          </div>
          <button
            className="primary-button"
            disabled={starting}
            onClick={() => start({ workoutTemplateId: today.workoutTemplateId })}
          >
            Начать
          </button>
        </div>
      )}

      <h2 className="start__subtitle">По тренировке из библиотеки</h2>
      {templates === null && !error && <p className="muted">Загрузка…</p>}
      {templates?.length === 0 && (
        <p className="muted">
          В библиотеке пока нет тренировок. <Link to="/workouts">Создать</Link>
        </p>
      )}
      {templates && templates.length > 0 && (
        <ul className="start__templates">
          {templates.map((t) => (
            <li key={t.id} className="start__template">
              <span className="start__template-name">{t.name}</span>
              <span className="start__template-count">{t.exerciseCount} упр.</span>
              <button
                className="gray-button"
                disabled={starting}
                onClick={() => start({ workoutTemplateId: t.id })}
              >
                Начать
              </button>
            </li>
          ))}
        </ul>
      )}

      <h2 className="start__subtitle">Свободная тренировка</h2>
      <form className="start__free" onSubmit={startFree}>
        <input
          className="field__input"
          value={freeName}
          onChange={(e) => setFreeName(e.target.value)}
          placeholder="Название (необязательно)"
          maxLength={100}
        />
        <button type="submit" className="gray-button" disabled={starting}>
          Начать свободную
        </button>
      </form>
    </section>
  )
}

export default StartWorkout
