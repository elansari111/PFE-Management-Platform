import { startVitest } from 'vitest/node'

// Filtre les options spécifiques à d'autres test runners (ex: --watchAll=false passé par GitHub Actions)
const args = process.argv.slice(2).filter((arg) => !arg.startsWith('--watchAll'))

const vitest = await startVitest('test', args, {
  watch: false,
  run: true,
})

if (vitest) {
  await vitest.close()
}
