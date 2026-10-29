import AppRoutes from '../routes'
import { AuthProvider } from '../context'

export function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}

export default App
