import { useState } from 'react'
import { niceScale, smoothPath } from './geometry'
import { num } from './format'

export type LinePoint = {
  value: number
  label: string
  tipTitle: string
  tipSub: string
}

type Props = {
  points: LinePoint[]
  unit: string
  ariaLabel: string
  height?: number
  goal?: number | null
  goalLabel?: string
  labelEvery?: number
  showLastValue?: boolean
  tipOnHoverOnly?: boolean
}

const W = 680
const PAD_LEFT = 56
const PAD_TOP = 20
const PAD_BOTTOM = 32

function LineChart({
  points,
  unit,
  ariaLabel,
  height = 260,
  goal = null,
  goalLabel,
  labelEvery = 1,
  showLastValue,
  tipOnHoverOnly,
}: Props) {
  const [hover, setHover] = useState<number | null>(null)
  const padRight = goal !== null ? 64 : showLastValue ? 48 : 16
  const values = points.map((p) => p.value)
  const domain = goal !== null ? [...values, goal] : values
  const { lo, hi, ticks } = niceScale(Math.min(...domain), Math.max(...domain))
  const n = points.length
  const x = (i: number) => (n === 1 ? (PAD_LEFT + W - padRight) / 2 : PAD_LEFT + (i * (W - PAD_LEFT - padRight)) / (n - 1))
  const y = (v: number) => PAD_TOP + ((hi - v) / (hi - lo || 1)) * (height - PAD_TOP - PAD_BOTTOM)
  const path = smoothPath(
    points.map((p, i) => [x(i), y(p.value)]),
    n > 30 ? 4 : 10,
  )
  const half = n > 1 ? (W - PAD_LEFT - padRight) / (n - 1) / 2 : (W - PAD_LEFT - padRight) / 2
  const active = hover ?? n - 1
  const tip = points[active]
  const shift = active > n - 3 ? '-90%' : active < 2 ? '-10%' : '-50%'
  const last = points[n - 1]

  return (
    <div className="chart" onMouseLeave={() => setHover(null)}>
      <svg viewBox={`0 0 ${W} ${height}`} className="chart__svg" role="img" aria-label={ariaLabel}>
        {ticks.map((t) => (
          <g key={t}>
            <line x1={4} x2={W - 4} y1={y(t)} y2={y(t)} className="chart__grid" />
            <text x={4} y={y(t) - 5} className="chart__tick">
              {num(t)} {unit}
            </text>
          </g>
        ))}
        {points.map((p, i) =>
          i % labelEvery === labelEvery - 1 || n <= labelEvery ? (
            <text key={i} x={x(i)} y={height - 8} textAnchor="middle" className="chart__axis">
              {p.label}
            </text>
          ) : null,
        )}
        {goal !== null && (
          <g>
            <line x1={4} x2={W - padRight + 4} y1={y(goal)} y2={y(goal)} className="chart__goal" />
            <text x={W - padRight + 10} y={y(goal) + 4} className="chart__goal-label">
              {goalLabel}
            </text>
          </g>
        )}
        {hover !== null && <line x1={x(active)} x2={x(active)} y1={PAD_TOP} y2={height - PAD_BOTTOM} className="chart__cursor" />}
        <path d={path} className="chart__line" />
        {n === 1 && <circle cx={x(0)} cy={y(points[0].value)} r={5} className="chart__single" />}
        {showLastValue && last && (
          <text x={x(n - 1) + 8} y={y(last.value) - 8} className="chart__last">
            {num(last.value)}
          </text>
        )}
        {points.map((_, i) => (
          <rect
            key={i}
            x={x(i) - half}
            y={PAD_TOP}
            width={half * 2}
            height={height - PAD_TOP - PAD_BOTTOM}
            fill="transparent"
            onMouseEnter={() => setHover(i)}
            onClick={() => setHover(i)}
          />
        ))}
      </svg>
      {tip && (hover !== null || !tipOnHoverOnly) && (
        <div
          role="tooltip"
          className="chart__tip"
          style={{
            left: `${(x(active) / W) * 100}%`,
            top: `${(y(tip.value) / height) * 100}%`,
            transform: `translate(${shift}, calc(-100% - 14px))`,
          }}
        >
          <strong>{tip.tipTitle}</strong>
          <span>{tip.tipSub}</span>
        </div>
      )}
    </div>
  )
}

export default LineChart
