import { Link } from 'react-router-dom'
import type { ExerciseListItem } from './api'
import { HeartIcon } from './icons'
import './ExerciseCard.css'

// карточка каталога: картинка, название-ссылка, мышцы (основные — красные), рабочий вес, избранное
function ExerciseCard({
  exercise: ex,
  to,
  onToggleFavourite,
  favouriteBusy = false,
}: {
  exercise: ExerciseListItem
  to: string
  onToggleFavourite?: () => void
  favouriteBusy?: boolean
}) {
  const muscles = [...(ex.muscles ?? [])].sort((a, b) => (a.role === b.role ? 0 : a.role === 'primary' ? -1 : 1))
  const weight =
    ex.workingWeight != null
      ? `Рабочий вес ${ex.workingWeight} кг`
      : ex.loadType === 'bodyweight'
        ? 'Собственный вес'
        : 'Рабочий вес появится после тренировки'

  return (
    <article className="exercise-card">
      {ex.imageUrl ? (
        <img className="exercise-card__image" src={ex.imageUrl} alt="" />
      ) : (
        <div className="exercise-card__image exercise-card__placeholder">нет картинки</div>
      )}
      {onToggleFavourite && (
        <button
          type="button"
          className={`exercise-card__fav${ex.favourite ? ' exercise-card__fav--on' : ''}`}
          onClick={onToggleFavourite}
          disabled={favouriteBusy}
          aria-pressed={ex.favourite}
          aria-label={ex.favourite ? `Убрать «${ex.name}» из избранного` : `Добавить «${ex.name}» в избранное`}
        >
          <HeartIcon filled={ex.favourite} />
        </button>
      )}
      <div className="exercise-card__body">
        <Link to={to} className="exercise-card__name">
          {ex.name}
        </Link>
        {muscles.length > 0 && (
          <div className="exercise-card__tags">
            {muscles.map((m) => (
              <span key={m.muscleId} className={`tag${m.role === 'primary' ? ' tag--primary' : ''}`}>
                {m.muscleName}
              </span>
            ))}
          </div>
        )}
        <p className="exercise-card__meta">
          {weight}
          {ex.custom && ' · своё'}
        </p>
      </div>
    </article>
  )
}

export default ExerciseCard
