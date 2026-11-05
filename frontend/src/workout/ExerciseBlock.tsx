import { useState, type KeyboardEvent } from 'react'
import {
  addSet,
  deleteSet,
  removeWorkoutExercise,
  updateSet,
  updateTargetSets,
  type SetValues,
  type WorkoutExercise,
  type WorkoutSet,
} from '../api'
import { CheckIcon, ChevronIcon, PlusIcon } from '../icons'

const MAX_TARGET_SETS = 50
const WEIGHT_STEP = 2.5

// вес: >= 0, до 9999.99, не больше двух знаков; повторы: целое 1..999
function parseWeight(raw: string): number | null {
  const n = Number(raw.replace(',', '.'))
  return raw.trim() !== '' && Number.isFinite(n) && n >= 0 && n <= 9999.99 ? Math.round(n * 100) / 100 : null
}

function parseReps(raw: string): number | null {
  const n = Number(raw)
  return Number.isInteger(n) && n >= 1 && n <= 999 ? n : null
}

function repsHint(ex: WorkoutExercise) {
  if (ex.repsMin != null && ex.repsMax != null) {
    return ex.repsMin === ex.repsMax ? `${ex.repsMin} повт.` : `${ex.repsMin}–${ex.repsMax} повт.`
  }
  if (ex.repsMin != null) return `от ${ex.repsMin} повт.`
  if (ex.repsMax != null) return `до ${ex.repsMax} повт.`
  return null
}

export const workingSets = (ex: WorkoutExercise) => ex.sets.filter((s) => !s.warmup).length

