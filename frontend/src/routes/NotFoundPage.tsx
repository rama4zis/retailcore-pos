import { useNavigate } from 'react-router-dom'

import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'

export function NotFoundPage() {
  const navigate = useNavigate()

  return (
    <main className="flex min-h-screen items-center justify-center px-4 py-8 text-rc-foreground">
      <Card
        className="max-w-lg"
        description="That route is not in the RetailCore POS map. Someone clipped out of bounds. Cute."
        title="Page not found"
      >
        <Button onClick={() => navigate('/dashboard')}>Return to dashboard</Button>
      </Card>
    </main>
  )
}
