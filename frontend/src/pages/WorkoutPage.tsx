import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getActiveWorkout, WORKOUT_CHANGED, type WorkoutDetails, type WorkoutSummary } from '../api'
import StartWorkout from '../workout/StartWorkout'
import ActiveWorkout from '../workout/ActiveWorkout'
import { formatDuration } from '../workout/time'
import './WorkoutPage.css'

type Phase =
  | { kind: 'loading' }
  | { kind: 'start' }
  | { kind: 'active'; workout: WorkoutDetails }
  | { kind: 'summary'; summary: WorkoutSummary }

// одна страница на весь сценарий: нет активной → старт, есть → экран тренировки, после «Завершить» → итоги
function WorkoutPage() {
  const [phase, setPhase] = useState<Phase>({ kind: 'loading' })
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getActiveWorkout()
      .then((w) => setPhase(w ? { kind: 'active', workout: w } : { kind: 'start' }))
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))
  }, [])

  // шапка показывает «Идёт тренировка» — сообщаем ей о старте, завершении и отмене
  useEffect(() => {
    window.dispatchEvent(new Event(WORKOUT_CHANGED))
  }, [phase.kind])

  return (
    <main className="page workout-page">
      {error && <p className="error">Ошибка: {error}</p>}
      {!error && phase.kind === 'loading' && <p className="muted">Загрузка…</p>}
      {phase.kind === 'start' && <StartWorkout onStarted={(w) => setPhase({ kind: 'active', workout: w })} />}
      {phase.kind === 'active' && (
        <ActiveWorkout
          workout={phase.workout}
          onChange={(w) => setPhase({ kind: 'active', workout: w })}
          onFinished={(s) => setPhase({ kind: 'summary', summary: s })}
          onCancelled={() => setPhase({ kind: 'start' })}
        />
      )}
      {phase.kind === 'summary' && <Summary summary={phase.summary} onAgain={() => setPhase({ kind: 'start' })} />}
    </main>
  )
}

function Summary({ summary, onAgain }: { summary: WorkoutSummary; onAgain: () => void }) {
  return (
    <section className="summary">
      <p className="summary__kicker">Тренировка завершена</p>
      <h1 className="page-title">{summary.name}</h1>

      <div className="summary__stats">
        <div className="stat">
          <span className="stat__value">
            {summary.completionPercent != null ? `${summary.completionPercent}%` : '—'}
          </span>
          <span className="stat__label">
            {summary.completionPercent != null ? 'выполнение плана' : 'свободная, без плана'}
          </span>
        </div>
        <div className="stat">
          <span className="stat__value">{formatDuration(summary.durationSeconds)}</span>
          <span className="stat__label">длительность</span>
        </div>
        <div className="stat">
          <span className="stat__value">{summary.completedSets}</span>
          <span className="stat__label">рабочих подходов</span>
        </div>
      </div>

      <div className="summary__actions">
        <Link to="/" className="primary-button">
          На главную
        </Link>
        <button className="gray-button" onClick={onAgain}>
          Начать ещё одну
        </button>
      </div>
    </section>
  )
}

export default WorkoutPage
