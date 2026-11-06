import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  getBodyWeightChart,
  getExercises,
  getMonthlyWorkouts,
  getMuscleGroupStats,
  getPersonalRecords,
  getStatsExercises,
  getStatsOverview,
  type BodyWeightChart,
  type MonthlyWorkouts,
  type MuscleGroupStats,
  type PersonalRecords,
  type StatsOverview,
  type StatsPeriod,
} from '../api'
import BodyWeightCard from '../analytics/BodyWeightCard'
import Donut from '../analytics/Donut'
import { GROUP_COLORS } from '../analytics/colors'
import type { PickerExercise } from '../analytics/ExercisePicker'
import MonthlyBars from '../analytics/MonthlyBars'
import ProgressCard from '../analytics/ProgressCard'
import { longDate, num, pluralDecimal, signed } from '../analytics/format'
import { plural } from '../plural'
import './AnalyticsPage.css'

const PERIODS: { value: StatsPeriod; label: string; weeks: string }[] = [
  { value: 'MONTH', label: 'Месяц', weeks: 'за месяц' },
  { value: 'QUARTER', label: '3 месяца', weeks: 'за 3 месяца' },
  { value: 'YEAR', label: 'Год', weeks: 'за год' },
]

const errorText = (e: unknown) => (e instanceof Error ? e.message : String(e))

