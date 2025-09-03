<script setup>
import { ref, computed } from "vue";
import { useAuth } from "../stores/auth";

const auth = useAuth();

const cryptoOpen = ref(false);
function toggleCrypto() {
  cryptoOpen.value = !cryptoOpen.value;
}
function closeCrypto() {
  cryptoOpen.value = false;
}

const isAdmin = computed(() => {
  const u = auth.user || {};
  const r =
    u.role ??
    (Array.isArray(u.roles) && u.roles[0]) ??
    (Array.isArray(u.authorities) && u.authorities[0]?.authority) ??
    "";
  return String(r).toUpperCase().includes("ADMIN");
});

</script>

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

          <div class="relative">
            <button class="link inline-flex items-center gap-1" @click="toggleCrypto">
              Crypto
              <svg width="14" height="14" viewBox="0 0 20 20"><path d="M5 7l5 5 5-5" fill="currentColor"/></svg>
            </button>

            <div
              v-show="cryptoOpen"
              class="absolute left-0 mt-2 bg-white border rounded-xl shadow p-2 min-w-[180px] z-50"
            >
              <router-link
                class="block px-3 py-2 hover:bg-gray-100 rounded-md"
                :to="{ name:'crypto-trade' }"
                @click="closeCrypto"
              >
                Sell / Buy
              </router-link>

              <router-link
                class="block px-3 py-2 hover:bg-gray-100 rounded-md"
                :to="{ name:'crypto-wallet' }"
                @click="closeCrypto"
              >
                Crypto wallet
              </router-link>

              <router-link
                class="block px-3 py-2 hover:bg-gray-100 rounded-md"
                :to="{ name:'crypto-live' }"
                @click="closeCrypto"
              >
                Live crypto
              </router-link>
            </div>
          </div>
        </template>

         <RouterLink
      v-if="auth.isAdmin"
      to="/admin/dashboard"
      class="hover:underline"
    >
      Admin
    </RouterLink>

        <div class="flex items-center gap-2">
          <router-link v-if="!auth.isAuthenticated" to="/login" class="btn-outline">Login</router-link>
          <button v-else class="btn-solid" @click="auth.logout()">Logout</button>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.link { @apply text-gray-700 hover:text-gray-900 transition-colors; }
.btn-solid { @apply px-4 py-2 rounded-xl bg-black text-white hover:bg-gray-900 transition-colors; }
.btn-outline { @apply px-4 py-2 rounded-xl border border-gray-300 hover:bg-gray-100 transition-colors; }
</style>
