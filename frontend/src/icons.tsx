// тонкие контурные иконки из дизайна; цвет берут из currentColor
type IconProps = { size?: number; filled?: boolean }

const base = (size: number) => ({
  width: size,
  height: size,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 2.4,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
  'aria-hidden': true,
})

export const PlayIcon = ({ size = 18 }: IconProps) => (
  <svg {...base(size)}>
    <path d="M7 4v16l13-8z" />
  </svg>
)

export const PlusIcon = ({ size = 22 }: IconProps) => (
  <svg {...base(size)} strokeWidth={2.8}>
    <path d="M12 5v14M5 12h14" />
  </svg>
)

export const CheckIcon = ({ size = 18 }: IconProps) => (
  <svg {...base(size)} strokeWidth={2.6}>
    <path d="M5 12l5 5 9-10" />
  </svg>
)

export const HeartIcon = ({ size = 20, filled = false }: IconProps) => (
  <svg {...base(size)} strokeWidth={2.2} fill={filled ? 'currentColor' : 'none'}>
    <path d="M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10z" />
  </svg>
)

export const SearchIcon = ({ size = 18 }: IconProps) => (
  <svg {...base(size)} strokeWidth={2.2}>
    <circle cx="11" cy="11" r="7" />
    <path d="M20 20l-3.5-3.5" />
  </svg>
)

export const ChevronIcon = ({ size = 18 }: IconProps) => (
  <svg {...base(size)}>
    <path d="M6 9l6 6 6-6" />
  </svg>
)
