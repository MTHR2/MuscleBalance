import { useState } from 'react'
import { Link } from 'react-router-dom'
import { effectiveTarget, formatSets, heatColor, progress, type MuscleStat } from './stats'

const VISIBLE = 5

// мышцы, где подходов меньше цели (цель 0 — «не качаем» — не показываем), самые отстающие сверху
function UndertrainedList({ stats }: { stats: MuscleStat[] }) {
  const [showAll, setShowAll] = useState(false)
  const tracked = stats.filter((s) => progress(s) !== null)
  const behind = tracked
    .filter((s) => (progress(s) ?? 1) < 1)
    .sort((a, b) => (progress(a) ?? 0) - (progress(b) ?? 0))
  const shown = showAll ? behind : behind.slice(0, VISIBLE)

  return (
    <section className="card" aria-labelledby="undertrained-title">
      <div className="undertrained__head">
        <h2 id="undertrained-title" className="undertrained__title">
          Недобитые мышцы
        </h2>
        <span className="undertrained__count">
          {behind.length} из {tracked.length}
        </span>
      </div>

      {behind.length === 0 ? (
        <p className="undertrained__done">Все мышцы на этой неделе проработаны.</p>
      ) : (
        <ul className="undertrained__list">
          {shown.map((s) => {
            const ratio = progress(s) ?? 0
            return (
              <li key={s.muscleId} className="undertrained__item">
                <div className="undertrained__row">
                  <span className="undertrained__name">{s.name}</span>
                  <span className="undertrained__sets">
                    {formatSets(s.sets)} / {effectiveTarget(s)}
                  </span>
                  <Link to={`/exercises/${s.groupId}`} className="undertrained__link">
                    Упражнения
                  </Link>
                </div>
                <div className="progress" aria-hidden="true">
                  {/* минимум 2%, чтобы нулевой прогресс тоже был виден */}
                  <div
                    className="progress__bar"
                    style={{ width: `${Math.max(ratio * 100, 2)}%`, background: heatColor(ratio) }}
                  />
                </div>
              </li>
            )
          })}
        </ul>
      )}

      {behind.length > VISIBLE && (
        <button type="button" className="undertrained__more" onClick={() => setShowAll((v) => !v)}>
          {showAll ? 'Свернуть' : 'Показать все'}
        </button>
      )}
    </section>
  )
}

export default UndertrainedList
