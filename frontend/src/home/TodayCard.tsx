import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  getActiveWorkout,
  getPlan,
  getPlans,
  getTodayWorkout,
  startWorkout,
  type PlanDetails,
  type TodayWorkout,
} from '../api'
import { CheckIcon, PlayIcon } from '../icons'
import { plural } from '../plural'
import { WEEKDAYS } from '../weekdays'

// 1 = Пн ... 7 = Вс, как в плане
const todayDayOfWeek = () => ((new Date().getDay() + 6) % 7) + 1

type State =
  | { kind: 'loading' }
  | { kind: 'active' } // тренировка уже идёт
  | { kind: 'no-plan' }
  | { kind: 'rest'; plan: PlanDetails }
  | { kind: 'today'; plan: PlanDetails; template: TodayWorkout }

// тёмная карточка «Сегодня по плану»: тренировка на сегодняшний день недели из активного плана
function TodayCard() {
  const navigate = useNavigate()
  const [state, setState] = useState<State>({ kind: 'loading' })
  const [starting, setStarting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function load(): Promise<State> {
      if (await getActiveWorkout().catch(() => null)) return { kind: 'active' }
      const active = (await getPlans()).find((p) => p.active)
      if (!active) return { kind: 'no-plan' }
      // тренировку на сегодня считает бэк; план нужен ради названия и подсказки «следующая» в день отдыха
      const [plan, template] = await Promise.all([getPlan(active.id), getTodayWorkout()])
      return template ? { kind: 'today', plan, template } : { kind: 'rest', plan }
    }
    load()
      .then(setState)
      .catch(() => setState({ kind: 'no-plan' }))
  }, [])

  async function start(workoutTemplateId: number) {
    setStarting(true)
    setError(null)
    try {
      await startWorkout({ workoutTemplateId })
      navigate('/workout')
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
      setStarting(false)
    }
  }

  const weekday = WEEKDAYS.find((w) => w.day === todayDayOfWeek())!
  const totalSets = state.kind === 'today' ? state.template.exercises.reduce((n, e) => n + e.targetSets, 0) : 0

  return (
    <section className="today" aria-labelledby="today-title">
      {state.kind === 'loading' && <p className="today__muted">Загрузка…</p>}

      {state.kind === 'active' && (
        <>
          <span className="today__label">Идёт тренировка</span>
          <h2 id="today-title" className="today__title">
            Тренировка не завершена
          </h2>
          <p className="today__muted">Вернитесь к ней, чтобы записать оставшиеся подходы.</p>
          <Link to="/workout" className="today__start">
            <PlayIcon />
            Продолжить
          </Link>
        </>
      )}

      {state.kind === 'no-plan' && (
        <>
          <span className="today__label">Нет активного плана</span>
          <h2 id="today-title" className="today__title">
            Тренируйтесь по плану
          </h2>
          <p className="today__muted">Составьте неделю из тренировок, и здесь появится тренировка на сегодня.</p>
          <Link to="/plans/new" className="today__start">
            Создать план
          </Link>
          <Link to="/workout" className="today__alt">
            или свободная тренировка
          </Link>
        </>
      )}

      {state.kind === 'rest' && (
        <>
          <span className="today__label">{weekday.full} · отдых</span>
          <h2 id="today-title" className="today__title">
            Сегодня день отдыха
          </h2>
          <p className="today__muted">
            {nextWorkout(state.plan) ?? 'В плане нет тренировок'} · план «{state.plan.name}»
          </p>
          <Link to="/workout" className="today__alt">
            Всё равно потренироваться
          </Link>
        </>
      )}

      {state.kind === 'today' && (
        <>
          {state.template.completedToday ? (
            <span className="today__label today__label--done">
              <CheckIcon size={14} /> Сегодня сделано · {weekday.short}
            </span>
          ) : (
            <span className="today__label">Сегодня по плану · {weekday.short}</span>
          )}
          <h2 id="today-title" className="today__title">
            {state.template.name}
          </h2>
          <p className="today__muted">
            {state.template.exercises.length} упр. · {totalSets} {plural(totalSets, ['подход', 'подхода', 'подходов'])}{' '}
            · план «{state.plan.name}»
          </p>
          <ul className="today__thumbs" aria-label="Упражнения">
            {state.template.exercises.slice(0, 4).map((e) => (
              <li key={e.exerciseId} title={e.exerciseName}>
                {e.imageUrl ? (
                  <img src={e.imageUrl} alt={e.exerciseName} />
                ) : (
                  <span className="today__thumb-name">{e.exerciseName}</span>
                )}
              </li>
            ))}
            {state.template.exercises.length > 4 && (
              <li className="today__thumb-more">+{state.template.exercises.length - 4}</li>
            )}
          </ul>
          {state.template.completedToday ? (
            <>
              <p className="today__done">Тренировка на сегодня уже выполнена. Отдыхайте!</p>
              <button
                type="button"
                className="today__again"
                disabled={starting}
                onClick={() => start(state.template.id)}
              >
                {starting ? 'Запуск…' : 'Пройти ещё раз'}
              </button>
            </>
          ) : (
            <>
              <button
                type="button"
                className="today__start"
                disabled={starting}
                onClick={() => start(state.template.id)}
              >
                <PlayIcon />
                {starting ? 'Запуск…' : 'Начать'}
              </button>
              <Link to="/workout" className="today__alt">
                или свободная тренировка
              </Link>
            </>
          )}
        </>
      )}

      {error && <p className="today__error">{error}</p>}
    </section>
  )
}

// «Следующая: Ср — Спина + бицепс», начиная с завтрашнего дня по кругу недели
function nextWorkout(plan: PlanDetails) {
  const today = todayDayOfWeek()
  for (let i = 1; i <= 7; i++) {
    const day = ((today - 1 + i) % 7) + 1
    const entry = plan.days.find((d) => d.dayOfWeek === day)
    if (entry) return `Следующая: ${WEEKDAYS[day - 1].short} — ${entry.workoutTemplateName}`
  }
  return null
}

export default TodayCard
