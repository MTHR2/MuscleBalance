import { useRef, useState, type MouseEvent } from 'react'
import { PICTURE_REGIONS, PICTURE_SIZE, PICTURE_VIEWBOX } from './pictureRegions'
import { effectiveTarget, formatSets, heatColor, progress, type MuscleStat } from './stats'
import './MuscleHeatmap.css'

const SKIPPED = '#9a9a9a' // мышцу не качаем — оставляем серой

// ПРОТОТИП, сейчас нигде не подключён (на главной схема MuscleHeatmap); оставлен, чтобы вернуться позже.
// тепловая карта поверх рисунка: картинка обесцвечена, обведённые мышцы тонируются (mix-blend-mode: color),
// поэтому рельеф и волокна рисунка остаются видны
function PictureHeatmap({ stats }: { stats: MuscleStat[] }) {
  const byRegion = new Map(stats.map((s) => [s.regionKey, s]))
  const [hovered, setHovered] = useState<{ stat: MuscleStat; x: number; y: number } | null>(null)
  const boxRef = useRef<HTMLDivElement>(null)

  function track(e: MouseEvent, stat: MuscleStat) {
    const box = boxRef.current?.getBoundingClientRect()
    if (box) setHovered({ stat, x: e.clientX - box.left, y: e.clientY - box.top })
  }

  const regions = PICTURE_REGIONS.map((r, i) => ({ ...r, i, stat: byRegion.get(r.regionKey) }))

  return (
    <div className="heatmap" ref={boxRef} onMouseLeave={() => setHovered(null)}>
      <svg viewBox={PICTURE_VIEWBOX} className="picture-heatmap__svg" role="img" aria-label="Мышцы спереди и сзади">
        <defs>
          <filter id="ph-gray">
            <feColorMatrix type="saturate" values="0" />
            <feComponentTransfer>
              {/* чуть светлее и мягче, как на карточках групп мышц */}
              {/* slope + intercept = 1: белый фон остаётся белым */}
              <feFuncR type="linear" slope="0.85" intercept="0.15" />
              <feFuncG type="linear" slope="0.85" intercept="0.15" />
              <feFuncB type="linear" slope="0.85" intercept="0.15" />
            </feComponentTransfer>
          </filter>
          <filter id="ph-soft" x="-5%" y="-5%" width="110%" height="110%">
            <feGaussianBlur stdDeviation="2.5" />
          </filter>
        </defs>

        <image href="/body.webp" width={PICTURE_SIZE.width} height={PICTURE_SIZE.height} filter="url(#ph-gray)" />

        {/* цвет: накладывается на серую картинку, края размыты, чтобы не было видно ломаных контуров */}
        <g className="picture-heatmap__tint" filter="url(#ph-soft)">
          {regions.map(({ d, i, stat }) => {
            if (!stat) return null
            const ratio = progress(stat)
            return <path key={i} d={d} fill={ratio === null ? SKIPPED : heatColor(ratio)} />
          })}
        </g>

        {/* невидимый слой для наведения и обводка активной мышцы */}
        <g>
          {regions.map(({ d, i, stat }) =>
            stat ? (
              <path
                key={i}
                d={d}
                className={`picture-heatmap__hit${hovered?.stat.regionKey === stat.regionKey ? ' picture-heatmap__hit--active' : ''}`}
                onMouseEnter={(e) => track(e, stat)}
                onMouseMove={(e) => track(e, stat)}
                onClick={(e) => track(e, stat)}
              />
            ) : null,
          )}
        </g>
      </svg>

      {hovered && (
        <div className="heatmap__tooltip" style={{ left: hovered.x, top: hovered.y }}>
          <strong>{hovered.stat.name}</strong>
          {effectiveTarget(hovered.stat) === 0 ? (
            <span>в плане не качаем</span>
          ) : (
            <span>
              Подходы: {formatSets(hovered.stat.sets)} из {effectiveTarget(hovered.stat)}
              {hovered.stat.targetSets === null && ' (по умолчанию)'}
            </span>
          )}
          <span>Повторы: {hovered.stat.reps}</span>
        </div>
      )}

      <div className="heatmap__legend" aria-hidden="true">
        <span>0</span>
        <span className="heatmap__scale" />
        <span>цель</span>
      </div>
    </div>
  )
}

export default PictureHeatmap
