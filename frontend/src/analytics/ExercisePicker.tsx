import { useEffect, useRef, useState } from 'react'
import type { ExerciseStatsItem } from '../api'
import { ChevronIcon } from '../icons'
import { num } from './format'

export type PickerExercise = ExerciseStatsItem & { hasData: boolean }

type Props = {
  exercises: PickerExercise[]
  selectedId: number | null
  onSelect: (id: number) => void
}

function ExercisePicker({ exercises, selectedId, onSelect }: Props) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const boxRef = useRef<HTMLDivElement>(null)
  const selected = exercises.find((e) => e.id === selectedId)
  const q = query.trim().toLowerCase()
  const filtered = q ? exercises.filter((e) => e.name.toLowerCase().includes(q)) : exercises

  useEffect(() => {
    if (!open) return
    const onDown = (e: MouseEvent) => {
      if (boxRef.current && !boxRef.current.contains(e.target as Node)) setOpen(false)
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', onDown)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onDown)
      document.removeEventListener('keydown', onKey)
    }
  }, [open])

  return (
    <div className="ex-picker" ref={boxRef}>
      <button
        type="button"
        className="ex-picker__toggle"
        aria-haspopup="listbox"
        aria-expanded={open}
        onClick={() => {
          setOpen((o) => !o)
          setQuery('')
        }}
      >
        <span className="ex-picker__current">{selected?.name ?? 'Выберите упражнение'}</span>
        <ChevronIcon size={16} />
      </button>
      {open && (
        <div className="ex-picker__popover" role="dialog" aria-label="Выбор упражнения">
          <input
            className="ex-picker__search"
            placeholder="Найти упражнение"
            aria-label="Поиск упражнения"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            autoFocus
          />
          <ul className="ex-picker__list" role="listbox" aria-label="Упражнения">
            {filtered.map((e) => (
              <li key={e.id}>
                <button
                  type="button"
                  role="option"
                  aria-selected={e.id === selectedId}
                  className={e.id === selectedId ? 'ex-picker__item ex-picker__item--selected' : 'ex-picker__item'}
                  onClick={() => {
                    onSelect(e.id)
                    setOpen(false)
                  }}
                >
                  <span className="ex-picker__name">{e.name}</span>
                  {!e.hasData ? (
                    <span className="ex-picker__weight">нет подходов</span>
                  ) : (
                    e.loadType === 'weighted' &&
                    e.workingWeight !== null &&
                    e.workingWeight > 0 && <span className="ex-picker__weight">{num(e.workingWeight)} кг</span>
                  )}
                </button>
              </li>
            ))}
            {filtered.length === 0 && <li className="ex-picker__empty">Ничего не нашлось</li>}
          </ul>
        </div>
      )}
    </div>
  )
}

export default ExercisePicker
