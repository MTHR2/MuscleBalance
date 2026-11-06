import { useState } from 'react'
import { num } from './format'

export type DonutSlice = { id: number; name: string; value: number; color: string }

const W = 330
const H = 300
const CX = W / 2
const CY = H / 2
const R = 70
const STROKE = 26
const OVERLAP = 3
const LABEL_R = R + STROKE / 2 + 20

function Donut({ slices }: { slices: DonutSlice[] }) {
  const [hover, setHover] = useState<number | null>(null)
  const visible = slices.filter((s) => s.value > 0)
  const total = visible.reduce((sum, s) => sum + s.value, 0)
  const circumference = 2 * Math.PI * R
  const lengths = visible.map((s) => (s.value / total) * circumference)
  const starts = lengths.map((_, i) => lengths.slice(0, i).reduce((sum, l) => sum + l, 0))
  const segments = visible.map((s, i) => {
    const length = lengths[i]
    const start = starts[i]
    const dash = Math.max(0.1, length - STROKE + OVERLAP)
    const offset = -(start + (STROKE - OVERLAP) / 2)
    const mid = ((start + length / 2) / circumference) * 2 * Math.PI - Math.PI / 2
    const cos = Math.cos(mid)
    const side = Math.abs(cos) < 0.3 ? 'center' : cos > 0 ? 'right' : 'left'
    return {
      ...s,
      dash,
      offset,
      percent: Math.round((s.value / total) * 100),
      lx: CX + cos * LABEL_R,
      ly: CY + Math.sin(mid) * LABEL_R,
      side,
    }
  })
  const active = segments.find((s) => s.id === hover)

  if (total === 0) return <p className="muted">За этот период рабочих подходов нет.</p>

  return (
    <div className="donut" onMouseLeave={() => setHover(null)}>
      <svg
        viewBox={`0 0 ${W} ${H}`}
        className="donut__svg"
        role="img"
        aria-label={`Нагрузка по мышцам: ${segments.map((s) => `${s.name} ${s.percent}%`).join(', ')}`}
      >
        {segments.map((s) => (
          <circle
            key={s.id}
            cx={CX}
            cy={CY}
            r={R}
            fill="none"
            stroke={s.color}
            strokeWidth={hover === s.id ? STROKE + 6 : STROKE}
            strokeLinecap="round"
            strokeDasharray={`${s.dash} ${circumference}`}
            strokeDashoffset={s.offset}
            transform={`rotate(-90 ${CX} ${CY})`}
            onMouseEnter={() => setHover(s.id)}
            onClick={() => setHover(s.id)}
            className="donut__segment"
          />
        ))}
      </svg>
      <div className="donut__center">
        {active ? (
          <>
            <strong>{num(active.value)}</strong>
            <span>подх.</span>
          </>
        ) : (
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" aria-hidden="true">
            <path d="M7 7v10M17 7v10M4 9.5v5M20 9.5v5M7 12h10" />
          </svg>
        )}
      </div>
      {segments.map((s) => (
        <div
          key={s.id}
          className={`donut__label donut__label--${s.side}`}
          style={{ left: `${(s.lx / W) * 100}%`, top: `${(s.ly / H) * 100}%` }}
        >
          <span className="donut__percent">
            <span className="donut__dot" style={{ background: s.color }} />
            {s.percent}%
          </span>
          <span className={hover === s.id ? 'donut__name donut__name--active' : 'donut__name'}>{s.name}</span>
        </div>
      ))}
    </div>
  )
}

export default Donut
