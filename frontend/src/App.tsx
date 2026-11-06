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
import AnalyticsPage from './pages/AnalyticsPage'
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
          <svg width="30" height="30" viewBox="0 0 40 40" aria-hidden="true">
            <rect width="40" height="40" rx="11" fill="#bfddf2" />
            <path d="M12 14v12M28 14v12M8 17v6M32 17v6M12 20h16" fill="none" stroke="#2f6e9e" strokeWidth="3.4" strokeLinecap="round" />
          </svg>
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
          <NavLink to="/analytics" className={navClass}>
            Аналитика
          </NavLink>
        </nav>
        {onWorkout ? (
          hasActive && <span className="live-badge">Идёт тренировка</span>
        ) : (
          <Link to="/workout" className="accent-button">
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
        <Route path="/analytics" element={<AnalyticsPage />} />
      </Routes>
    </>
  )
}

export default App
