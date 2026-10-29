import { forwardRef } from 'react'
import { Spinner } from './Spinner'

export const Button = forwardRef(
  (
    {
      children,
      variant = 'primary',
      size = 'md',
      isLoading = false,
      disabled = false,
      leftIcon = null,
      rightIcon = null,
      type = 'button',
      className = '',
      ...props
    },
    ref
  ) => {
    const baseStyles =
      'inline-flex items-center justify-center font-medium rounded-lg transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed select-none'

    const variantStyles = {
      primary:
        'bg-[#1E3A8A] text-white hover:bg-[#172554] active:bg-[#0f172a] focus:ring-[#1E3A8A] shadow-sm',
      secondary:
        'bg-slate-100 text-slate-800 hover:bg-slate-200 active:bg-slate-300 focus:ring-slate-400',
      accent:
        'bg-[#0DAD98] text-white hover:bg-[#0b8f7d] active:bg-[#097364] focus:ring-[#0DAD98] shadow-sm',
      danger:
        'bg-rose-600 text-white hover:bg-rose-700 active:bg-rose-800 focus:ring-rose-500 shadow-sm',
      outline:
        'border border-[#E2E8F0] bg-white text-slate-700 hover:bg-slate-50 hover:border-slate-300 focus:ring-[#1E3A8A] shadow-xs',
      ghost:
        'text-slate-600 hover:bg-slate-100 hover:text-slate-900 active:bg-slate-200 focus:ring-slate-300',
    }

    const sizeStyles = {
      sm: 'text-xs px-2.5 py-1.5 gap-1.5',
      md: 'text-sm px-4 py-2 gap-2',
      lg: 'text-base px-5 py-2.5 gap-2.5',
    }

    return (
      <button
        ref={ref}
        type={type}
        disabled={disabled || isLoading}
        className={`${baseStyles} ${variantStyles[variant] || variantStyles.primary} ${sizeStyles[size] || sizeStyles.md} ${className}`}
        {...props}
      >
        {isLoading ? (
          <Spinner
            size={size === 'lg' ? 'md' : 'sm'}
            color={variant === 'outline' || variant === 'ghost' || variant === 'secondary' ? 'primary' : 'white'}
          />
        ) : (
          leftIcon && <span className="inline-flex shrink-0">{leftIcon}</span>
        )}
        <span>{children}</span>
        {!isLoading && rightIcon && <span className="inline-flex shrink-0">{rightIcon}</span>}
      </button>
    )
  }
)

Button.displayName = 'Button'

export default Button

