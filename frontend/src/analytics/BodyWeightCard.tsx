import { useState, type FormEvent } from 'react'
import { saveBodyWeight, updateProfile, type BodyWeightChart } from '../api'
import LineChart, { type LinePoint } from './LineChart'
import { num, shortDate, signed, todayIso } from './format'

type Props = {
  chart: BodyWeightChart | null
  error: string | null
  onChanged: () => void
}

type Mode = 'idle' | 'weight' | 'goal'

function parseKg(value: string) {
  const v = Number(value.replace(',', '.'))
  return Number.isFinite(v) && v >= 20 && v <= 500 ? Math.round(v * 100) / 100 : null
}

function BodyWeightCard({ chart, error, onChanged }: Props) {
  const [mode, setMode] = useState<Mode>('idle')
  const [value, setValue] = useState('')
  const [date, setDate] = useState(todayIso())
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  function open(next: Mode) {
    setMode(next)
    setFormError(null)
    setDate(todayIso())
    setValue(next === 'goal' && chart?.target != null ? String(chart.target) : '')
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    const kg = parseKg(value)
    if (kg === null) {
      setFormError('Введите вес от 20 до 500 кг')
      return
    }
    setSaving(true)
    setFormError(null)
    try {
      if (mode === 'weight') await saveBodyWeight({ date, weight: kg })
      else await updateProfile({ targetBodyWeight: kg })
      setMode('idle')
      onChanged()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : String(err))
    } finally {
      setSaving(false)
    }
  }

  const points: LinePoint[] =
    chart?.points.map((p) => {
      const value = p.avg7 ?? p.weight
      return {
        value,
        label: shortDate(p.date),
        tipTitle: `${num(p.weight)} кг`,
        tipSub: `${shortDate(p.date)}${p.avg7 !== null ? ` · среднее ${num(p.avg7)}` : ''}`,
      }
    }) ?? []
  const every = Math.max(1, Math.ceil(points.length / 5))

  return (
    <section className="card an-card" aria-labelledby="bw-title">
      <div className="an-card__head">
        <h2 id="bw-title" className="an-card__title">
          Вес тела
        </h2>
        <div className="an-card__actions">
          <button type="button" className="gray-button an-small-button" onClick={() => open('goal')}>
            Цель
          </button>
          <button type="button" className="accent-button an-small-button" onClick={() => open('weight')}>
            + Записать вес
          </button>
        </div>
      </div>

      {mode !== 'idle' && (
        <form className="an-form" onSubmit={submit}>
          <label className="an-form__field">
            <span>{mode === 'weight' ? 'Вес, кг' : 'Цель, кг'}</span>
            <input
              className="field__input"
              inputMode="decimal"
              value={value}
              onChange={(e) => setValue(e.target.value)}
              placeholder="78,4"
              autoFocus
            />
          </label>
          {mode === 'weight' && (
            <label className="an-form__field">
              <span>Дата</span>
              <input
                className="field__input"
                type="date"
                value={date}
                max={todayIso()}
                onChange={(e) => setDate(e.target.value)}
              />
            </label>
          )}
          <div className="an-form__buttons">
            <button type="submit" className="primary-button" disabled={saving}>
              Сохранить
            </button>
            <button type="button" className="outline-button" onClick={() => setMode('idle')}>
              Отмена
            </button>
          </div>
          {formError && <p className="error an-form__error">{formError}</p>}
        </form>
      )}

      {error && <p className="error">Не удалось загрузить вес: {error}</p>}

      {chart && (
        <>
          <div className="an-stats">
            <div className="an-stat an-stat--plain">
              <span className="an-stat__label">Сейчас</span>
              <span className="an-stat__value">{chart.current !== null ? `${num(chart.current)} кг` : '—'}</span>
            </div>
            <div className="an-stat an-stat--plain">
              <span className="an-stat__label">За 30 дней</span>
              <span className="an-stat__value">{chart.change30 !== null ? `${signed(chart.change30)} кг` : '—'}</span>
            </div>
            <div className="an-stat an-stat--plain">
              <span className="an-stat__label">До цели</span>
              <span className="an-stat__value">
                {chart.toTarget !== null ? `${num(Math.abs(chart.toTarget))} кг` : 'не задана'}
              </span>
            </div>
          </div>
          {points.length > 1 ? (
            <LineChart
              points={points}
              unit="кг"
              height={230}
              goal={chart.target}
              goalLabel={chart.target !== null ? `цель ${num(chart.target)}` : undefined}
              labelEvery={every}
              showLastValue
              tipOnHoverOnly
              ariaLabel="Вес тела, среднее за 7 дней"
            />
          ) : (
            <p className="muted">
              {points.length === 1 ? 'Нужен ещё хотя бы один замер, чтобы построить график.' : 'Замеров пока нет — запишите первый.'}
            </p>
          )}
        </>
      )}
    </section>
  )
}

export default BodyWeightCard
