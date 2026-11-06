import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  getActiveWorkout,
  getExercises,
  getMuscles,
  getPlan,
  getPlans,
  getWorkoutHistory,
  getWorkoutTemplate,
  getWorkoutTemplates,
  startWorkout,
  WORKOUT_CHANGED,
  type StartWorkoutRequest,
  type WorkoutSummary,
  type WorkoutTemplateDetails,
} from '../api'
import CreateWorkoutModal from '../CreateWorkoutModal'
import { PlusIcon } from '../icons'
import { plural } from '../plural'
import { formatDuration } from '../workout/time'
import './WorkoutsPage.css'

const DAY_PLURAL_DATIVE = ['', 'понедельникам', 'вторникам', 'средам', 'четвергам', 'пятницам', 'субботам', 'воскресеньям']
const WEEKDAY_SHORT = ['вс', 'пн', 'вт', 'ср', 'чт', 'пт', 'сб']
const todayDayOfWeek = () => ((new Date().getDay() + 6) % 7) + 1

type TemplateCard = WorkoutTemplateDetails & {
  totalSets: number
  days: number[]
  groups: string[]
}

const errorText = (e: unknown) => (e instanceof Error ? e.message : String(e))

function mondayStart() {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  d.setDate(d.getDate() - ((d.getDay() + 6) % 7))
  return d
}

function formatVolume(kg: number | null) {
  if (kg === null || kg === 0) return '—'
  return kg >= 1000 ? `${String(Math.round(kg / 100) / 10).replace('.', ',')} т` : `${Math.round(kg)} кг`
}

