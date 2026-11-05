// Контуры мышц — из пакета body-muscles (Apache-2.0, © Ivan Vulović, https://github.com/vulovix/body-muscles).
// Там 70+ областей с делением на левую/правую сторону и пучки; здесь они сводятся к нашим region_key из БД.
import { BACK_MUSCLES, FRONT_MUSCLES, type MuscleDef } from 'body-muscles'

// порядок важен: первое совпадение по началу id
const PREFIX_TO_REGION: [string, string][] = [
  ['shoulder-front', 'front_delts'],
  ['shoulder-side', 'side_delts'],
  ['deltoid-rear', 'rear_delts'],
  ['chest', 'chest'],
  ['biceps', 'biceps'],
  ['triceps', 'triceps'],
  ['forearm', 'forearms'],
  ['abs', 'abs'],
  ['obliques', 'obliques'],
  ['traps', 'traps'],
  ['lats', 'lats'],
  ['lower-back', 'lower_back'],
  ['gluteus', 'glutes'],
  ['quads', 'quads'],
  ['adductors', 'adductors'],
  ['hamstrings', 'hamstrings'],
  ['calves', 'calves'],
]

// null — область без мышцы в нашем справочнике (голова, кисти, колени, передняя большеберцовая и т.п.)
export function toRegionKey(id: string): string | null {
  return PREFIX_TO_REGION.find(([prefix]) => id.startsWith(prefix))?.[1] ?? null
}

export type BodyView = { key: 'front' | 'back'; label: string; viewBox: string; muscles: MuscleDef[] }

// координаты взяты из пакета: спереди x 0..35, сзади x 37..72
export const BODY_VIEWS: BodyView[] = [
  { key: 'front', label: 'Спереди', viewBox: '0 0 35 93', muscles: FRONT_MUSCLES },
  { key: 'back', label: 'Сзади', viewBox: '37 0 35 93', muscles: BACK_MUSCLES },
]
