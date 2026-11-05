import { useEffect, useState, type FormEvent } from 'react'
import {
  createWorkoutTemplate,
  getExercisesByGroup,
  getMuscleGroups,
  type ExerciseListItem,
  type MuscleGroupCard as Group,
} from './api'
import Modal from './Modal'
import './CreateWorkoutModal.css'

// подходы в тренировке не вводятся: в шаблон уходит значение по умолчанию, меняется уже на тренировке
const DEFAULT_SETS = 3

function CreateWorkoutModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const [name, setName] = useState('')
  const [selected, setSelected] = useState<ExerciseListItem[]>([])
  const [picking, setPicking] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const toggle = (exercise: ExerciseListItem) =>
    setSelected((list) =>
      list.some((e) => e.id === exercise.id) ? list.filter((e) => e.id !== exercise.id) : [...list, exercise],
    )

  async function save(e: FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await createWorkoutTemplate({
        name: name.trim(),
        exercises: selected.map((ex) => ({ exerciseId: ex.id, targetSets: DEFAULT_SETS })),
      })
      onCreated()
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err))
    } finally {
      setSaving(false)
    }
  }

  if (picking) {
    return (
      <Modal title="Добавить упражнение" onClose={onClose}>
        <ExercisePicker selectedIds={selected.map((e) => e.id)} onToggle={toggle} />
        <div className="workout-form__footer">
          <button type="button" className="primary-button" onClick={() => setPicking(false)}>
            Готово{selected.length > 0 && ` (${selected.length})`}
          </button>
        </div>
      </Modal>
    )
  }

  const canSave = name.trim() !== '' && selected.length > 0 && !saving

  return (
    <Modal title="Новая тренировка" onClose={onClose}>
      <form className="workout-form" onSubmit={save}>
        <label className="field">
          <span className="field__label">Название</span>
          <input
            className="field__input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Например, Грудь + трицепс"
            autoFocus
          />
        </label>

        <div className="field">
          <span className="field__label">Упражнения</span>
          {selected.length === 0 && <p className="muted workout-form__empty">Пока не добавлено ни одного</p>}
          <ul className="selected-list">
            {selected.map((ex) => (
              <li key={ex.id} className="selected-item">
                {ex.imageUrl ? (
                  <img className="selected-item__image" src={ex.imageUrl} alt="" />
                ) : (
                  <div className="selected-item__image" />
                )}
                <span className="selected-item__name">{ex.name}</span>
                <span className="selected-item__sets">{DEFAULT_SETS} подхода</span>
                <button
                  type="button"
                  className="selected-item__remove"
                  onClick={() => toggle(ex)}
                  aria-label={`Убрать ${ex.name}`}
                >
                  ×
                </button>
              </li>
            ))}
          </ul>
          <button type="button" className="gray-button" onClick={() => setPicking(true)}>
            + Добавить упражнение
          </button>
        </div>

        {error && <p className="error">{error}</p>}

        <div className="workout-form__footer">
          <button type="submit" className="primary-button" disabled={!canSave}>
            {saving ? 'Сохранение…' : 'Сохранить'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

// выбор упражнений: сначала группа мышц, затем клик по упражнению добавляет или убирает его
export function ExercisePicker({
  selectedIds,
  onToggle,
}: {
  selectedIds: number[]
  onToggle: (exercise: ExerciseListItem) => void
}) {
  const [groups, setGroups] = useState<Group[]>([])
  const [groupId, setGroupId] = useState<number | null>(null)
  const [exercises, setExercises] = useState<ExerciseListItem[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getMuscleGroups()
      .then((list) => {
        setGroups(list)
        if (list.length > 0) setGroupId(list[0].id)
      })
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))
  }, [])

  useEffect(() => {
    if (groupId === null) return
    setExercises(null)
    getExercisesByGroup(groupId)
      .then(setExercises)
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))
  }, [groupId])

  return (
    <div className="picker">
      <div className="picker__groups">
        {groups.map((g) => (
          <button
            key={g.id}
            type="button"
            className={`chip-button${g.id === groupId ? ' chip-button--active' : ''}`}
            onClick={() => setGroupId(g.id)}
          >
            {g.name}
          </button>
        ))}
      </div>

      {error && <p className="error">Ошибка: {error}</p>}
      {!error && exercises === null && <p className="muted">Загрузка…</p>}
      {exercises?.length === 0 && <p className="muted">В этой группе пока нет упражнений</p>}

      <div className="picker__list">
        {exercises?.map((ex) => {
          const isSelected = selectedIds.includes(ex.id)
          return (
            <button
              key={ex.id}
              type="button"
              className={`picker-item${isSelected ? ' picker-item--selected' : ''}`}
              onClick={() => onToggle(ex)}
              aria-pressed={isSelected}
            >
              {ex.imageUrl ? (
                <img className="picker-item__image" src={ex.imageUrl} alt="" />
              ) : (
                <div className="picker-item__image picker-item__placeholder">нет картинки</div>
              )}
              <span className="picker-item__name">{ex.name}</span>
              {isSelected && <span className="picker-item__check">✓</span>}
            </button>
          )
        })}
      </div>
    </div>
  )
}

export default CreateWorkoutModal
