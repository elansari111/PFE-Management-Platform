export function Skeleton({ className = '', ...props }) {
  return (
    <div
      className={`animate-pulse rounded-md bg-slate-200/80 ${className}`}
      {...props}
    />
  )
}

export function SkeletonText({ lines = 3, className = '' }) {
  return (
    <div className={`space-y-2.5 ${className}`}>
      {Array.from({ length: lines }).map((_, index) => (
        <Skeleton
          key={index}
          className={`h-3.5 ${
            index === lines - 1 ? 'w-3/5' : index % 2 === 0 ? 'w-full' : 'w-4/5'
          }`}
        />
      ))}
    </div>
  )
}

export function SkeletonCard({ className = '' }) {
  return (
    <div className={`bg-white border border-[#E2E8F0] rounded-xl p-5 space-y-4 shadow-xs ${className}`}>
      <div className="flex items-center justify-between">
        <Skeleton className="h-4 w-1/3" />
        <Skeleton className="h-10 w-10 rounded-xl" />
      </div>
      <Skeleton className="h-8 w-1/2" />
      <Skeleton className="h-3 w-4/5" />
    </div>
  )
}

export function SkeletonAvatar({ size = 'md', className = '' }) {
  const sizeClasses = {
    sm: 'w-8 h-8',
    md: 'w-10 h-10',
    lg: 'w-12 h-12',
  }
  return <Skeleton className={`rounded-full shrink-0 ${sizeClasses[size] || sizeClasses.md} ${className}`} />
}

export default Skeleton

