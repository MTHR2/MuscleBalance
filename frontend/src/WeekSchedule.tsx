import { WEEKDAYS } from './weekdays'
import './WeekSchedule.css'

export type ScheduleDay = { dayOfWeek: number; workoutTemplateName: string }

// неделя Пн–Вс: тренировка в день или «Отдых»; onRemove — кнопка «×» (только при редактировании)
function WeekSchedule({ days, onRemove }: { days: ScheduleDay[]; onRemove?: (dayOfWeek: number) => void }) {
  return (
    <ul className="week">
      {WEEKDAYS.map(({ day, full }) => {
        const entry = days.find((d) => d.dayOfWeek === day)
        return (
          <li key={day} className={`week__day${entry ? '' : ' week__day--rest'}`}>
            <span className="week__label">{full}</span>
            <span className="week__workout">{entry ? entry.workoutTemplateName : 'Отдых'}</span>
            {entry && onRemove && (
              <button
                type="button"
                className="week__remove"
                onClick={() => onRemove(day)}
                aria-label={`Убрать тренировку: ${full}`}
              >
                ×
              </button>
            )}
          </li>
        )
      })}
    </ul>
  )
}

export default WeekSchedule
