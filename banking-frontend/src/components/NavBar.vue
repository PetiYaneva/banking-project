<template>
  <nav class="sticky top-0 z-40 bg-white/70 backdrop-blur border-b border-black/5 shadow-[0_1px_0_#0000000d]">
    <div class="max-w-6xl mx-auto px-6 h-14 flex items-center justify-between">
      <router-link to="/" class="text-lg font-semibold tracking-tight hover:opacity-80">BankingApp</router-link>

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
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useAuth } from "../stores/auth";
const auth = useAuth();
</script>

<style scoped>
.link { @apply text-gray-700 hover:text-gray-900 transition-colors; }
.btn-solid { @apply px-4 py-2 rounded-xl bg-black text-white hover:bg-gray-900 transition-colors; }
.btn-outline { @apply px-4 py-2 rounded-xl border border-gray-300 hover:bg-gray-100 transition-colors; }
</style>
