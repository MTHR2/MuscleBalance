import { useEffect, useState } from 'react'
import { getMuscles, type Muscle } from './api'

export type Targets = {
  defaultWeeklySets: string // строка из поля; пусто — общей цели нет
  perMuscle: Record<number, string> // muscleId → цель; пусто — как общая, 0 — не качаем
}

// значение поля цели: целое 0..100 (для общей — 1..100), пусто — не задано
export function parseTarget(raw: string, min = 0): number | null | 'invalid' {
  if (raw.trim() === '') return null
  const n = Number(raw)
  return Number.isInteger(n) && n >= min && n <= 100 ? n : 'invalid'
}

// цели плана: общая цель подходов на мышцу в неделю и отдельные цели на мышцы
function TargetsEditor({ value, onChange }: { value: Targets; onChange: (v: Targets) => void }) {
  const [muscles, setMuscles] = useState<Muscle[] | null>(null)
  const [open, setOpen] = useState(Object.keys(value.perMuscle).length > 0)

  useEffect(() => {
    getMuscles()
      .then(setMuscles)
      .catch(() => setMuscles([]))
  }, [])

  const groups = new Map<string, Muscle[]>()
  for (const m of muscles ?? []) groups.set(m.groupName, [...(groups.get(m.groupName) ?? []), m])

  const setMuscle = (id: number, raw: string) => {
    const perMuscle = { ...value.perMuscle }
    if (raw === '') delete perMuscle[id]
    else perMuscle[id] = raw
    onChange({ ...value, perMuscle })
  }

  const defaultInvalid = parseTarget(value.defaultWeeklySets, 1) === 'invalid'

  return (
    <div className="field targets-editor">
      <span className="field__label">Цели на мышцы</span>
      <label className="targets-editor__default">
        <span>Подходов на каждую мышцу в неделю</span>
        <input
          className="field__input targets-editor__input"
          inputMode="numeric"
          value={value.defaultWeeklySets}
          onChange={(e) => onChange({ ...value, defaultWeeklySets: e.target.value })}
          aria-invalid={defaultInvalid}
          placeholder="—"
        />
      </label>
      <p className="targets-editor__hint">
        По этой цели тепловая карта на главной красит мышцы. Оставьте пустым, если цель не нужна.
      </p>

      <button type="button" className="targets-editor__toggle" aria-expanded={open} onClick={() => setOpen((v) => !v)}>
        {open ? 'Скрыть отдельные мышцы' : 'Настроить отдельные мышцы'}
      </button>

      {open && muscles === null && <p className="muted">Загрузка…</p>}
      {open && muscles?.length === 0 && <p className="muted">Не удалось загрузить список мышц.</p>}
      {open && muscles && muscles.length > 0 && (
        <>
          <p className="targets-editor__hint">Пусто — как общая цель, 0 — мышцу в этом плане не качаем.</p>
          <div className="targets-editor__groups">
            {[...groups].map(([group, list]) => (
              <fieldset key={group} className="targets-editor__group">
                <legend>{group}</legend>
                {list.map((m) => {
                  const raw = value.perMuscle[m.id] ?? ''
                  return (
                    <label key={m.id} className="targets-editor__muscle">
                      <span>{m.name}</span>
                      <input
                        className="field__input targets-editor__input"
                        inputMode="numeric"
                        value={raw}
                        onChange={(e) => setMuscle(m.id, e.target.value)}
                        placeholder={value.defaultWeeklySets || '—'}
                        aria-invalid={parseTarget(raw) === 'invalid'}
                      />
                    </label>
                  )
                })}
              </fieldset>
            ))}
          </div>
        </>
      )}
    </div>
  )
}

export default TargetsEditor