// одно упражнение тренировки: свёрнутое — строка с прогрессом, раскрытое — подходы и новый подход
function ExerciseBlock({
  workoutId,
  exercise: ex,
  open,
  current,
  onToggle,
  onChange,
  onSetLogged,
  onRemoved,
}: {
  workoutId: number
  exercise: WorkoutExercise
  open: boolean
  current: boolean
  onToggle: () => void
  onChange: (ex: WorkoutExercise) => void
  onSetLogged: () => void
  onRemoved: () => void
}) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function run(action: () => Promise<void>) {
    setBusy(true)
    setError(null)
    try {
      await action()
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setBusy(false)
    }
  }

  const last = ex.sets[ex.sets.length - 1]
  const lastWorking = [...ex.sets].reverse().find((s) => !s.warmup)
  // вес для нового подхода: последний рабочий в этой тренировке, иначе рабочий вес с прошлой
  const defaultWeight = lastWorking?.weight ?? ex.workingWeight ?? last?.weight ?? 0
  const defaultReps = last?.reps ?? ex.repsMin ?? ex.repsMax ?? 10

  const setTarget = (targetSets: number) =>
    run(async () => onChange(await updateTargetSets(workoutId, ex.id, targetSets)))

  const add = (values: SetValues) =>
    run(async () => {
      const created = await addSet(workoutId, ex.id, values)
      onChange({ ...ex, sets: [...ex.sets, created] })
      onSetLogged()
    })

  const save = (set: WorkoutSet, values: SetValues) =>
    run(async () => {
      const updated = await updateSet(workoutId, ex.id, set.id, values)
      onChange({ ...ex, sets: ex.sets.map((s) => (s.id === updated.id ? updated : s)) })
    })

  const removeExercise = () => {
    if (ex.sets.length > 0 && !window.confirm(`Убрать «${ex.exerciseName}» из тренировки? Записанные подходы удалятся.`)) {
      return
    }
    run(async () => {
      await removeWorkoutExercise(workoutId, ex.id)
      onRemoved()
    })
  }

  const remove = (set: WorkoutSet) =>
    run(async () => {
      await deleteSet(workoutId, ex.id, set.id)
      onChange({ ...ex, sets: ex.sets.filter((s) => s.id !== set.id) })
    })

  const done = workingSets(ex)
  const complete = ex.targetSets > 0 && done >= ex.targetSets
  const bodyweight = ex.loadType === 'bodyweight'
  const hint = [
    repsHint(ex),
    ex.workingWeight != null ? `рабочий вес ${ex.workingWeight} кг` : bodyweight ? 'собственный вес' : null,
    ex.plannedSets == null ? 'добавлено по ходу' : null,
  ].filter(Boolean)

  const image = ex.imageUrl ? <img className="wex__image" src={ex.imageUrl} alt="" /> : <div className="wex__image" />

  if (!open) {
    return (
      <article className={`wex wex--closed${complete ? ' wex--done' : ''}`}>
        <button type="button" className="wex__toggle" onClick={onToggle} aria-expanded={false}>
          {image}
          <span className="wex__title">
            <span className="wex__name">{ex.exerciseName}</span>
            {hint.length > 0 && <span className="wex__hint">{hint.join(' · ')}</span>}
          </span>
          <span className={`wex__chip${complete ? ' wex__chip--done' : ''}`}>
            {complete && <CheckIcon size={16} />}
            {done} / {ex.targetSets}
          </span>
        </button>
      </article>
    )
  }

  return (
    <article className={`wex wex--open${current ? ' wex--current' : ''}${complete ? ' wex--done' : ''}`}>
      <header className="wex__head">
        <button type="button" className="wex__toggle wex__toggle--open" onClick={onToggle} aria-expanded={true}>
          {image}
          <span className="wex__title">
            {current && !complete && <span className="wex__now">Сейчас</span>}
            <span className="wex__name wex__name--big">{ex.exerciseName}</span>
            {hint.length > 0 && <span className="wex__hint">{hint.join(' · ')}</span>}
          </span>
          <span className="wex__chevron" aria-hidden="true">
            <ChevronIcon />
          </span>
        </button>
        <div className="wex__target" role="group" aria-label="Подходы: сделано из запланированных">
          <button
            type="button"
            className="round-button"
            onClick={() => setTarget(ex.targetSets - 1)}
            disabled={busy || ex.targetSets <= 0}
            aria-label="Меньше подходов"
          >
            −
          </button>
          <span className="wex__progress">
            {done} / {ex.targetSets}
          </span>
          <button
            type="button"
            className="round-button"
            onClick={() => setTarget(ex.targetSets + 1)}
            disabled={busy || ex.targetSets >= MAX_TARGET_SETS}
            aria-label="Больше подходов"
          >
            +
          </button>
        </div>
      </header>

      <div className="sets">
        <div className="sets__row sets__row--head" aria-hidden="true">
          <span>№</span>
          <span>{bodyweight ? 'Доп. вес, кг' : 'Вес, кг'}</span>
          <span>Повторы</span>
          <span />
          <span />
          <span />
        </div>
        {ex.sets.map((s, i) => (
          <SetRow
            key={s.id}
            set={s}
            disabled={busy}
            canDelete={i === ex.sets.length - 1}
            onSave={(v) => save(s, v)}
            onDelete={() => remove(s)}
          />
        ))}
        {/* key сбрасывает поля нового подхода после каждого добавления */}
        <NewSetRow
          key={ex.sets.length}
          number={ex.sets.length + 1}
          weightLabel={bodyweight ? 'Доп. вес, кг' : 'Вес, кг'}
          defaultWeight={defaultWeight}
          defaultReps={defaultReps}
          disabled={busy}
          onAdd={add}
        />
      </div>

      {error && <p className="error wex__error">{error}</p>}

      <button type="button" className="wex__remove" onClick={removeExercise} disabled={busy}>
        Убрать упражнение
      </button>
    </article>
  )
}

// сделанный подход: правка веса и повторов сохраняется при уходе с поля или Enter
function SetRow({
  set,
  disabled,
  canDelete,
  onSave,
  onDelete,
}: {
  set: WorkoutSet
  disabled: boolean
  canDelete: boolean
  onSave: (v: SetValues) => void
  onDelete: () => void
}) {
  const [weight, setWeight] = useState(String(set.weight))
  const [reps, setReps] = useState(String(set.reps))

  function commit() {
    const w = parseWeight(weight)
    const r = parseReps(reps)
    if (w === null || r === null) {
      // неверное значение — возвращаем сохранённое
      setWeight(String(set.weight))
      setReps(String(set.reps))
      return
    }
    if (w !== set.weight || r !== set.reps) onSave({ weight: w, reps: r, warmup: set.warmup })
  }

  const onKey = (e: KeyboardEvent<HTMLInputElement>) => e.key === 'Enter' && e.currentTarget.blur()

  return (
    <div className={`sets__row sets__row--done${set.warmup ? ' sets__row--warmup' : ''}`}>
      <span className="sets__number">{set.setNumber}</span>
      <input
        className="sets__input"
        inputMode="decimal"
        value={weight}
        onChange={(e) => setWeight(e.target.value)}
        onBlur={commit}
        onKeyDown={onKey}
        disabled={disabled}
        aria-label={`Вес, подход ${set.setNumber}`}
      />
      <input
        className="sets__input"
        inputMode="numeric"
        value={reps}
        onChange={(e) => setReps(e.target.value)}
        onBlur={commit}
        onKeyDown={onKey}
        disabled={disabled}
        aria-label={`Повторы, подход ${set.setNumber}`}
      />
      <button
        type="button"
        className={`warmup-toggle${set.warmup ? ' warmup-toggle--on' : ''}`}
        onClick={() => onSave({ weight: set.weight, reps: set.reps, warmup: !set.warmup })}
        disabled={disabled}
        aria-pressed={set.warmup}
        title="Разминочный подход не идёт в итоги и рабочий вес"
      >
        Разминка
      </button>
      <span className={`sets__mark${set.warmup ? ' sets__mark--warmup' : ''}`} role="img" aria-label="Записан">
        <CheckIcon />
      </span>
      {/* удалить можно только последний подход, иначе собьётся нумерация */}
      {canDelete ? (
        <button
          type="button"
          className="sets__delete"
          onClick={onDelete}
          disabled={disabled}
          aria-label={`Удалить подход ${set.setNumber}`}
        >
          ×
        </button>
      ) : (
        <span />
      )}
    </div>
  )
}

