import { useEffect, useState } from 'react'

// 0:07, 12:34, 1:02:03
export function formatClock(totalSeconds: number) {
  const s = Math.max(0, Math.floor(totalSeconds))
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const sec = String(s % 60).padStart(2, '0')
  return h > 0 ? `${h}:${String(m).padStart(2, '0')}:${sec}` : `${m}:${sec}`
}

// «меньше минуты», «58 мин», «1 ч 05 мин»
export function formatDuration(totalSeconds: number) {
  if (totalSeconds < 60) return 'меньше минуты'
  const minutes = Math.round(totalSeconds / 60)
  if (minutes < 60) return `${minutes} мин`
  return `${Math.floor(minutes / 60)} ч ${String(minutes % 60).padStart(2, '0')} мин`
}

// текущее время, обновляется раз в секунду
export function useNow() {
  const [now, setNow] = useState(() => Date.now())
  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(timer)
  }, [])
  return now
}

// секунды с начала тренировки
export function useElapsedSeconds(startedAt: string) {
  return (useNow() - Date.parse(startedAt)) / 1000
}
