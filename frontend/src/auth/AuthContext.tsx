import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type PropsWithChildren,
} from 'react';
import { api, TOKEN_KEY } from '../lib/api';
import type { UserDetails, UserSummary } from '../types';

interface AuthContextValue {
  user: UserSummary | null;
  loading: boolean;
  login: (cpf: string, password: string) => Promise<void>;
  logout: () => void;
  updateCurrentUser: (user: UserSummary) => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<UserSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) {
      setLoading(false);
      return;
    }
    api
      .get<UserDetails>('/users/me')
      .then(({ data }) => setUser(data))
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setLoading(false));
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      loading,
      login: async (cpf, password) => {
        const { data } = await api.post<{ token: string; user: UserSummary }>('/auth/login', {
          cpf,
          password,
        });
        localStorage.setItem(TOKEN_KEY, data.token);
        setUser(data.user);
      },
      logout: () => {
        localStorage.removeItem(TOKEN_KEY);
        setUser(null);
      },
      updateCurrentUser: setUser,
    }),
    [loading, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