function WorkoutsPage() {
  const navigate = useNavigate()
  const [cards, setCards] = useState<TemplateCard[] | null>(null)
  const [history, setHistory] = useState<WorkoutSummary[] | null>(null)
  const [hasActive, setHasActive] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [historyError, setHistoryError] = useState<string | null>(null)
  const [startError, setStartError] = useState<string | null>(null)
  const [starting, setStarting] = useState(false)
  const [freeName, setFreeName] = useState('')
  const [creating, setCreating] = useState(false)

  const load = useCallback(() => {
    Promise.all([
      getWorkoutTemplates(),
      getExercises().catch(() => []),
      getMuscles().catch(() => []),
      getPlans()
        .then((plans) => plans.find((p) => p.active))
        .then((active) => (active ? getPlan(active.id) : null))
        .catch(() => null),
    ])
      .then(async ([templates, catalog, muscles, plan]) => {
        const details = await Promise.all(templates.map((t) => getWorkoutTemplate(t.id)))
        const groupOfMuscle = new Map(muscles.map((m) => [m.id, m.groupName]))
        const exerciseGroups = new Map(
          catalog.map((e) => [
            e.id,
            (e.muscles ?? []).filter((m) => m.role === 'primary').map((m) => groupOfMuscle.get(m.muscleId)),
          ]),
        )
        setCards(
          details.map((t) => {
            const counts = new Map<string, number>()
            for (const ex of t.exercises) {
              for (const g of exerciseGroups.get(ex.exerciseId) ?? []) {
                if (g) counts.set(g, (counts.get(g) ?? 0) + ex.targetSets)
              }
            }
            return {
              ...t,
              totalSets: t.exercises.reduce((sum, e) => sum + e.targetSets, 0),
              days: plan?.days.filter((d) => d.workoutTemplateId === t.id).map((d) => d.dayOfWeek) ?? [],
              groups: [...counts.entries()].sort((a, b) => b[1] - a[1]).slice(0, 3).map(([g]) => g),
            }
          }),
        )
      })
      .catch((e) => setError(errorText(e)))
  }, [])

  useEffect(load, [load])

  useEffect(() => {
    getWorkoutHistory()
      .then(setHistory)
      .catch((e) => setHistoryError(errorText(e)))
    getActiveWorkout()
      .then((w) => setHasActive(w !== null))
      .catch(() => setHasActive(false))
  }, [])

  const closeModal = useCallback(() => setCreating(false), [])

  async function start(body: StartWorkoutRequest) {
    if (hasActive) {
      navigate('/workout')
      return
    }
    setStarting(true)
    setStartError(null)
    try {
      await startWorkout(body)
      window.dispatchEvent(new Event(WORKOUT_CHANGED))
      navigate('/workout')
    } catch (e) {
      setStartError(errorText(e))
      setStarting(false)
    }
  }

  function startFree(e: FormEvent) {
    e.preventDefault()
    const name = freeName.trim()
    start(name ? { name } : {})
  }

  const today = todayDayOfWeek()
  const weekFrom = mondayStart().getTime()
  const thisWeek = history?.filter((h) => new Date(h.startedAt).getTime() >= weekFrom) ?? []
  const weekSets = thisWeek.reduce((sum, h) => sum + h.completedSets, 0)
  const weekSeconds = thisWeek.reduce((sum, h) => sum + h.durationSeconds, 0)

  return (
    <main className="page workouts">
      <div className="page-head">
        <div>
          <h1 className="page-title">Тренировки</h1>
          <p className="page-subtitle">Шаблоны собираются один раз и ставятся в любой план</p>
        </div>
        <button type="button" className="primary-button" onClick={() => setCreating(true)}>
          + Новый шаблон
        </button>
      </div>

      {hasActive && (
        <p className="workouts__active">
          Сейчас идёт тренировка.{' '}
          <button type="button" className="workouts__link" onClick={() => navigate('/workout')}>
            Продолжить
          </button>
        </p>
      )}
      {startError && <p className="error">Не удалось начать: {startError}</p>}
      {error && <p className="error">Ошибка: {error}</p>}
      {!error && cards === null && <p className="muted">Загрузка…</p>}

      <section className="templates" aria-label="Мои шаблоны">
        {cards?.map((t) => {
          const isToday = t.days.includes(today)
          const thumbs = t.exercises.filter((e) => e.imageUrl).slice(0, 3)
          return (
            <article key={t.id} className="template-card">
              <div className="template-card__thumbs">
                {thumbs.map((e) => (
                  <img key={e.exerciseId} src={e.imageUrl!} alt="" className="template-card__thumb" />
                ))}
                {Array.from({ length: 3 - thumbs.length }, (_, i) => (
                  <span key={i} className="template-card__thumb template-card__thumb--empty" />
                ))}
              </div>
              <div>
                <h2 className="template-card__name">{t.name}</h2>
                <p className="template-card__meta">
                  {t.exercises.length} {plural(t.exercises.length, ['упражнение', 'упражнения', 'упражнений'])} ·{' '}
                  {t.totalSets} {plural(t.totalSets, ['подход', 'подхода', 'подходов'])}
                  {t.days.length > 0 && ` · в плане по ${t.days.map((d) => DAY_PLURAL_DATIVE[d]).join(' и ')}`}
                </p>
              </div>
              {t.groups.length > 0 && (
                <div className="template-card__tags">
                  {t.groups.map((g) => (
                    <span key={g} className="tag">
                      {g}
                    </span>
                  ))}
                </div>
              )}
              <button
                type="button"
                className={`template-card__start ${isToday ? 'accent-button' : 'gray-button'}`}
                disabled={starting || t.exercises.length === 0}
                onClick={() => start({ workoutTemplateId: t.id })}
              >
                {isToday ? 'Начать — сегодня по плану' : 'Начать'}
              </button>
            </article>
          )
        })}

        <form className="template-card template-card--free" onSubmit={startFree}>
          <span className="template-card__plus" aria-hidden="true">
            <PlusIcon size={20} />
          </span>
          <h2 className="template-card__name">Свободная тренировка</h2>
          <p className="template-card__meta">Без шаблона: упражнения добавляете по ходу</p>
          <input
            className="field__input"
            value={freeName}
            onChange={(e) => setFreeName(e.target.value)}
            placeholder="Название (необязательно)"
            aria-label="Название свободной тренировки"
            maxLength={100}
          />
          <button type="submit" className="gray-button template-card__start" disabled={starting}>
            Начать свободную
          </button>
        </form>
      </section>

      {cards?.length === 0 && (
        <p className="muted">Шаблонов пока нет. Нажмите «Новый шаблон», чтобы собрать первый.</p>
      )}

      <section className="history-block" aria-labelledby="history-title">
        <div className="history-block__head">
          <h2 id="history-title" className="section-title">
            История
          </h2>
          {thisWeek.length > 0 && (
            <span className="history-block__week">
              Эта неделя: {thisWeek.length} {plural(thisWeek.length, ['тренировка', 'тренировки', 'тренировок'])} ·{' '}
              {weekSets} {plural(weekSets, ['подход', 'подхода', 'подходов'])} · {formatDuration(weekSeconds)}
            </span>
          )}
        </div>
        {historyError && <p className="error">Ошибка: {historyError}</p>}
        {!historyError && history === null && <p className="muted">Загрузка…</p>}
        {history?.length === 0 && <p className="muted">Завершённых тренировок пока нет.</p>}
        {history && history.length > 0 && (
          <ol className="history">
            {history.map((h) => {
              const date = new Date(h.startedAt)
              const byPlan = h.completionPercent !== null
              return (
                <li key={h.id} className="history__item">
                  <div className="history__date">
                    <span className="history__day">{date.getDate()}</span>
                    <span className="history__weekday">{WEEKDAY_SHORT[date.getDay()]}</span>
                  </div>
                  <div className="history__main">
                    <span className="history__name">{h.name}</span>
                    <span className={byPlan ? 'tag tag--plan' : 'tag'}>{byPlan ? 'по шаблону' : 'свободная'}</span>
                  </div>
                  <dl className="history__stats">
                    <div>
                      <dt>время</dt>
                      <dd>{formatDuration(h.durationSeconds)}</dd>
                    </div>
                    <div>
                      <dt>подходы</dt>
                      <dd>{h.completedSets}</dd>
                    </div>
                    <div>
                      <dt>объём</dt>
                      <dd>{formatVolume(h.volume)}</dd>
                    </div>
                    <div>
                      <dt>план</dt>
                      <dd className={h.completionPercent !== null && h.completionPercent >= 100 ? 'history__done' : ''}>
                        {h.completionPercent !== null ? `${h.completionPercent}%` : '—'}
                      </dd>
                    </div>
                  </dl>
                </li>
              )
            })}
          </ol>
        )}
      </section>

      {creating && (
        <CreateWorkoutModal
          onClose={closeModal}
          onCreated={() => {
            setCreating(false)
            load()
          }}
        />
      )}
    </main>
  )
}

export default WorkoutsPage
