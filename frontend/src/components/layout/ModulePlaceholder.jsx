import { Construction, Sparkles, ArrowLeft } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Button } from '../ui/Button'
import { Badge } from '../ui/Badge'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../ui/Card'

export function ModulePlaceholder({
  title,
  description,
  phase = 'Phase 4',
  expectedFeatures = [],
  icon: Icon = Construction,
}) {
  return (
    <div className="space-y-6 max-w-4xl mx-auto py-6 animate-in fade-in duration-200">
      <div className="flex items-center space-x-2 text-xs text-slate-500">
        <Link to="/dashboard" className="hover:text-[#1E3A8A] flex items-center gap-1">
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Retour au Tableau de bord</span>
        </Link>
      </div>

      <Card className="border-slate-200">
        <CardHeader className="bg-slate-50/70 border-b border-[#E2E8F0]">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-center space-x-3">
              <div className="w-12 h-12 rounded-xl bg-blue-50 text-[#1E3A8A] border border-blue-100 flex items-center justify-center shrink-0">
                <Icon className="w-6 h-6" />
              </div>
              <div>
                <CardTitle className="text-xl text-[#1E3A8A]">{title}</CardTitle>
                <CardDescription>{description}</CardDescription>
              </div>
            </div>

            <Badge variant="accent" size="md">
              <Sparkles className="w-3.5 h-3.5 mr-1" />
              <span>{phase}</span>
            </Badge>
          </div>
        </CardHeader>

        <CardContent className="p-6 sm:p-8 space-y-6">
          <div className="rounded-xl p-4 bg-blue-50/50 border border-blue-100 text-xs text-blue-900 leading-relaxed">
            <strong>Note d&apos;avancement :</strong> Ce module fait partie de l&apos;architecture globale
            validée. L&apos;interface et les contrats API associés seront développés lors des étapes
            suivantes conformément à la feuille de route Agile de l&apos;équipe.
          </div>

          {expectedFeatures.length > 0 && (
            <div className="space-y-3">
              <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                Fonctionnalités planifiées pour ce module :
              </h4>
              <ul className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                {expectedFeatures.map((feat, idx) => (
                  <li
                    key={idx}
                    className="flex items-start space-x-2 text-xs text-slate-600 bg-white p-2.5 rounded-lg border border-[#E2E8F0]"
                  >
                    <span className="w-1.5 h-1.5 rounded-full bg-[#0DAD98] mt-1.5 shrink-0" />
                    <span>{feat}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div className="pt-4 border-t border-[#E2E8F0] flex items-center justify-between">
            <span className="text-xs text-slate-400">PFE Management Platform &bull; dev-simo</span>
            <Link to="/dashboard">
              <Button variant="primary" size="sm">
                Retour au Tableau de bord
              </Button>
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

export default ModulePlaceholder

