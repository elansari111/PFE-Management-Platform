import { ArrowUpRight, ArrowDownRight, Minus } from 'lucide-react'

export function StatCard({
  title,
  value,
  icon: Icon,
  iconColor = 'text-[#1E3A8A]',
  iconBg = 'bg-blue-50',
  trend,
  trendDirection = 'neutral', // 'up' | 'down' | 'neutral'
  trendLabel,
  caption,
  className = '',
  onClick,
}) {
  const isClickable = Boolean(onClick)

  const trendColors = {
    up: 'text-emerald-700 bg-emerald-50 border-emerald-200',
    down: 'text-rose-700 bg-rose-50 border-rose-200',
    neutral: 'text-slate-600 bg-slate-50 border-slate-200',
  }

  const TrendIcon =
    trendDirection === 'up'
      ? ArrowUpRight
      : trendDirection === 'down'
      ? ArrowDownRight
      : Minus

  return (
    <div
      onClick={onClick}
      className={`bg-white border border-[#E2E8F0] rounded-xl p-5 sm:p-6 shadow-xs transition-all duration-200 ${
        isClickable ? 'cursor-pointer hover:shadow-md hover:border-slate-300 active:scale-[0.99]' : 'hover:shadow-sm'
      } ${className}`}
    >
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{title}</p>
          <p className="text-2xl sm:text-3xl font-extrabold text-[#1E3A8A] tracking-tight">{value}</p>
        </div>

        {Icon && (
          <div
            className={`w-12 h-12 rounded-xl flex items-center justify-center shrink-0 ${iconBg} ${iconColor} shadow-2xs`}
          >
            <Icon className="w-6 h-6" />
          </div>
        )}
      </div>

      {(trend || caption) && (
        <div className="mt-4 pt-3 border-t border-[#E2E8F0]/80 flex items-center justify-between text-xs">
          {trend && (
            <span
              className={`inline-flex items-center px-2 py-0.5 rounded-full font-semibold border ${
                trendColors[trendDirection] || trendColors.neutral
              }`}
            >
              <TrendIcon className="w-3.5 h-3.5 mr-0.5" />
              <span>{trend}</span>
            </span>
          )}

          {trendLabel && <span className="text-slate-500 ml-1.5">{trendLabel}</span>}

          {caption && <span className="text-slate-500 ml-auto">{caption}</span>}
        </div>
      )}
    </div>
  )
}

export default StatCard

