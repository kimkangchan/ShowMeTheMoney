"use client";

import { createContext, useContext, useEffect, useState } from "react";
import api from "@/lib/api";
import { User } from "@/types";

interface AuthContextValue {
  user: User | null;
  isLoggedIn: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // TODO: 백엔드 연동 후 아래 목업 제거
    setUser({ uuid: 1, username: "user1", name: "김강찬", email: "test@test.com", grade: 1, createdAt: "" });
    setIsLoading(false);
    // api
    //   .get<{ data: User }>("/api/auth/me")
    //   .then((res) => setUser(res.data.data))
    //   .catch(() => setUser(null))
    //   .finally(() => setIsLoading(false));
  }, []);

  async function login(username: string, password: string) {
    const res = await api.post<{ data: User }>("/api/auth/login", {
      username,
      password,
    });
    setUser(res.data.data);
  }

  async function logout() {
    await api.post("/api/auth/logout");
    setUser(null);
  }

  return (
    <AuthContext.Provider
      value={{ user, isLoggedIn: !!user, isLoading, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
