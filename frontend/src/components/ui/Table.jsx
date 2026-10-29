export function Table({ children, className = '', ...props }) {
  return (
    <div className="w-full overflow-x-auto rounded-xl border border-[#E2E8F0] bg-white shadow-xs">
      <table className={`w-full text-left text-sm border-collapse ${className}`} {...props}>
        {children}
      </table>
    </div>
  )
}

export function TableHeader({ children, className = '', ...props }) {
  return (
    <thead className={`bg-[#F8FAFC] border-b border-[#E2E8F0] ${className}`} {...props}>
      {children}
    </thead>
  )
}

export function TableBody({ children, className = '', ...props }) {
  return (
    <tbody className={`divide-y divide-[#E2E8F0] bg-white ${className}`} {...props}>
      {children}
    </tbody>
  )
}

export function TableRow({ children, className = '', isClickable = false, ...props }) {
  return (
    <tr
      className={`transition-colors ${
        isClickable ? 'cursor-pointer hover:bg-slate-50/80 active:bg-slate-100' : 'hover:bg-slate-50/50'
      } ${className}`}
      {...props}
    >
      {children}
    </tr>
  )
}

export function TableHead({ children, className = '', ...props }) {
  return (
    <th
      scope="col"
      className={`px-4 sm:px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider ${className}`}
      {...props}
    >
      {children}
    </th>
  )
}

export function TableCell({ children, className = '', ...props }) {
  return (
    <td className={`px-4 sm:px-6 py-3.5 text-slate-700 whitespace-nowrap text-xs sm:text-sm ${className}`} {...props}>
      {children}
    </td>
  )
}

export function TableCaption({ children, className = '', ...props }) {
  return (
    <caption className={`p-4 text-xs text-slate-500 text-left bg-slate-50 border-t border-[#E2E8F0] ${className}`} {...props}>
      {children}
    </caption>
  )
}

export default Table

