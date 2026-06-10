import { createContext, useContext, useMemo, useState, useEffect } from "react";
import { getToken, removeToken, saveToken, logout as apiLogout } from "../api/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(getToken());
  const [user, setUser] = useState(null);

  useEffect(() => {
    if (token) {
      import("../api/api").then(({ getCurrentUser }) => {
        const currentUser = getCurrentUser();
        setUser(currentUser);
      });
    } else {
      setUser(null);
    }
  }, [token]);

  const isAuthenticated = !!token;

  const login = (newToken) => {
    saveToken(newToken);
    setToken(newToken);
  };

  const logout = () => {
    apiLogout();
    setToken(null);
    setUser(null);
  };

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated,
      login,
      logout,
    }),
    [token, user, isAuthenticated]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}

export function getCurrentUser() {
  const token = getToken();
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return {
      id: payload.userId || payload.id,
      login: payload.sub,
      roles: payload.roles || [],
    };
  } catch {
    return null;
  }
}