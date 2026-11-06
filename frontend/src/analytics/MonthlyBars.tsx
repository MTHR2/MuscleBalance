import { useState } from 'react'
import type { MonthlyWorkouts } from '../api'
import { plural } from '../plural'
import { monthLong, monthShort } from './format'
import { niceScale, topRoundedBar } from './geometry'

const W = 560
const H = 230
const PAD_LEFT = 44
const PAD_RIGHT = 8
const PAD_TOP = 20
const PAD_BOTTOM = 30

function MonthlyBars({ months }: { months: MonthlyWorkouts[] }) {
  const [hover, setHover] = useState<number | null>(null)
  const n = months.length
  const max = Math.max(4, ...months.map((m) => Math.max(m.count, m.planned ?? 0)))
  const { hi, ticks } = niceScale(0, max, 4)
  const y = (v: number) => PAD_TOP + ((hi - v) / hi) * (H - PAD_TOP - PAD_BOTTOM)
  const slot = (W - PAD_LEFT - PAD_RIGHT) / Math.max(n, 1)
  const barWidth = slot * 0.62
  const base = y(0)
  const active = hover ?? n - 1
  const tip = months[active]
  const tipX = PAD_LEFT + active * slot + slot / 2

  return (
    <div className="chart" onMouseLeave={() => setHover(null)}>
      <svg
        viewBox={`0 0 ${W} ${H}`}
        className="chart__svg"
        role="img"
        aria-label={`Тренировки по месяцам: ${months.map((m) => `${monthShort(m.month)} ${m.count}`).join(', ')}`}
      >
        {ticks.map((t) => (
          <g key={t}>
            <line x1={4} x2={W - 4} y1={y(t)} y2={y(t)} className="chart__grid" />
            {t > 0 && (
              <text x={4} y={y(t) - 5} className="chart__tick">
                {t} трен.
              </text>
            )}
          </g>
        ))}
        {months.map((m, i) => {
          const x = PAD_LEFT + i * slot + (slot - barWidth) / 2
          const current = i === n - 1
          return (
            <g key={m.month}>
              {m.count > 0 && (
                <path
                  d={topRoundedBar(x, y(m.count), barWidth, base, 10)}
                  className={current ? 'chart__bar chart__bar--current' : 'chart__bar'}
                />
              )}
              {m.planned !== null && m.planned > 0 && (
                <line x1={x - 4} x2={x + barWidth + 4} y1={y(m.planned)} y2={y(m.planned)} className="chart__goal" />
              )}
              <text
                x={x + barWidth / 2}
                y={H - 8}
                textAnchor="middle"
                className={i === active ? 'chart__axis chart__axis--active' : 'chart__axis'}
              >
                {monthShort(m.month)}
              </text>
              <rect
                x={PAD_LEFT + i * slot}
                y={PAD_TOP}
                width={slot}
                height={H - PAD_TOP - PAD_BOTTOM}
                fill="transparent"
                onMouseEnter={() => setHover(i)}
                onClick={() => setHover(i)}
              />
            </g>
          )
        })}
      </svg>
      {tip && (
        <div
          role="tooltip"
          className="chart__tip"
          style={{
            left: `${(tipX / W) * 100}%`,
            top: `${(y(tip.count) / H) * 100}%`,
            transform: `translate(${active > n - 3 ? '-90%' : active < 2 ? '-10%' : '-50%'}, calc(-100% - 10px))`,
          }}
        >
          <strong>
            {tip.count} {plural(tip.count, ['тренировка', 'тренировки', 'тренировок'])}
          </strong>
          <span>
            {monthLong(tip.month)}
            {active === n - 1 ? ' · месяц идёт' : tip.planned !== null ? ` · план ${tip.planned}` : ''}
          </span>
        </div>
      )}
    </div>
  )
}

export default MonthlyBars
