import { useCallback, useEffect, useState } from 'react'
import { getWorkoutHistory, getWorkoutTemplates, type WorkoutSummary, type WorkoutTemplateListItem } from '../api'
import CreateWorkoutModal from '../CreateWorkoutModal'
import { plural } from '../plural'
import { formatDuration } from '../workout/time'
import './WorkoutsPage.css'

// библиотека тренировок и история проведённых
function WorkoutsPage() {
  const [workouts, setWorkouts] = useState<WorkoutTemplateListItem[] | null>(null)
  const [history, setHistory] = useState<WorkoutSummary[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [historyError, setHistoryError] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)

  const load = useCallback(() => {
    getWorkoutTemplates()
      .then(setWorkouts)
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))
  }, [])

  useEffect(load, [load])

  useEffect(() => {
    getWorkoutHistory()
      .then(setHistory)
      .catch((e) => setHistoryError(e instanceof Error ? e.message : String(e)))
  }, [])

  const closeModal = useCallback(() => setCreating(false), [])

  return (
    <main className="page page--narrow">
      <div className="page-head">
        <h1 className="page-title">Мои тренировки</h1>
        <button className="primary-button" onClick={() => setCreating(true)}>
          + Добавить
        </button>
      </div>

      {error && <p className="error">Ошибка: {error}</p>}
      {!error && workouts === null && <p className="muted">Загрузка…</p>}
      {workouts?.length === 0 && <p className="muted">Тренировок пока нет. Нажмите «Добавить», чтобы создать первую.</p>}
      {workouts && workouts.length > 0 && (
        <ul className="workout-list">
          {workouts.map((w) => (
            <li key={w.id} className="workout-row">
              <span className="workout-row__name">{w.name}</span>
              <span className="workout-row__count">
                {w.exerciseCount} {plural(w.exerciseCount, ['упражнение', 'упражнения', 'упражнений'])}
              </span>
            </li>
          ))}
        </ul>
      )}

      <h2 className="section-title">История</h2>
      {historyError && <p className="error">Ошибка: {historyError}</p>}
      {!historyError && history === null && <p className="muted">Загрузка…</p>}
      {history?.length === 0 && <p className="muted">Завершённых тренировок пока нет.</p>}
      {history && history.length > 0 && (
        <ul className="history">
          {history.map((h) => (
            <li key={h.id} className="history__item">
              <div className="history__main">
                <span className="history__name">{h.name}</span>
                <time className="history__date" dateTime={h.startedAt}>
                  {formatDate(h.startedAt)}
                </time>
              </div>
              <dl className="history__stats">
                <div>
                  <dt>длительность</dt>
                  <dd>{formatDuration(h.durationSeconds)}</dd>
                </div>
                <div>
                  <dt>{plural(h.completedSets, ['подход', 'подхода', 'подходов'])}</dt>
                  <dd>{h.completedSets}</dd>
                </div>
                <div>
                  <dt>план</dt>
                  <dd>{h.completionPercent !== null ? `${h.completionPercent}%` : 'свободная'}</dd>
                </div>
              </dl>
            </li>
          ))}
        </ul>
      )}

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

// «пн, 2 ноября, 18:40»
function formatDate(iso: string) {
  return new Date(iso).toLocaleString('ru-RU', {
    weekday: 'short',
    day: 'numeric',
    month: 'long',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default WorkoutsPage
