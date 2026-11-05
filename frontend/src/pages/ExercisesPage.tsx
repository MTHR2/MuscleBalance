import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  getExercises,
  getMuscleGroups,
  getMuscles,
  setFavourite,
  type ExerciseListItem,
  type Muscle,
  type MuscleGroupCard as Group,
} from '../api'
import CreateExerciseModal from '../CreateExerciseModal'
import ExerciseCard from '../ExerciseCard'
import { HeartIcon, SearchIcon } from '../icons'
import { plural } from '../plural'
import './ExercisesPage.css'

type Filter = 'all' | 'mine' | 'favourite'

// каталог: группы мышц сверху, ниже упражнения; поиск и фильтры — на бэке (GET /exercises)
function ExercisesPage() {
  const navigate = useNavigate()
  const params = useParams()
  const groupId = params.groupId ? Number(params.groupId) : null

  const [groups, setGroups] = useState<Group[] | null>(null)
  const [muscles, setMuscles] = useState<Muscle[]>([])
  const [all, setAll] = useState<ExerciseListItem[] | null>(null) // весь каталог — для счётчиков на группах
  const [items, setItems] = useState<ExerciseListItem[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [query, setQuery] = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')
  const [filter, setFilter] = useState<Filter>('all')
  const [favBusy, setFavBusy] = useState<number | null>(null)
  const [creating, setCreating] = useState(false)

  const fail = (e: unknown) => setError(e instanceof Error ? e.message : String(e))
  const closeCreate = useCallback(() => setCreating(false), [])

  const loadAll = useCallback(() => getExercises().then(setAll).catch(fail), [])

  useEffect(() => {
    getMuscleGroups().then(setGroups).catch(fail)
    getMuscles().then(setMuscles).catch(fail)
    loadAll()
  }, [loadAll])

  // поиск уходит на бэк, когда пользователь перестал печатать
  useEffect(() => {
    const t = setTimeout(() => setDebouncedQuery(query), 250)
    return () => clearTimeout(t)
  }, [query])

  const loadItems = useCallback(
    () =>
      getExercises({
        q: debouncedQuery,
        muscleGroupId: groupId,
        favourite: filter === 'favourite',
        custom: filter === 'mine',
      })
        .then(setItems)
        .catch(fail),
    [debouncedQuery, groupId, filter],
  )

  useEffect(() => {
    loadItems()
  }, [loadItems])

  // группа упражнения — по его основной мышце; нужна для адреса карточки
  const groupOfMuscle = useMemo(() => new Map(muscles.map((m) => [m.id, m.groupId])), [muscles])
  const groupOf = (ex: ExerciseListItem) => {
    const primary = ex.muscles?.find((m) => m.role === 'primary')
    return groupId ?? (primary ? groupOfMuscle.get(primary.muscleId) : undefined) ?? 0
  }

  const counts = useMemo(() => {
    const result = new Map<number, number>()
    for (const ex of all ?? []) {
      const ids = new Set(
        (ex.muscles ?? []).filter((m) => m.role === 'primary').map((m) => groupOfMuscle.get(m.muscleId)),
      )
      for (const id of ids) if (id !== undefined) result.set(id, (result.get(id) ?? 0) + 1)
    }
    return result
  }, [all, groupOfMuscle])

  async function toggleFavourite(ex: ExerciseListItem) {
    setFavBusy(ex.id)
    try {
      const updated = await setFavourite(ex.id, !ex.favourite)
      const patch = (list: ExerciseListItem[] | null) =>
        list?.map((e) => (e.id === ex.id ? { ...e, favourite: updated.favourite } : e)) ?? null
      setAll(patch)
      // в фильтре «Избранные» снятое сердечко убирает карточку из списка
      setItems((list) =>
        filter === 'favourite' && !updated.favourite ? (list?.filter((e) => e.id !== ex.id) ?? null) : patch(list),
      )
    } catch (e) {
      fail(e)
    } finally {
      setFavBusy(null)
    }
  }

  const group = groups?.find((g) => g.id === groupId) ?? null
  const filters: { key: Filter; label: string }[] = [
    { key: 'all', label: 'Все' },
    { key: 'mine', label: 'Мои' },
    { key: 'favourite', label: 'Избранные' },
  ]

  return (
    <main className="page">
      <div className="page-head">
        <h1 className="page-title exercises__title">Упражнения</h1>
        <label className="search">
          <SearchIcon />
          <span className="sr-only">Поиск упражнения</span>
          <input
            type="search"
            className="search__input"
            placeholder="Поиск по названию"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </label>
        <button type="button" className="gray-button" onClick={() => setCreating(true)} disabled={muscles.length === 0}>
          + Своё упражнение
        </button>
      </div>

      <div className="exercises__filters" role="group" aria-label="Фильтр">
        {filters.map((f) => (
          <button
            key={f.key}
            type="button"
            className={`chip-button${filter === f.key ? ' chip-button--active' : ''}`}
            aria-pressed={filter === f.key}
            onClick={() => setFilter(f.key)}
          >
            {f.key === 'favourite' && <HeartIcon size={16} />}
            {f.label}
          </button>
        ))}
      </div>

      {error && <p className="error">Ошибка: {error}</p>}

      <h2 className="section-title">Группа мышц</h2>
      {groups === null && !error && <p className="muted">Загрузка…</p>}
      {groups && (
        <div className="group-grid">
          {groups.map((g) => {
            const selected = g.id === groupId
            return (
              <button
                key={g.id}
                type="button"
                className={`group-tile${selected ? ' group-tile--selected' : ''}`}
                aria-pressed={selected}
                // повторный клик по выбранной группе снимает фильтр
                onClick={() => navigate(selected ? '/exercises' : `/exercises/${g.id}`)}
              >
                {g.imageUrl ? (
                  <img className="group-tile__image" src={g.imageUrl} alt="" />
                ) : (
                  <span className="group-tile__image group-tile__placeholder" />
                )}
                <span className="group-tile__name">
                  {g.name}
                  {all && <span className="group-tile__count">{counts.get(g.id) ?? 0}</span>}
                </span>
              </button>
            )
          })}
        </div>
      )}

      <div className="exercises__list-head">
        <h2 className="exercises__list-title">{group?.name ?? 'Все упражнения'}</h2>
        {items && (
          <span className="exercises__list-meta">
            {items.length} {plural(items.length, ['упражнение', 'упражнения', 'упражнений'])}, по алфавиту
            {group && (
              <>
                {' · '}
                <Link to="/exercises">показать все</Link>
              </>
            )}
          </span>
        )}
      </div>

      {items === null && !error && <p className="muted">Загрузка…</p>}
      {items?.length === 0 && (
        <p className="muted">
          {debouncedQuery.trim()
            ? 'Ничего не нашлось.'
            : filter === 'mine'
              ? 'Своих упражнений пока нет — создайте первое кнопкой «+ Своё упражнение».'
              : filter === 'favourite'
                ? 'В избранном пока пусто — отметьте упражнение сердечком.'
                : 'Упражнений пока нет.'}
        </p>
      )}
      {items && items.length > 0 && (
        <div className="exercise-grid">
          {items.map((ex) => (
            <ExerciseCard
              key={ex.id}
              exercise={ex}
              to={`/exercises/${groupOf(ex)}/${ex.id}`}
              onToggleFavourite={() => toggleFavourite(ex)}
              favouriteBusy={favBusy === ex.id}
            />
          ))}
        </div>
      )}

      {creating && (
        <CreateExerciseModal
          muscles={muscles}
          onClose={closeCreate}
          onCreated={(ex) => {
            setCreating(false)
            loadAll()
            const primary = ex.muscles.find((m) => m.role === 'primary')
            navigate(`/exercises/${(primary && groupOfMuscle.get(primary.muscleId)) ?? 0}/${ex.id}`)
          }}
        />
      )}
    </main>
  )
}

export default ExercisesPage
