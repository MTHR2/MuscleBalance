// дни недели как в БД: 1 = понедельник ... 7 = воскресенье
export const WEEKDAYS = [
  { day: 1, short: 'Пн', full: 'Понедельник' },
  { day: 2, short: 'Вт', full: 'Вторник' },
  { day: 3, short: 'Ср', full: 'Среда' },
  { day: 4, short: 'Чт', full: 'Четверг' },
  { day: 5, short: 'Пт', full: 'Пятница' },
  { day: 6, short: 'Сб', full: 'Суббота' },
  { day: 7, short: 'Вс', full: 'Воскресенье' },
]

// «день / дня / дней»
export function daysWord(n: number) {
  const mod10 = n % 10
  const mod100 = n % 100
  if (mod10 === 1 && mod100 !== 11) return 'тренировочный день'
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return 'тренировочных дня'
  return 'тренировочных дней'
}
