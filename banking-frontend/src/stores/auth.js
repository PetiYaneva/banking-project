// src/stores/auth.js
import { defineStore } from "pinia";
import { login as apiLogin, register as apiRegister } from "../api/auth";
import { isProfileCompleted } from "../api/profile";

export const useAuth = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("token"),
    profileCompleted: false,
    initialized: false,
  }),

  getters: {
    isAuthenticated: (s) => !!s.token,
  },

  actions: {
    setToken(t) {
      this.token = t || null;
      if (t) localStorage.setItem("token", t);
      else localStorage.removeItem("token");
    },

    async bootstrap() {
      if (!this.token) { this.initialized = true; return; }
      try {
        this.profileCompleted = await isProfileCompleted();
      } catch {
        this.setToken(null);
        this.profileCompleted = false;
      } finally {
        this.initialized = true;
      }
    },

    async login(credentials) {
      // динамичен импорт на рутера, за да няма цикъл
      const { default: router } = await import("../router");
      try {
        const { data } = await apiLogin(credentials);
        this.setToken(data.token);
        try { this.profileCompleted = await isProfileCompleted(); }
        catch { this.profileCompleted = false; }
        await router.replace({ name: this.profileCompleted ? "dashboard" : "before" });
      } catch (e) {
        this.setToken(null);
        this.profileCompleted = false;
        throw e;
      }
    },

    async register(payload) {
      const { default: router } = await import("../router");
      try {
        const { data } = await apiRegister(payload);
        this.setToken(data.token);
        try { this.profileCompleted = await isProfileCompleted(); }
        catch { this.profileCompleted = false; }
        await router.replace({ name: "before" });
      } catch (e) {
        this.setToken(null);
        this.profileCompleted = false;
        throw e;
      }
    },

    async refreshCompleted() {
      try { this.profileCompleted = await isProfileCompleted(); }
      catch {}
    },

    async logout() {
      const { default: router } = await import("../router");
      this.setToken(null);
      this.profileCompleted = false;
      await router.replace({ name: "landing" });
    },
  },
});
