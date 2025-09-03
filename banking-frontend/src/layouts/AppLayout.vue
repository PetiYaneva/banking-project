<script setup>
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useAuth } from "../stores/auth";

const auth = useAuth();
const route = useRoute();

const items = [
  { to: "/dashboard", label: "Dashboard", icon: "🏠" },
  { to: "/accounts", label: "Accounts", icon: "💳" },
  { to: "/transfer", label: "Transfer", icon: "🔁" },
  { to: "/transactions", label: "Transactions", icon: "📄" },
  { to: "/loans", label: "Loans", icon: "📈" },
  { to: "/crypto/live", label: "Crypto Live", icon: "📡" },
  { to: "/crypto/trade", label: "Crypto Trade", icon: "🪙" },
  { to: "/crypto/portfolio", label: "Portfolio", icon: "🧺" },
  { to: "/reports", label: "Reports", icon: "📊" },
];

const isActive = (to) => computed(() => {
  // активен ако: точен мач или текущият път започва с to + "/"
  return route.path === to || route.path.startsWith(to + "/");
});

const mobileOpen = ref(false);
const toggleMobile = () => (mobileOpen.value = !mobileOpen.value);
const closeMobile = () => (mobileOpen.value = false);
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <!-- Topbar -->
    <header class="sticky top-0 z-40 bg-white/70 backdrop-blur border-b border-black/5 shadow-[0_1px_0_#0000000d]">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between">
        <router-link to="/dashboard" class="text-lg font-semibold tracking-tight hover:opacity-80">
          BankingApp
        </router-link>

        <!-- desktop nav -->
        <div class="hidden md:flex items-center gap-5 text-[15px]">
          <template v-if="!auth.profileCompleted">
            <router-link class="link" to="/welcome">Начало</router-link>
            <router-link class="link" to="/complete-profile">Завърши профила</router-link>
          </template>
          <template v-else>
            <router-link class="link" to="/dashboard">Dashboard</router-link>
            <router-link class="link" to="/accounts">Accounts</router-link>
            <router-link class="link" to="/transfer">Transfer</router-link>
            <router-link class="link" to="/transactions">Transactions</router-link>
            <router-link class="link" to="/loans">Loans</router-link>
            <router-link class="link" to="/reports">Reports</router-link>
            <router-link v-if="auth.isAdmin" class="link" to="/admin">Admin</router-link>
          </template>
        </div>

        <div class="flex items-center gap-2">
          <router-link v-if="!auth.isAuthenticated" to="/login" class="btn-outline">Login</router-link>
          <button v-else class="btn-solid" @click="auth.logout()">Logout</button>

          <!-- mobile toggle -->
          <button class="md:hidden px-3 py-2 rounded-lg border border-gray-300" @click="toggleMobile">Menu</button>
        </div>
      </div>

      <!-- mobile dropdown -->
      <div v-if="mobileOpen" class="md:hidden border-t bg-white">
        <div class="max-w-7xl mx-auto px-4 py-3 flex flex-col gap-2">
          <template v-if="!auth.profileCompleted">
            <router-link class="link" to="/welcome" @click="closeMobile">Начало</router-link>
            <router-link class="link" to="/complete-profile" @click="closeMobile">Завърши профила</router-link>
          </template>
          <template v-else>
            <router-link v-for="it in items" :key="it.to" class="link" :to="it.to" @click="closeMobile">
              {{ it.label }}
            </router-link>
            <router-link v-if="auth.isAdmin" class="link" to="/admin" @click="closeMobile">Admin</router-link>
          </template>
        </div>
      </div>
    </header>

    <div class="mx-auto max-w-7xl px-4 sm:px-6 py-6 grid grid-cols-12 gap-4 lg:gap-6">
      <!-- Sidebar (скрит ако няма профил) -->
      <aside v-if="auth.profileCompleted" class="col-span-12 md:col-span-3 lg:col-span-2">
        <nav class="bg-white border rounded-2xl p-3">
          <router-link
            v-for="it in items" :key="it.to" :to="it.to"
            class="flex items-center justify-between px-3 py-2 rounded-lg mb-1 hover:bg-slate-50"
            :class="isActive(it.to)() ? 'bg-slate-100 font-medium' : ''"
          >
            <div class="flex items-center gap-2">
              <span class="text-base">{{ it.icon }}</span>
              <span class="text-sm">{{ it.label }}</span>
            </div>
          </router-link>
        </nav>
      </aside>

      <!-- Main -->
      <main :class="auth.profileCompleted ? 'col-span-12 md:col-span-9 lg:col-span-10' : 'col-span-12'">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.link { @apply text-gray-700 hover:text-gray-900 transition-colors; }
.btn-solid { @apply px-4 py-2 rounded-xl bg-black text-white hover:bg-gray-900 transition-colors; }
.btn-outline { @apply px-4 py-2 rounded-xl border border-gray-300 hover:bg-gray-100 transition-colors; }
</style>
