import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  activatePlan,
  deletePlan,
  getPlan,
  getPlans,
  getWorkoutTemplate,
  type PlanDetails,
  type PlanListItem,
  type WorkoutTemplateDetails,
} from '../api'
import { DEFAULT_TARGET_SETS } from '../heatmap/stats'
import { plural } from '../plural'
import { WEEKDAYS, daysWord } from '../weekdays'
import './PlansPage.css'

// 1 = Пн ... 7 = Вс
const todayDayOfWeek = () => ((new Date().getDay() + 6) % 7) + 1

// планы: вкладки сверху (активный первым), ниже неделя выбранного плана
function PlansPage() {
  const navigate = useNavigate()
  const params = useParams()
  const [plans, setPlans] = useState<PlanListItem[] | null>(null)
  const [plan, setPlan] = useState<PlanDetails | null>(null)
  const [templates, setTemplates] = useState<Map<number, WorkoutTemplateDetails>>(new Map())
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadPlans = useCallback(
    () =>
      getPlans()
        .then(setPlans)
        .catch((e) => setError(e instanceof Error ? e.message : String(e))),
    [],
  )

  useEffect(() => {
    loadPlans()
  }, [loadPlans])

  // выбранный: из адреса, иначе активный (с бэка он приходит первым)
  const selectedId = params.planId ? Number(params.planId) : (plans?.[0]?.id ?? null)

  useEffect(() => {
    if (selectedId === null) return
    setPlan(null)
    getPlan(selectedId)
      .then(async (p) => {
        setPlan(p)
        // число упражнений и подходов на карточке дня — из шаблонов тренировок
        const ids = [...new Set(p.days.map((d) => d.workoutTemplateId))]
        const list = await Promise.all(ids.map(getWorkoutTemplate))
        setTemplates(new Map(list.map((t) => [t.id, t])))
      })
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))
  }, [selectedId])

  async function run(action: () => Promise<void>) {
    setBusy(true)
    setError(null)
    try {
      await action()
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setBusy(false)
    }
  }

  const activate = (p: PlanDetails) =>
    run(async () => {
      setPlan(await activatePlan(p.id))
      await loadPlans()
    })

  const remove = (p: PlanDetails) => {
    const warning = p.active
      ? `«${p.name}» — активный план. После удаления цели на главной пропадут. Удалить?`
      : `Удалить план «${p.name}»? Проведённые тренировки останутся в истории.`
    if (!window.confirm(warning)) return
    run(async () => {
      await deletePlan(p.id)
      setPlan(null)
      await loadPlans()
      navigate('/plans')
    })
  }

  const trainingDays = plan?.days.length ?? 0
  const today = todayDayOfWeek()

  return (
    <main className="page">
      {plans === null && !error && <p className="muted">Загрузка…</p>}
      {error && <p className="error">Ошибка: {error}</p>}

      {plans?.length === 0 && (
        <section className="plans-empty">
          <h1 className="page-title">План на неделю</h1>
          <p className="page-subtitle">
            Планов пока нет. Составьте неделю из тренировок — по ней главная будет подсказывать, что делать сегодня.
          </p>
          <Link to="/plans/new" className="primary-button">
            Создать новый план
          </Link>
        </section>
      )}

      {plans && plans.length > 0 && (
        <>
          <div className="plan-tabs" aria-label="Мои планы">
            {plans.map((p) => (
              <Link
                key={p.id}
                to={`/plans/${p.id}`}
                className={`chip-button${p.id === selectedId ? ' chip-button--active' : ''}`}
                aria-current={p.id === selectedId ? 'page' : undefined}
              >
                {p.name}
                {p.active && <span className="plan-tabs__active">активный</span>}
              </Link>
            ))}
            <Link to="/plans/new" className="chip-button chip-button--dashed">
              + Новый план
            </Link>
          </div>

          {plan === null && <p className="muted">Загрузка…</p>}
          {plan && (
            <>
              <div className="page-head plan__head">
                <div className="plan__title-box">
                  <h1 className="page-title">{plan.name}</h1>
                  <p className="page-subtitle">
                    {trainingDays} {daysWord(trainingDays)}, {7 - trainingDays} {restWord(7 - trainingDays)}
                  </p>
                  {plan.description && <p className="plan__description">{plan.description}</p>}
                </div>
                <div className="plan__actions">
                  <Link to={`/plans/${plan.id}/edit`} className="gray-button">
                    Изменить
                  </Link>
                  {!plan.active && (
                    <button type="button" className="gray-button" onClick={() => activate(plan)} disabled={busy}>
                      Сделать активным
                    </button>
                  )}
                  <button type="button" className="outline-button" onClick={() => remove(plan)} disabled={busy}>
                    Удалить
                  </button>
                </div>
              </div>

              <ol className="week-grid" aria-label="Неделя">
                {WEEKDAYS.map((w) => {
                  const entry = plan.days.find((d) => d.dayOfWeek === w.day)
                  const isToday = plan.active && w.day === today
                  if (!entry) {
                    return (
                      <li key={w.day} className={`day day--rest${isToday ? ' day--today' : ''}`}>
                        <span className="day__name">
                          <abbr title={w.full}>{w.short}</abbr>
                          {isToday && <span className="day__today">сегодня</span>}
                        </span>
                        <span className="day__rest">Отдых</span>
                      </li>
                    )
                  }
                  const t = templates.get(entry.workoutTemplateId)
                  return (
                    <li key={w.day} className={`day${isToday ? ' day--today' : ''}`}>
                      <span className="day__name">
                        <abbr title={w.full}>{w.short}</abbr>
                        {isToday && <span className="day__today">сегодня</span>}
                      </span>
                      <strong className="day__workout">{entry.workoutTemplateName}</strong>
                      {t && (
                        <ul className="day__thumbs" aria-label="Упражнения">
                          {t.exercises.slice(0, 3).map((e) => (
                            <li key={e.exerciseId} title={e.exerciseName}>
                              {e.imageUrl ? <img src={e.imageUrl} alt={e.exerciseName} /> : e.exerciseName}
                            </li>
                          ))}
                          {t.exercises.length > 3 && <li className="day__thumb-more">+{t.exercises.length - 3}</li>}
                        </ul>
                      )}
                      {t && (
                        <span className="day__meta">
                          {t.exercises.length} упр. · {t.exercises.reduce((n, e) => n + e.targetSets, 0)} подх.
                        </span>
                      )}
                    </li>
                  )
                })}
              </ol>

              <section className="targets" aria-labelledby="targets-title">
                <div className="targets__head">
                  <h2 id="targets-title" className="targets__title">
                    Цели на мышцы
                  </h2>
                  <span className="targets__default">
                    {plan.defaultWeeklySets !== null ? (
                      <>
                        Общая цель: <strong>{plan.defaultWeeklySets}</strong>{' '}
                        {plural(plan.defaultWeeklySets, ['подход', 'подхода', 'подходов'])} на мышцу в неделю
                      </>
                    ) : (
                      <>Общая цель не задана — карта считает {DEFAULT_TARGET_SETS} подходов</>
                    )}
                  </span>
                </div>
                {plan.muscleTargets.length > 0 ? (
                  <ul className="targets__list">
                    {plan.muscleTargets.map((t) => (
                      <li key={t.muscleId} className={`targets__item${t.weeklySets === 0 ? ' targets__item--off' : ''}`}>
                        {t.muscleName} · {t.weeklySets === 0 ? 'не качаем' : t.weeklySets}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="targets__text">Отдельных целей нет — у всех мышц общая цель.</p>
                )}
              </section>
            </>
          )}
        </>
      )}
    </main>
  )
}

function restWord(n: number) {
  const mod10 = n % 10
  const mod100 = n % 100
  if (mod10 === 1 && mod100 !== 11) return 'день отдыха'
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return 'дня отдыха'
  return 'дней отдыха'
}

export default PlansPage
