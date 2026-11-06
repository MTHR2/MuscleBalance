const MONTHS_SHORT = ['янв', 'фев', 'мар', 'апр', 'мая', 'июн', 'июл', 'авг', 'сен', 'окт', 'ноя', 'дек']
const MONTHS_GENITIVE = [
  'января',
  'февраля',
  'марта',
  'апреля',
  'мая',
  'июня',
  'июля',
  'августа',
  'сентября',
  'октября',
  'ноября',
  'декабря',
]
const MONTHS_STANDALONE_SHORT = ['янв', 'фев', 'мар', 'апр', 'май', 'июн', 'июл', 'авг', 'сен', 'окт', 'ноя', 'дек']
const MONTHS_STANDALONE = [
  'Январь',
  'Февраль',
  'Март',
  'Апрель',
  'Май',
  'Июнь',
  'Июль',
  'Август',
  'Сентябрь',
  'Октябрь',
  'Ноябрь',
  'Декабрь',
]

export function parseDate(iso: string) {
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y, (m ?? 1) - 1, d ?? 1)
}

export const shortDate = (iso: string) => {
  const d = parseDate(iso)
  return `${d.getDate()} ${MONTHS_SHORT[d.getMonth()]}`
}

export const longDate = (iso: string) => {
  const d = parseDate(iso)
  return `${d.getDate()} ${MONTHS_GENITIVE[d.getMonth()]}`
}

export const monthShort = (yearMonth: string) => MONTHS_STANDALONE_SHORT[parseDate(yearMonth).getMonth()]

export const monthLong = (yearMonth: string) => {
  const d = parseDate(yearMonth)
  return `${MONTHS_STANDALONE[d.getMonth()]} ${d.getFullYear()}`
}

export function num(v: number, digits = 1) {
  const factor = 10 ** digits
  return String(Math.round(v * factor) / factor).replace('.', ',')
}

export function signed(v: number, digits = 1) {
  if (v === 0) return '0'
  return (v > 0 ? '+' : '−') + num(Math.abs(v), digits)
}

export function pluralDecimal(n: number, forms: [string, string, string]) {
  if (!Number.isInteger(Math.round(n * 10) / 10)) return forms[1]
  const k = Math.round(n)
  const mod10 = k % 10
  const mod100 = k % 100
  if (mod10 === 1 && mod100 !== 11) return forms[0]
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return forms[1]
  return forms[2]
}

export function todayIso() {
  const d = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
