import { defineStore } from "pinia";
import { login as apiLogin, register as apiRegister } from "../api/auth";
import { isProfileCompleted } from "../api/profile";
import { decodeJwt } from "../api/http";

export const useAuth = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("token"),
    profileCompleted: false,
    initialized: false,
  }),
  getters: {
    isAuthenticated: (s) => !!s.token,
    claims: (s) => {
      try { return s.token ? decodeJwt(s.token) : {}; } catch { return {}; }
    },
    userId() {
      const c = this.claims;
      return c?.userId ?? c?.sub ?? null;
    },
    roles() {
      const c = this.claims;
      const set = new Set();
      if (c.role) set.add(String(c.role).toUpperCase());
      if (Array.isArray(c.roles)) c.roles.forEach(r => set.add(String(r).toUpperCase()));
      if (Array.isArray(c.authorities)) c.authorities.forEach(r => set.add(String(r).toUpperCase()));
      return Array.from(set);
    },
    isAdmin() {
      return this.roles.includes("ADMIN") || this.roles.includes("ROLE_ADMIN");
    },
    user() {
      const c = this.claims;
      if (!c || Object.keys(c).length === 0) return null;
      return { id: this.userId, email: c.email ?? null, name: c.name ?? null, ...c };
    },
  },
  actions: {
    // извикай я от router guard преди решенията за навигация
    async init() {
      if (this.initialized) return;

      if (this.isAuthenticated) {
        if (this.isAdmin) {
          this.profileCompleted = true;
        } else {
          // ако в токена има profileCompleted — вземи него
          const c = this.claims;
          if (typeof c?.profileCompleted === "boolean") {
            this.profileCompleted = c.profileCompleted;
          } else {
            // иначе – питай бекенда (връща boolean)
            try {
              this.profileCompleted = await isProfileCompleted();
            } catch {
              this.profileCompleted = false;
            }
          }
        }
      }

      this.initialized = true;
    },

    async login(payload) {
      const { data } = await apiLogin(payload);
      this.token = data?.token;
      localStorage.setItem("token", this.token);

      if (this.isAdmin) {
        this.profileCompleted = true;
      } else {
        const c = this.claims;
        if (typeof c?.profileCompleted === "boolean") {
          this.profileCompleted = c.profileCompleted;
        } else {
          try {
            this.profileCompleted = await isProfileCompleted(); 
          } catch {
            this.profileCompleted = false;
          }
        }
      }

      this.initialized = true;
    },

    async register(payload) {
      const { data } = await apiRegister(payload);
      this.token = data?.token;
      localStorage.setItem("token", this.token);
      this.profileCompleted = false;
      this.initialized = true;
    },

    logout() {
      this.token = null;
      this.profileCompleted = false;
      this.initialized = false;
      localStorage.removeItem("token");
    },
  },
});
