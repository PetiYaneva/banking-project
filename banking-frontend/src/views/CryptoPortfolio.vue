<!-- src/views/CryptoWallet.vue -->
<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuth } from "../stores/auth";
import { getPortfolioByUser } from "../api/cryptoTrade";
import { getUserAccounts } from "../api/account"; // само за да вземем едната сметка при redirect към Купи

const router = useRouter();
const auth = useAuth();
const userId = computed(() => auth.user?.id || auth.userId);

const loading = ref(false);
const vs = ref("usd");
const portfolio = ref([]);
const message = ref("");
const defaultAccountId = ref(""); // ще го ползваме за прехвърляне към Купи

async function loadDefaultAccount() {
  // намираме първата сметка (за да я подадем при "Купи")
  const { data } = await getUserAccounts(userId.value);
  const list = data || [];
  defaultAccountId.value = list[0]?.id || "";
}

async function loadPortfolio() {
  if (!userId.value) return;
  loading.value = true;
  message.value = "";
  try {
    const { data } = await getPortfolioByUser(userId.value, vs.value);
    portfolio.value = Array.isArray(data) ? data : (data?.positions || []);
  } catch (e) {
    message.value = e?.response?.data?.message || "Грешка при зареждане на портфейла.";
  } finally {
    loading.value = false;
  }
}

function goBuy() {
  router.push({
    name: "CryptoTrade",
    query: { mode: "buy", accountId: defaultAccountId.value || "" },
  });
}

function goSell(sym) {
  router.push({
    name: "CryptoTrade",
    query: { mode: "sell", sym, accountId: defaultAccountId.value || "" },
  });
}

onMounted(async () => {
  await loadDefaultAccount();
  await loadPortfolio();
});
</script>

<template>
  <div class="p-5 space-y-6">
    <h1 class="text-2xl font-semibold">Крипто портфейл</h1>

    <section class="bg-white border rounded-2xl p-4">
      <div class="grid sm:grid-cols-3 gap-3">
        <div>
          <label class="text-sm text-slate-600">Показвай в</label>
          <select v-model="vs" @change="loadPortfolio" class="mt-1 w-full rounded-md border px-3 py-2">
            <option value="usd">USD</option>
            <option value="bgn">BGN</option>
            <option value="eur">EUR</option>
          </select>
        </div>
        <div class="flex items-end">
          <button class="rounded-md bg-emerald-600 text-white px-4 py-2 hover:bg-emerald-700" @click="goBuy">
            Купи криптовалута
          </button>
        </div>
      </div>
    </section>

    <section class="bg-white border rounded-2xl p-4">
      <div v-if="loading">Зареждане...</div>
      <div v-else>
        <div v-if="!portfolio || portfolio.length === 0" class="text-center py-8">
          <p class="text-slate-600">Все още нямате закупени криптовалути.</p>
          <button class="mt-4 rounded-md bg-indigo-600 text-white px-4 py-2 hover:bg-indigo-700" @click="goBuy">
            Купи сега
          </button>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead>
              <tr class="text-left text-slate-600 border-b">
                <th class="py-2 pr-4">Крипто</th>
                <th class="py-2 pr-4 text-right">Количество</th>
                <th class="py-2 pr-4 text-right">Средна цена</th>
                <th class="py-2 pr-4 text-right">Стойност ({{ vs.toUpperCase() }})</th>
                <th class="py-2 pr-4"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in portfolio" :key="p.symbol || p.sym" class="border-b">
                <td class="py-2 pr-4 font-medium">{{ p.symbol || p.sym }}</td>
                <td class="py-2 pr-4 text-right">{{ Number(p.quantity ?? p.qty ?? 0).toFixed(8) }}</td>
                <td class="py-2 pr-4 text-right">{{ Number(p.avgPrice ?? 0).toFixed(2) }}</td>
                <td class="py-2 pr-4 text-right">{{ Number(p.currentValue ?? p.value ?? 0).toFixed(2) }}</td>
                <td class="py-2 pr-4 text-right">
                  <button class="rounded-md bg-slate-800 text-white px-3 py-1.5 hover:bg-slate-900"
                          @click="goSell(p.symbol || p.sym)">
                    Продай
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <p v-if="message" class="mt-3 text-rose-600">{{ message }}</p>
      </div>
    </section>
  </div>
</template>
