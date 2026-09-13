import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ArrowRight, LockKeyhole, ShieldCheck } from 'lucide-react'
import { useAuth } from '@/auth/AuthContext'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { formatCpf, getErrorMessage } from '@/lib/utils'
import { Alert, Spinner } from '@/components/ui/feedback'
import styles from './styles.module.css'

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [cpf, setCpf] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (user) return <Navigate to={user.role === 'ADMIN' ? '/users' : `/users/${user.id}`} replace />

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(cpf, password)
      const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname
      navigate(from || '/users', { replace: true })
    } catch (exception) {
      setError(getErrorMessage(exception))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <img src="/solution-logo.svg" alt="Solution" className={styles.logo} />
        <div className={styles.heroContent}>
          <span className={styles.eyebrow}>Gestão segura</span>
          <h1>Dados organizados.<br />Decisões mais simples.</h1>
          <p>Centralize usuários e endereços com segurança, consistência e consulta inteligente de CEP.</p>
          <div className={styles.feature}><ShieldCheck /><span><strong>Acesso por perfil</strong>Permissões garantidas pela API</span></div>
          <div className={styles.feature}><LockKeyhole /><span><strong>Dados protegidos</strong>Autenticação com token e senha criptografada</span></div>
        </div>
        <small>Desafio técnico · Desenvolvimento web</small>
      </section>
      <main className={styles.panel}>
        <form className={styles.form} onSubmit={submit}>
          <div><span className={`${styles.eyebrow} ${styles.eyebrowBrand}`}>Bem-vindo</span><h2>Acesse sua conta</h2><p>Use CPF e senha cadastrados.</p></div>
          {error && <Alert>{error}</Alert>}
          <Input label="CPF" name="cpf" inputMode="numeric" autoComplete="username" placeholder="000.000.000-00" value={cpf} onChange={(e) => setCpf(formatCpf(e.target.value))} required />
          <Input label="Senha" name="password" type="password" autoComplete="current-password" placeholder="Sua senha" value={password} onChange={(e) => setPassword(e.target.value)} required />
          <Button type="submit" disabled={loading} className={styles.submit}>
            {loading ? <Spinner light /> : <>Entrar <ArrowRight size={18} /></>}
          </Button>
          <p className={styles.help}>Primeiro acesso administrativo: <code>529.982.247-25</code> / <code>Admin@123</code></p>
        </form>
      </main>
    </div>
  )
}
