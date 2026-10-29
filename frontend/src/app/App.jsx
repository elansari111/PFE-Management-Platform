import AppRoutes from '../routes'
import { AuthProvider } from '../context'
import { ToastProvider } from '../components/ui'

export function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </ToastProvider>
  )
}

export default App