// следующий подход: значения подставлены; на телефоне — кнопки −/+ и большая кнопка «Записать подход»
function NewSetRow({
  number,
  weightLabel,
  defaultWeight,
  defaultReps,
  disabled,
  onAdd,
}: {
  number: number
  weightLabel: string
  defaultWeight: number
  defaultReps: number
  disabled: boolean
  onAdd: (v: SetValues) => void
}) {
  const [weight, setWeight] = useState(String(defaultWeight))
  const [reps, setReps] = useState(String(defaultReps))
  const [warmup, setWarmup] = useState(false)

  const w = parseWeight(weight)
  const r = parseReps(reps)

  const submit = () => {
    if (w !== null && r !== null) onAdd({ weight: w, reps: r, warmup })
  }
  const onKey = (e: KeyboardEvent<HTMLInputElement>) => e.key === 'Enter' && submit()

  const stepWeight = (delta: number) => setWeight(String(Math.max(0, Math.round(((w ?? 0) + delta) * 100) / 100)))
  const stepReps = (delta: number) => setReps(String(Math.min(999, Math.max(1, (r ?? 0) + delta))))

  return (
    <div className="sets__row sets__row--new">
      <span className="sets__number">{number}</span>
      <div className="stepper" data-label={weightLabel}>
        <button
          type="button"
          className="stepper__btn"
          onClick={() => stepWeight(-WEIGHT_STEP)}
          disabled={disabled}
          aria-label={`Минус ${WEIGHT_STEP} кг`}
        >
          −
        </button>
        <input
          className="sets__input"
          inputMode="decimal"
          value={weight}
          onChange={(e) => setWeight(e.target.value)}
          onKeyDown={onKey}
          disabled={disabled}
          aria-label={`${weightLabel}, новый подход`}
          aria-invalid={w === null}
        />
        <button
          type="button"
          className="stepper__btn"
          onClick={() => stepWeight(WEIGHT_STEP)}
          disabled={disabled}
          aria-label={`Плюс ${WEIGHT_STEP} кг`}
        >
          +
        </button>
      </div>
      <div className="stepper" data-label="Повторы">
        <button type="button" className="stepper__btn" onClick={() => stepReps(-1)} disabled={disabled} aria-label="Минус повтор">
          −
        </button>
        <input
          className="sets__input"
          inputMode="numeric"
          value={reps}
          onChange={(e) => setReps(e.target.value)}
          onKeyDown={onKey}
          disabled={disabled}
          aria-label="Повторы, новый подход"
          aria-invalid={r === null}
        />
        <button type="button" className="stepper__btn" onClick={() => stepReps(1)} disabled={disabled} aria-label="Плюс повтор">
          +
        </button>
      </div>
      <button
        type="button"
        className={`warmup-toggle${warmup ? ' warmup-toggle--on' : ''}`}
        onClick={() => setWarmup((v) => !v)}
        disabled={disabled}
        aria-pressed={warmup}
        title="Отметить как разминку"
      >
        Разминка
      </button>
      <button
        type="button"
        className="sets__add"
        onClick={submit}
        disabled={disabled || w === null || r === null}
        aria-label="Записать подход"
      >
        <PlusIcon />
        <span className="sets__add-text">Записать подход</span>
      </button>
    </div>
  )
}

export default ExerciseBlock
