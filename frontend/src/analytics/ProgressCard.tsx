import { useEffect, useState } from 'react'
import { getExerciseProgress, type ExerciseProgress, type StatsPeriod } from '../api'
import { plural } from '../plural'
import ExercisePicker, { type PickerExercise } from './ExercisePicker'
import LineChart, { type LinePoint } from './LineChart'
import { num, shortDate, signed } from './format'

const PERIOD_LABEL: Record<StatsPeriod, string> = { MONTH: 'За месяц', QUARTER: 'За 3 месяца', YEAR: 'За год' }

type Props = {
  period: StatsPeriod
  exercises: PickerExercise[] | null
  selectedId: number | null
  onSelect: (id: number) => void
}

function ProgressCard({ period, exercises, selectedId, onSelect }: Props) {
  const [progress, setProgress] = useState<ExerciseProgress | null>(null)
  const [error, setError] = useState<string | null>(null)

  const selected = exercises?.find((e) => e.id === selectedId) ?? null
  const hasData = selected?.hasData ?? false

  useEffect(() => {
    if (selectedId === null || !hasData) return
    let alive = true
    getExerciseProgress(selectedId, period)
      .then((p) => {
        if (!alive) return
        setProgress(p)
        setError(null)
      })
      .catch((e) => alive && setError(e instanceof Error ? e.message : String(e)))
    return () => {
      alive = false
    }
  }, [selectedId, period, hasData])

  const bodyweight = progress?.loadType === 'bodyweight'
  const unit = bodyweight ? 'повт.' : 'кг'
  const points: LinePoint[] =
    progress?.points.map((p) => ({
      value: bodyweight ? p.reps : p.weight,
      label: shortDate(p.weekStart),
      tipTitle: bodyweight
        ? `${p.reps} ${plural(p.reps, ['повтор', 'повтора', 'повторов'])}${p.weight > 0 ? ` · +${num(p.weight)} кг` : ''}`
        : `${num(p.weight)} кг × ${p.reps}`,
      tipSub: `неделя ${shortDate(p.weekStart)}${p.record ? ' · новый рекорд' : ''}`,
    })) ?? []
  const current = hasData && progress && progress.exerciseId === selectedId ? progress : null

  return (
    <section className="card an-card an-progress" aria-labelledby="progress-title">
      <div className="an-card__head">
        <div>
          <h2 id="progress-title" className="an-card__title">
            Рабочий вес
          </h2>
          <p className="an-card__sub">
            {bodyweight ? 'Лучший подход недели по числу повторов' : 'Лучший рабочий подход каждой недели'}
          </p>
        </div>
        {exercises && exercises.length > 0 && (
          <ExercisePicker exercises={exercises} selectedId={selectedId} onSelect={onSelect} />
        )}
      </div>

      {exercises && exercises.length === 0 && <p className="muted">В каталоге пока нет упражнений.</p>}
      {selected && !hasData && (
        <p className="muted">По этому упражнению за выбранный период нет рабочих подходов.</p>
      )}
      {hasData && error && <p className="error">Не удалось загрузить прогресс: {error}</p>}

      {current && (
        <>
          <div className="an-stats">
            <div className="an-stat">
              <span className="an-stat__label">Сейчас</span>
              <span className="an-stat__value">{current.current !== null ? `${num(current.current)} ${unit}` : '—'}</span>
            </div>
            <div className="an-stat">
              <span className="an-stat__label">{PERIOD_LABEL[period]}</span>
              <span className="an-stat__value">
                {current.delta !== null ? `${signed(current.delta)} ${unit}` : '—'}
                {current.deltaPercent !== null && (
                  <span className="an-stat__extra"> · {signed(current.deltaPercent, 0)}%</span>
                )}
              </span>
            </div>
            {!bodyweight && (
              <div className="an-stat">
                <span className="an-stat__label">Расчётный максимум</span>
                <span className="an-stat__value">
                  {current.estimatedMax !== null ? `≈ ${num(current.estimatedMax, 0)} кг` : '—'}
                </span>
              </div>
            )}
          </div>
          {points.length > 0 ? (
            <LineChart
              points={points}
              unit={unit}
              labelEvery={points.length > 8 ? 2 : 1}
              ariaLabel={`${current.name}: рабочий вес по неделям`}
            />
          ) : (
            <p className="muted">За этот период по упражнению нет рабочих подходов.</p>
          )}
        </>
      )}
    </section>
  )
}

export default ProgressCard