function AnalyticsPage() {
  const [period, setPeriod] = useState<StatsPeriod>('QUARTER')
  const [overview, setOverview] = useState<StatsOverview | null>(null)
  const [groups, setGroups] = useState<MuscleGroupStats[] | null>(null)
  const [monthly, setMonthly] = useState<MonthlyWorkouts[] | null>(null)
  const [exercises, setExercises] = useState<PickerExercise[] | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [records, setRecords] = useState<PersonalRecords | null>(null)
  const [bodyWeight, setBodyWeight] = useState<BodyWeightChart | null>(null)
  const [errors, setErrors] = useState<Record<string, string>>({})

  const fail = (key: string) => (e: unknown) => setErrors((prev) => ({ ...prev, [key]: errorText(e) }))

  const loadBodyWeight = useCallback(() => {
    getBodyWeightChart(period)
      .then(setBodyWeight)
      .catch((e) => setErrors((prev) => ({ ...prev, bodyWeight: errorText(e) })))
  }, [period])

  useEffect(() => {
    getStatsOverview(period).then(setOverview).catch(fail('overview'))
    getMuscleGroupStats(period).then(setGroups).catch(fail('groups'))
    getPersonalRecords(period).then(setRecords).catch(fail('records'))
    Promise.all([getStatsExercises(period), getExercises()])
      .then(([withSets, catalog]) => {
        const known = new Set(withSets.map((e) => e.id))
        const rest = catalog
          .filter((e) => !known.has(e.id))
          .sort((a, b) => a.name.localeCompare(b.name, 'ru'))
          .map((e) => ({
            id: e.id,
            name: e.name,
            imageUrl: e.imageUrl,
            loadType: e.loadType,
            workingWeight: e.workingWeight ?? null,
            hasData: false,
          }))
        const list = [...withSets.map((e) => ({ ...e, hasData: true })), ...rest]
        setExercises(list)
        setSelectedId((id) => (id !== null && list.some((e) => e.id === id) ? id : (list[0]?.id ?? null)))
      })
      .catch(fail('exercises'))
    loadBodyWeight()
  }, [period, loadBodyWeight])

  useEffect(() => {
    getMonthlyWorkouts(12).then(setMonthly).catch(fail('monthly'))
  }, [])

  const periodInfo = PERIODS.find((p) => p.value === period)!
  const slices =
    groups?.map((g, i) => ({ id: g.groupId, name: g.name, value: g.sets, color: GROUP_COLORS[i % GROUP_COLORS.length] })) ?? []
  const totalSets = groups?.reduce((sum, g) => sum + g.sets, 0) ?? 0
  const withTargets = groups?.filter((g) => g.targetSets > 0) ?? []
  const lagging = [...withTargets]
    .map((g) => ({ ...g, ratio: g.sets / g.targetSets }))
    .filter((g) => g.ratio < 0.7)
    .sort((a, b) => a.ratio - b.ratio)
    .slice(0, 2)
  const firstActive = monthly?.findIndex((m) => m.count > 0) ?? -1
  const finishedMonths = monthly && firstActive >= 0 ? monthly.slice(firstActive, -1) : []
  const avgMonthly =
    finishedMonths.length > 0 ? finishedMonths.reduce((sum, m) => sum + m.count, 0) / finishedMonths.length : null

  return (
    <main className="page analytics">
      <div className="page-head">
        <div>
          <h1 className="page-title">Аналитика</h1>
          <p className="page-subtitle">Как растут веса, насколько вы регулярны и что происходит с весом тела</p>
        </div>
        <div className="segmented" role="group" aria-label="Период">
          {PERIODS.map((p) => (
            <button
              key={p.value}
              type="button"
              aria-pressed={p.value === period}
              className={p.value === period ? 'segmented__item segmented__item--active' : 'segmented__item'}
              onClick={() => {
                setErrors({})
                setPeriod(p.value)
              }}
            >
              {p.label}
            </button>
          ))}
        </div>
      </div>

      <section className="an-kpis" aria-label="Главное">
        {errors.overview && <p className="error">Не удалось загрузить сводку: {errors.overview}</p>}
        {overview && (
          <>
            <div className="an-kpi">
              <span className="an-kpi__label">Тренировок в этом месяце</span>
              <span className="an-kpi__value">
                {overview.workoutsThisMonth}
                {overview.workoutsPlannedThisMonth !== null && (
                  <span className="an-kpi__unit"> из {overview.workoutsPlannedThisMonth}</span>
                )}
              </span>
              <span className="an-kpi__sub">
                {overview.workoutsPlannedThisMonth !== null ? 'по плану' : 'нет активного плана'}
              </span>
            </div>
            <div className="an-kpi">
              <span className="an-kpi__label">Серия</span>
              <span className="an-kpi__value">
                {overview.weekStreak ?? '—'}
                {overview.weekStreak !== null && (
                  <span className="an-kpi__unit"> {plural(overview.weekStreak, ['неделя', 'недели', 'недель'])}</span>
                )}
              </span>
              <span className="an-kpi__sub">
                {overview.weekStreak !== null ? 'подряд без пропусков плана' : 'нужен активный план'}
              </span>
            </div>
            <div className="an-kpi">
              <span className="an-kpi__label">Объём за 30 дней</span>
              <span className="an-kpi__value">
                {num(overview.volumeTonnes30d)}
                <span className="an-kpi__unit"> т</span>
              </span>
              <span className="an-kpi__sub">
                {overview.volumeChangePercent !== null
                  ? `${signed(overview.volumeChangePercent, 0)}% к прошлым 30 дням`
                  : 'не с чем сравнить'}
              </span>
            </div>
            <div className="an-kpi">
              <span className="an-kpi__label">Средняя тренировка</span>
              <span className="an-kpi__value">
                {overview.avgDurationSeconds !== null ? Math.round(overview.avgDurationSeconds / 60) : '—'}
                {overview.avgDurationSeconds !== null && <span className="an-kpi__unit"> мин</span>}
              </span>
              <span className="an-kpi__sub">
                {overview.avgSets !== null
                  ? `${num(overview.avgSets)} ${pluralDecimal(overview.avgSets, ['рабочий подход', 'рабочих подхода', 'рабочих подходов'])}`
                  : 'тренировок пока нет'}
              </span>
            </div>
          </>
        )}
      </section>

      <div className="an-row an-row--main">
        <ProgressCard period={period} exercises={exercises} selectedId={selectedId} onSelect={setSelectedId} />

        <section className="card an-card an-donut" aria-labelledby="donut-title">
          <div>
            <h2 id="donut-title" className="an-card__title">
              Нагрузка по мышцам
            </h2>
            <p className="an-card__sub">
              {num(totalSets)} {pluralDecimal(totalSets, ['рабочий подход', 'рабочих подхода', 'рабочих подходов'])}{' '}
              {periodInfo.weeks}
            </p>
          </div>
          {errors.groups && <p className="error">{errors.groups}</p>}
          {groups && <Donut slices={slices} />}
        </section>
      </div>

      <div className="an-row">
        <section className="card an-card" aria-labelledby="monthly-title">
          <div className="an-card__head">
            <h2 id="monthly-title" className="an-card__title">
              Тренировки по месяцам
            </h2>
            {avgMonthly !== null && <span className="an-card__note">в среднем {num(avgMonthly)} в месяц</span>}
          </div>
          {errors.monthly && <p className="error">{errors.monthly}</p>}
          {monthly && <MonthlyBars months={monthly} />}
          <div className="an-legend">
            <span>
              <i className="an-legend__box" />
              проведено
            </span>
            <span>
              <i className="an-legend__box an-legend__box--soft" />
              текущий месяц
            </span>
            <span>
              <i className="an-legend__dash" />
              план
            </span>
          </div>
        </section>

        <BodyWeightCard chart={bodyWeight} error={errors.bodyWeight ?? null} onChanged={loadBodyWeight} />
      </div>

      <div className="an-row">
        <section className="card an-card" aria-labelledby="goals-title">
          <div>
            <h2 id="goals-title" className="an-card__title">
              Выполнение цели по группам
            </h2>
            <p className="an-card__sub">Рабочие подходы {periodInfo.weeks} против цели плана</p>
          </div>
          {groups && withTargets.length === 0 && (
            <p className="muted">В активном плане нет целей по мышцам.</p>
          )}
          <ul className="an-goals">
            {withTargets.map((g) => {
              const ratio = g.sets / g.targetSets
              return (
                <li key={g.groupId} className="an-goal">
                  <span className="an-goal__name">{g.name}</span>
                  <div className="progress an-goal__bar">
                    <div
                      className="progress__bar"
                      style={{
                        width: `${Math.max(2, Math.min(100, ratio * 100))}%`,
                        background: ratio < 0.7 ? 'var(--data-soft)' : 'var(--data)',
                      }}
                    />
                  </div>
                  <span className="an-goal__value">
                    {num(g.sets)} / {g.targetSets}
                  </span>
                </li>
              )
            })}
          </ul>
          {lagging.length > 0 && (
            <p className="an-hint">
              Меньше всего — {lagging.map((g) => `${g.name.toLowerCase()} (${Math.round(g.ratio * 100)}%)`).join(' и ')}.{' '}
              <Link to="/exercises">Подобрать упражнения</Link>
            </p>
          )}
        </section>

        <section className="card an-card" aria-labelledby="records-title">
          <div className="an-card__head">
            <h2 id="records-title" className="an-card__title">
              Личные рекорды
            </h2>
            {records && <span className="an-card__note">{records.totalInPeriod} {periodInfo.weeks}</span>}
          </div>
          {errors.records && <p className="error">{errors.records}</p>}
          {records && records.records.length === 0 && <p className="muted">За этот период новых рекордов нет.</p>}
          <ol className="an-records">
            {records?.records.map((r) => (
              <li key={`${r.exerciseId}-${r.date}`} className="an-record">
                <span className="an-record__icon" aria-hidden="true">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0z" />
                    <path d="M17 5h3v2a3 3 0 0 1-3 3M7 5H4v2a3 3 0 0 0 3 3" />
                  </svg>
                </span>
                <span className="an-record__main">
                  <span className="an-record__name">{r.name}</span>
                  <span className="an-record__date">{longDate(r.date)}</span>
                </span>
                <span className="an-record__value">
                  <strong>
                    {r.loadType === 'bodyweight'
                      ? `${r.reps} ${plural(r.reps, ['повтор', 'повтора', 'повторов'])}`
                      : `${num(r.weight)} кг × ${r.reps}`}
                  </strong>
                  {r.delta !== null && (
                    <span>
                      {signed(r.delta)} {r.loadType === 'bodyweight' ? 'повт.' : 'кг'}
                    </span>
                  )}
                </span>
              </li>
            ))}
          </ol>
        </section>
      </div>
    </main>
  )
}

export default AnalyticsPage
