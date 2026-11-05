import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getExercise, getMuscleGroup, setFavourite, type ExerciseDetails, type MuscleGroupCard as Group } from '../api'
import { HeartIcon } from '../icons'
import './ExercisePage.css'

const loadTypeLabels: Record<ExerciseDetails['loadType'], string> = {
  weighted: 'С отягощением',
  bodyweight: 'Собственный вес',
}

function ExercisePage() {
  const params = useParams()
  const groupId = Number(params.groupId)
  const exerciseId = Number(params.exerciseId)
  const [exercise, setExercise] = useState<ExerciseDetails | null>(null)
  const [group, setGroup] = useState<Group | null>(null)
  const [error, setError] = useState<string | null>(null)

  const [favBusy, setFavBusy] = useState(false)

  useEffect(() => {
    // groupId = 0 — у упражнения нет основной мышцы с группой; тогда ссылка ведёт в весь каталог
    Promise.all([getExercise(exerciseId), groupId > 0 ? getMuscleGroup(groupId) : Promise.resolve(null)])
      .then(([e, g]) => {
        setExercise(e)
        setGroup(g)
      })
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))
  }, [exerciseId, groupId])

  async function toggleFavourite(ex: ExerciseDetails) {
    setFavBusy(true)
    try {
      setExercise(await setFavourite(ex.id, !ex.favourite))
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setFavBusy(false)
    }
  }

  const primary = exercise?.muscles.filter((m) => m.role === 'primary') ?? []
  const secondary = exercise?.muscles.filter((m) => m.role === 'secondary') ?? []

  return (
    <main className="page">
      <Link to={group ? `/exercises/${groupId}` : '/exercises'} className="back-link">
        ← {group?.name ?? 'Все упражнения'}
      </Link>

      {error && <p className="error">Ошибка: {error}</p>}
      {!error && exercise === null && <p className="muted">Загрузка…</p>}
      {exercise && (
        <div className="exercise">
          {exercise.imageUrl ? (
            <img className="exercise__image" src={exercise.imageUrl} alt={exercise.name} />
          ) : (
            <div className="exercise__image exercise__placeholder">нет картинки</div>
          )}

          <div className="exercise__info">
            <div className="exercise__title-row">
              <h1 className="page-title">{exercise.name}</h1>
              <button
                type="button"
                className={`exercise__fav${exercise.favourite ? ' exercise__fav--on' : ''}`}
                onClick={() => toggleFavourite(exercise)}
                disabled={favBusy}
                aria-pressed={exercise.favourite}
              >
                <HeartIcon size={18} filled={exercise.favourite} />
                {exercise.favourite ? 'В избранном' : 'В избранное'}
              </button>
            </div>
            {exercise.custom && <span className="tag">своё упражнение</span>}
            {exercise.description && <p className="exercise__description">{exercise.description}</p>}

            <dl className="exercise__facts">
              <dt>Тип нагрузки</dt>
              <dd>{loadTypeLabels[exercise.loadType]}</dd>
              <dt>Рабочий вес</dt>
              <dd>{exercise.workingWeight != null ? `${exercise.workingWeight} кг` : 'ещё не задан'}</dd>
            </dl>

            {primary.length > 0 && (
              <>
                <h2 className="exercise__subtitle">Основные мышцы</h2>
                <div className="chips">
                  {primary.map((m) => (
                    <span key={m.muscleId} className="chip chip--primary">
                      {m.muscleName}
                    </span>
                  ))}
                </div>
              </>
            )}

            {secondary.length > 0 && (
              <>
                <h2 className="exercise__subtitle">Вспомогательные мышцы</h2>
                <div className="chips">
                  {secondary.map((m) => (
                    <span key={m.muscleId} className="chip">
                      {m.muscleName}
                    </span>
                  ))}
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </main>
  )
}

export default ExercisePage
