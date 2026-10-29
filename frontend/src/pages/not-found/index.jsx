import { useNavigate } from 'react-router-dom'
import { FileQuestion, ArrowLeft, Home } from 'lucide-react'
import { Button } from '../../components/ui/Button'

export function NotFoundPage() {
  const navigate = useNavigate()

  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center text-center p-6 animate-in fade-in zoom-in-95 duration-200">
      <div className="w-20 h-20 rounded-3xl bg-blue-50 text-[#1E3A8A] border border-blue-100 flex items-center justify-center mb-6 shadow-sm">
        <FileQuestion className="w-10 h-10" />
      </div>

      <span className="text-xs font-bold uppercase tracking-wider text-[#0DAD98] bg-emerald-50 px-3 py-1 rounded-full border border-emerald-200">
        Erreur 404
      </span>

      <h1 className="text-2xl sm:text-3xl font-extrabold text-[#1E3A8A] mt-3">
        Page Introuvable
      </h1>

      <p className="text-sm text-slate-500 max-w-md mt-2 leading-relaxed">
        Désolé, l&apos;adresse demandée n&apos;existe pas ou a été déplacée. Vérifiez le chemin ou
        revenez vers une page connue.
      </p>

      <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
        <Button
          variant="outline"
          size="sm"
          onClick={() => navigate(-1)}
          leftIcon={<ArrowLeft className="w-4 h-4" />}
        >
          Page précédente
        </Button>
        <Button
          variant="primary"
          size="sm"
          onClick={() => navigate('/dashboard')}
          leftIcon={<Home className="w-4 h-4" />}
        >
          Retour au Tableau de bord
        </Button>
      </div>
    </div>
  )
}

export default NotFoundPage

