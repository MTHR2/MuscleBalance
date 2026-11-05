import { useState, type FormEvent } from 'react'
import { createExercise, type ExerciseDetails, type LoadType, type Muscle, type MuscleRole } from './api'
import Modal from './Modal'
import './CreateExerciseModal.css'

type Role = MuscleRole | 'none'

const ROLE_LABELS: { role: Role; label: string }[] = [
  { role: 'none', label: '—' },
  { role: 'primary', label: 'Основная' },
  { role: 'secondary', label: 'Вспом.' },
]

// своё упражнение: название, описание, тип нагрузки и мышцы с ролями (хотя бы одна основная)
function CreateExerciseModal({
  muscles,
  onClose,
  onCreated,
}: {
  muscles: Muscle[]
  onClose: () => void
  onCreated: (ex: ExerciseDetails) => void
}) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [loadType, setLoadType] = useState<LoadType>('weighted')
  const [roles, setRoles] = useState<Record<number, MuscleRole>>({})
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const groups = new Map<string, Muscle[]>()
  for (const m of muscles) groups.set(m.groupName, [...(groups.get(m.groupName) ?? []), m])

  const setRole = (id: number, role: Role) =>
    setRoles((r) => {
      const next = { ...r }
      if (role === 'none') delete next[id]
      else next[id] = role
      return next
    })

  const hasPrimary = Object.values(roles).includes('primary')
  const canSave = name.trim() !== '' && hasPrimary && !saving

  async function save(e: FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      onCreated(
        await createExercise({
          name: name.trim(),
          description: description.trim() || null,
          loadType,
          muscles: Object.entries(roles).map(([id, role]) => ({ muscleId: Number(id), role })),
        }),
      )
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err))
      setSaving(false)
    }
  }

  return (
    <Modal title="Своё упражнение" onClose={onClose}>
      <form className="exercise-form" onSubmit={save}>
        <label className="field">
          <span className="field__label">Название</span>
          <input
            className="field__input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            maxLength={100}
            placeholder="Например, Пуловер с гантелью"
            autoFocus
          />
        </label>

        <label className="field">
          <span className="field__label">
            Описание <span className="field__optional">необязательно</span>
          </span>
          <textarea
            className="field__input"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            maxLength={1000}
            rows={2}
          />
        </label>

        <fieldset className="field exercise-form__fieldset">
          <legend className="field__label">Нагрузка</legend>
          <div className="exercise-form__chips">
            {(
              [
                ['weighted', 'С отягощением'],
                ['bodyweight', 'Собственный вес'],
              ] as const
            ).map(([value, label]) => (
              <button
                key={value}
                type="button"
                className={`chip-button${loadType === value ? ' chip-button--active' : ''}`}
                aria-pressed={loadType === value}
                onClick={() => setLoadType(value)}
              >
                {label}
              </button>
            ))}
          </div>
        </fieldset>

        <fieldset className="field exercise-form__fieldset">
          <legend className="field__label">Мышцы</legend>
          <p className="exercise-form__hint">Отметьте хотя бы одну основную. Вспомогательные считаются как полподхода.</p>
          <div className="exercise-form__groups">
            {[...groups].map(([group, list]) => (
              <div key={group} className="exercise-form__group">
                <span className="exercise-form__group-name">{group}</span>
                {list.map((m) => {
                  const current: Role = roles[m.id] ?? 'none'
                  return (
                    <div key={m.id} className="exercise-form__muscle" role="group" aria-label={m.name}>
                      <span className="exercise-form__muscle-name">{m.name}</span>
                      <span className="segmented">
                        {ROLE_LABELS.map((r) => (
                          <button
                            key={r.role}
                            type="button"
                            className={`segmented__item${current === r.role ? ` segmented__item--on segmented__item--${r.role}` : ''}`}
                            aria-pressed={current === r.role}
                            onClick={() => setRole(m.id, r.role)}
                          >
                            {r.label}
                          </button>
                        ))}
                      </span>
                    </div>
                  )
                })}
              </div>
            ))}
          </div>
        </fieldset>

        {error && <p className="error">{error}</p>}

        <div className="exercise-form__footer">
          <button type="submit" className="primary-button" disabled={!canSave}>
            {saving ? 'Сохранение…' : 'Создать'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

export default CreateExerciseModal
