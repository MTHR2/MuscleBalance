// Статистика по мышцам за неделю: GET /api/v1/stats/muscles.

export type MuscleStat = {
  muscleId: number
  name: string
  regionKey: string // совпадает с muscles.region_key
  groupId: number
  groupName: string
  sets: number // рабочие подходы за неделю; secondary = 0.5
  reps: number // тоже с коэффициентом 0.5, поэтому может быть дробным
  targetSets: number | null // цель из активного плана; null — цели нет, 0 — мышцу не качаем
}

// чем красить мышцу, если в плане нет цели (нет активного плана или общей цели)
export const DEFAULT_TARGET_SETS = 10

export const effectiveTarget = (s: MuscleStat) => s.targetSets ?? DEFAULT_TARGET_SETS

// 0..1 — доля выполненной цели; null — мышцу не качаем
export function progress(s: MuscleStat): number | null {
  const target = effectiveTarget(s)
  return target === 0 ? null : Math.min(s.sets / target, 1)
}

// подходы могут быть дробными из-за secondary = 0.5: «7,5»
export function formatSets(n: number) {
  return Number.isInteger(n) ? String(n) : n.toFixed(1).replace('.', ',')
}

// пять ступеней от красного (0) до зелёного (цель); соседние отличаются и яркостью, не только оттенком
export const HEAT_STEPS = [
  { upTo: 0.2, color: '#E2463C', label: '0–20%' },
  { upTo: 0.4, color: '#EE8A3C', label: '20–40%' },
  { upTo: 0.65, color: '#F2B544', label: '40–65%' },
  { upTo: 1, color: '#9CC25A', label: '65–99%' },
  { upTo: Infinity, color: '#3FA66B', label: 'цель' },
]
export const SKIPPED_COLOR = '#D6D6D2' // мышцу в плане не качаем
export const NEUTRAL_COLOR = '#EDEDEA' // область без мышцы из справочника

export function heatColor(ratio: number) {
  return HEAT_STEPS.find((s) => ratio < s.upTo)!.color
}

// понедельник недели со сдвигом от текущей (0 — эта, -1 — прошлая) в формате 2026-11-02 для weekStart
export function weekStartIso(offset: number) {
  const monday = new Date()
  monday.setDate(monday.getDate() - ((monday.getDay() + 6) % 7) + offset * 7)
  const pad = (n: number) => String(n).padStart(2, '0')
  return { date: monday, iso: `${monday.getFullYear()}-${pad(monday.getMonth() + 1)}-${pad(monday.getDate())}` }
}

// начало недели (понедельник) и подпись «3 — 9 ноября»
export function currentWeekLabel(now = new Date()) {
  const monday = new Date(now)
  monday.setDate(now.getDate() - ((now.getDay() + 6) % 7))
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)
  const month = (d: Date) => d.toLocaleDateString('ru-RU', { month: 'long', day: 'numeric' })
  return monday.getMonth() === sunday.getMonth()
    ? `${monday.getDate()} — ${month(sunday)}`
    : `${month(monday)} — ${month(sunday)}`
}

// статистика недели (бэк сам приводит дату к понедельнику); без weekStart — текущая неделя
export async function getMuscleStats(weekStart?: string): Promise<MuscleStat[]> {
  const res = await fetch(`/api/v1/stats/muscles${weekStart ? `?weekStart=${weekStart}` : ''}`)
  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new Error(body?.message || `${res.status} ${res.statusText}`)
  }
  return res.json()
}
