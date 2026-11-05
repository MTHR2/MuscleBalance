import { useEffect, useState } from 'react'
import { getWeekSummary, type WeekSummary } from '../api'
import MuscleHeatmap from '../heatmap/MuscleHeatmap'
import UndertrainedList from '../heatmap/UndertrainedList'
import {
  DEFAULT_TARGET_SETS,
  currentWeekLabel,
  getMuscleStats,
  weekStartIso,
  type MuscleStat,
} from '../heatmap/stats'
import { ChevronIcon } from '../icons'
import TodayCard from '../home/TodayCard'
import { plural } from '../plural'
import './HomePage.css'

function HomePage() {
  const [stats, setStats] = useState<MuscleStat[] | null>(null)
  const [summary, setSummary] = useState<WeekSummary | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [weekOffset, setWeekOffset] = useState(0) // 0 — текущая неделя, -1 — прошлая
  const week = weekStartIso(weekOffset)

  useEffect(() => {
    const { iso } = weekStartIso(weekOffset)
    setError(null)
    getMuscleStats(iso)
      .then(setStats)
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))
    getWeekSummary(iso)
      .then(setSummary)
      .catch(() => setSummary(null))
  }, [weekOffset])

  // у всех мышц нет цели — нет активного плана или в нём не задана общая цель
  const noTargets = stats !== null && stats.every((s) => s.targetSets === null)

  return (
    <main className="page home">
      <section className="card home__map" aria-labelledby="week-title">
        <div className="home__map-head">
          <h1 id="week-title" className="home__title">
            Мышцы за неделю
          </h1>
          <div className="week-nav" role="group" aria-label="Неделя">
            <button
              type="button"
              className="round-button week-nav__btn"
              onClick={() => setWeekOffset((o) => o - 1)}
              aria-label="Предыдущая неделя"
            >
              <span className="week-nav__arrow week-nav__arrow--prev">
                <ChevronIcon size={16} />
              </span>
            </button>
            <span className="home__week" aria-live="polite">
              {weekOffset === 0 ? 'Эта неделя' : currentWeekLabel(week.date)}
            </span>
            <button
              type="button"
              className="round-button week-nav__btn"
              onClick={() => setWeekOffset((o) => o + 1)}
              disabled={weekOffset >= 0}
              aria-label="Следующая неделя"
            >
              <span className="week-nav__arrow week-nav__arrow--next">
                <ChevronIcon size={16} />
              </span>
            </button>
          </div>
        </div>
        <p className="page-subtitle">
          Цвет — сколько подходов из недельной цели плана уже сделано. Наведите на мышцу, чтобы увидеть цифры.
        </p>
        {noTargets && (
          <p className="notice home__demo">
            В активном плане нет целей — мышцы окрашены по цели {DEFAULT_TARGET_SETS} подходов в неделю.
          </p>
        )}
        {error && <p className="error">Не удалось загрузить статистику: {error}</p>}
        {stats === null && !error && <p className="muted">Загрузка…</p>}
        {stats && <MuscleHeatmap stats={stats} />}
      </section>

      <aside className="home__side">
        <TodayCard />
        {summary && <WeekTiles summary={summary} />}
        {stats && <UndertrainedList stats={stats} />}
      </aside>
    </main>
  )
}

// итоги недели: GET /api/v1/stats/week
function WeekTiles({ summary }: { summary: WeekSummary }) {
  return (
    <section className="home__tiles" aria-label="Итоги недели">
      {/* сделано может быть больше, чем в плане: свободные тренировки тоже считаются */}
      <div className="tile">
        <span className="tile__value">{summary.workoutsDone}</span>
        <span className="tile__label">
          {plural(summary.workoutsDone, ['тренировка', 'тренировки', 'тренировок'])}
          {summary.workoutsPlanned !== null && ` · в плане ${summary.workoutsPlanned}`}
        </span>
      </div>
      <div className="tile">
        <span className="tile__value">{summary.workingSets}</span>
        <span className="tile__label">{plural(summary.workingSets, ['подход', 'подхода', 'подходов'])}</span>
      </div>
      <div className="tile">
        <span className="tile__value">{summary.targetPercent !== null ? `${summary.targetPercent}%` : '—'}</span>
        <span className="tile__label">{summary.targetPercent !== null ? 'цели недели' : 'целей нет'}</span>
      </div>
    </section>
  )
}

export default HomePage
