export type Point = [number, number]

export function smoothPath(points: Point[], radius: number) {
  if (points.length === 0) return ''
  const f = (n: number) => n.toFixed(1)
  let d = `M${f(points[0][0])},${f(points[0][1])}`
  for (let i = 1; i < points.length - 1; i++) {
    const [px, py] = points[i - 1]
    const [cx, cy] = points[i]
    const [nx, ny] = points[i + 1]
    const l1 = Math.hypot(cx - px, cy - py) || 1
    const l2 = Math.hypot(nx - cx, ny - cy) || 1
    const k1 = Math.min(radius, l1 / 2) / l1
    const k2 = Math.min(radius, l2 / 2) / l2
    d += ` L${f(cx - (cx - px) * k1)},${f(cy - (cy - py) * k1)}`
    d += ` Q${f(cx)},${f(cy)} ${f(cx + (nx - cx) * k2)},${f(cy + (ny - cy) * k2)}`
  }
  if (points.length > 1) {
    const last = points[points.length - 1]
    d += ` L${f(last[0])},${f(last[1])}`
  }
  return d
}

const STEPS = [0.5, 1, 2, 2.5, 5, 10, 20, 25, 50, 100]

export function niceScale(min: number, max: number, maxTicks = 5) {
  if (!Number.isFinite(min) || !Number.isFinite(max)) return { lo: 0, hi: 1, step: 1, ticks: [0, 1] }
  if (min === max) {
    min -= 1
    max += 1
  }
  const step = STEPS.find((s) => (max - min) / s <= maxTicks - 1) ?? 100
  const lo = Math.floor(min / step) * step
  const hi = Math.ceil(max / step) * step
  const ticks: number[] = []
  for (let v = lo; v <= hi + step / 2; v += step) ticks.push(Math.round(v * 100) / 100)
  return { lo, hi, step, ticks }
}

export function topRoundedBar(x: number, y: number, width: number, base: number, radius: number) {
  const r = Math.max(0, Math.min(radius, width / 2, (base - y) / 2))
  return `M${x},${base} V${y + r} Q${x},${y} ${x + r},${y} H${x + width - r} Q${x + width},${y} ${x + width},${y + r} V${base} Z`
}
