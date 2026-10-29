/**
 * Spinner Component - Indicateur de chargement
 */
export function Spinner({ size = 'md', color = 'primary', className = '' }) {
  const sizeClasses = {
    xs: 'w-3 h-3 border-2',
    sm: 'w-4 h-4 border-2',
    md: 'w-6 h-6 border-2',
    lg: 'w-8 h-8 border-3',
    xl: 'w-12 h-12 border-4',
  }

  const colorClasses = {
    primary: 'border-slate-200 border-t-[#1E3A8A]',
    accent: 'border-slate-200 border-t-[#0DAD98]',
    white: 'border-white/30 border-t-white',
    slate: 'border-slate-200 border-t-slate-600',
  }

  return (
    <div
      role="status"
      aria-label="Chargement"
      className={`inline-block animate-spin rounded-full ${sizeClasses[size] || sizeClasses.md} ${colorClasses[color] || colorClasses.primary} ${className}`}
    >
      <span className="sr-only">Chargement...</span>
    </div>
  )
}

export default Spinner

