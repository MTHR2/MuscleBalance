import { useRef, useState, type MouseEvent } from 'react'
import { BODY_VIEWS, toRegionKey } from './regions'
import {
  HEAT_STEPS,
  NEUTRAL_COLOR as NEUTRAL,
  SKIPPED_COLOR as SKIPPED,
  effectiveTarget,
  formatSets,
  heatColor,
  progress,
  type MuscleStat,
} from './stats'
import './MuscleHeatmap.css'

// тело спереди и сзади; цвет — доля выполненной недельной цели, при наведении — подходы и повторы
function MuscleHeatmap({ stats }: { stats: MuscleStat[] }) {
  const byRegion = new Map(stats.map((s) => [s.regionKey, s]))
  const [hovered, setHovered] = useState<{ stat: MuscleStat; x: number; y: number } | null>(null)
  const boxRef = useRef<HTMLDivElement>(null)

  function track(e: MouseEvent, stat: MuscleStat) {
    const box = boxRef.current?.getBoundingClientRect()
    if (box) setHovered({ stat, x: e.clientX - box.left, y: e.clientY - box.top })
  }

  function fill(regionKey: string | null) {
    const stat = regionKey ? byRegion.get(regionKey) : undefined
    if (!stat) return NEUTRAL
    const ratio = progress(stat)
    return ratio === null ? SKIPPED : heatColor(ratio)
  }

  return (
    <div className="heatmap" ref={boxRef} onMouseLeave={() => setHovered(null)}>
      <div className="heatmap__views">
        {BODY_VIEWS.map((view) => (
          <figure key={view.key} className="heatmap__view">
            <svg viewBox={view.viewBox} className="heatmap__svg" role="img" aria-label={`Мышцы ${view.label.toLowerCase()}`}>
              {view.muscles.map((m) => {
                const region = toRegionKey(m.id)
                const stat = region ? byRegion.get(region) : undefined
                const active = stat && hovered?.stat.regionKey === stat.regionKey
                return (
                  <path
                    key={m.id}
                    d={m.path}
                    fill={fill(region)}
                    className={`heatmap__muscle${stat ? ' heatmap__muscle--tracked' : ''}${active ? ' heatmap__muscle--active' : ''}`}
                    onMouseEnter={stat ? (e) => track(e, stat) : () => setHovered(null)}
                    onMouseMove={stat ? (e) => track(e, stat) : undefined}
                    onClick={stat ? (e) => track(e, stat) : undefined}
                  />
                )
              })}
            </svg>
            <figcaption className="heatmap__caption">{view.label}</figcaption>
          </figure>
        ))}
      </div>

      {hovered && <Tooltip stat={hovered.stat} x={hovered.x} y={hovered.y} />}

      <ul className="heatmap__legend" aria-label="Шкала: доля недельной цели">
        {HEAT_STEPS.map((s) => (
          <li key={s.label}>
            <span className="heatmap__swatch" style={{ background: s.color }} />
            {s.label}
          </li>
        ))}
        <li>
          <span className="heatmap__swatch" style={{ background: SKIPPED }} />
          не качаем
        </li>
      </ul>
    </div>
  )
}

function Tooltip({ stat, x, y }: { stat: MuscleStat; x: number; y: number }) {
  const target = effectiveTarget(stat)
  return (
    <div className="heatmap__tooltip" role="tooltip" style={{ left: x, top: y }}>
      <strong>{stat.name}</strong>
      {target === 0 ? (
        <span>в плане не качаем</span>
      ) : (
        <span>
          {formatSets(stat.sets)} из {target} подходов
          {stat.targetSets === null && ' (цель не задана)'}
        </span>
      )}
      <span className="heatmap__tooltip-muted">{Math.round(stat.reps)} повторений</span>
    </div>
  )
}

export default MuscleHeatmap
