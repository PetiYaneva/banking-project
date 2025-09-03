<template>
  <header class="sticky top-0 z-40 bg-white/90 backdrop-blur border-b">
    <div class="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
      <RouterLink to="/" class="font-semibold">Banking</RouterLink>

      <div class="flex items-center gap-3" v-if="auth.isAuthenticated">
        <span
          class="inline-flex items-center gap-2 text-xs px-2.5 py-1 rounded-full border"
          :class="auth.profileCompleted
            ? 'text-emerald-700 border-emerald-300 bg-emerald-50'
            : 'text-amber-700 border-amber-300 bg-amber-50'">
          <span class="w-1.5 h-1.5 rounded-full"
                :class="auth.profileCompleted ? 'bg-emerald-600' : 'bg-amber-600'"></span>
          {{ auth.profileCompleted ? 'Profile complete' : 'Profile incomplete' }}
        </span>

        <span v-if="auth.isAdmin"
              class="inline-flex items-center gap-2 text-xs px-2.5 py-1 rounded-full border text-sky-700 border-sky-300 bg-sky-50">
          ADMIN
        </span>

        <button
          @click="onLogout"
          class="inline-flex items-center gap-2 h-9 px-4 rounded-full bg-slate-900 text-white
                  hover:bg-slate-800 active:bg-slate-900 shadow-sm
                  focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400/50
                  transition"
          aria-label="Logout">
          <span class="text-sm font-medium">Logout</span>
        </button>
      </div>

      <div v-else class="flex items-center gap-2">
        <RouterLink class="text-sm px-3 py-1.5 rounded hover:bg-slate-100" to="/login">Login</RouterLink>
        <RouterLink class="text-sm px-3 py-1.5 rounded hover:bg-slate-100" to="/register">Register</RouterLink>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter, RouterLink } from "vue-router";
import { useAuth } from "../stores/auth";

const auth = useAuth();
const router = useRouter();

function onLogout() {
  auth.logout();
  router.replace({ name: "login" });
}
</script>
