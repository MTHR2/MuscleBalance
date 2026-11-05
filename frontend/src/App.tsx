import { useEffect, useState } from 'react'
import { Link, NavLink, Route, Routes, useLocation } from 'react-router-dom'
import { getActiveWorkout, WORKOUT_CHANGED } from './api'
import { PlayIcon } from './icons'
import HomePage from './pages/HomePage'
import ExercisesPage from './pages/ExercisesPage'
import ExercisePage from './pages/ExercisePage'
import WorkoutsPage from './pages/WorkoutsPage'
import PlansPage from './pages/PlansPage'
import CreatePlanPage from './pages/CreatePlanPage'
import WorkoutPage from './pages/WorkoutPage'
import './App.css'

const navClass = ({ isActive }: { isActive: boolean }) => `nav__link${isActive ? ' active' : ''}`

function Header() {
  const { pathname } = useLocation()
  const onWorkout = pathname === '/workout'
  // идёт ли тренировка: тогда кнопка «Продолжить», а не «Начать»; перепроверяем при смене страницы
  const [hasActive, setHasActive] = useState(false)

  useEffect(() => {
    const check = () =>
      getActiveWorkout()
        .then((w) => setHasActive(w !== null))
        .catch(() => setHasActive(false))
    check()
    window.addEventListener(WORKOUT_CHANGED, check)
    return () => window.removeEventListener(WORKOUT_CHANGED, check)
  }, [pathname])

  return (
    <header className="header">
      <div className="header__inner">
        <Link to="/" className="logo">
          GymTracker
        </Link>
        <nav className="nav" aria-label="Разделы">
          <NavLink to="/" end className={navClass}>
            Главная
          </NavLink>
          <NavLink to="/exercises" className={navClass}>
            Упражнения
          </NavLink>
          <NavLink to="/workouts" className={navClass}>
            Тренировки
          </NavLink>
          <NavLink to="/plans" className={navClass}>
            План
          </NavLink>
        </nav>
        {onWorkout ? (
          hasActive && <span className="live-badge">Идёт тренировка</span>
        ) : (
          <Link to="/workout" className="primary-button">
            <PlayIcon />
            {hasActive ? 'Продолжить' : 'Начать'}
            <span className="hide-narrow"> тренировку</span>
          </Link>
        )}
      </div>
    </header>
  )
}

function App() {
  return (
    <>
      <Header />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/exercises" element={<ExercisesPage />} />
        <Route path="/exercises/:groupId" element={<ExercisesPage />} />
        <Route path="/exercises/:groupId/:exerciseId" element={<ExercisePage />} />
        <Route path="/workouts" element={<WorkoutsPage />} />
        <Route path="/plans" element={<PlansPage />} />
        <Route path="/plans/new" element={<CreatePlanPage />} />
        <Route path="/plans/:planId" element={<PlansPage />} />
        <Route path="/plans/:planId/edit" element={<CreatePlanPage />} />
        <Route path="/workout" element={<WorkoutPage />} />
      </Routes>
    </>
  )
}

export default App
