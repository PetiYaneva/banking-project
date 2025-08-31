<script setup>
import { ref, computed, onMounted } from "vue";
import { useAuth } from "../stores/auth";
import { getUserAccounts, getUserTotalBalance } from "../api/account";
import { getUserTransactions } from "../api/transactions";
import { getSimplePrices } from "../api/crypto";
import { CRYPTO_ASSETS } from "../constants/crypto";
import { getLoansByUser } from "../api/loans";

const auth = useAuth();
const userId = computed(() => auth.user?.id || auth.userId);

const loading = ref(true);
const accounts = ref([]);
const totalBalance = ref(0);
const lastTx = ref([]);               
const nextInstallment = ref(null);   
const inc6 = ref([]);                
const exp6 = ref([]);                
const cryptoMini = ref([]);           
const riskLevel = ref("Low");        

function pickTotal(resp) {
  const d = resp?.data;
  if (d == null) return 0;
  if (typeof d === "number") return d;
  if (typeof d === "string") return Number(d) || 0;
  if (typeof d === "object") return Number(d.total ?? d.balance ?? d.sum ?? 0);
  return 0;
}

function pickAccounts(resp) {
  const d = resp?.data;
  const arr = Array.isArray(d) ? d : (d?.content ?? d?.items ?? []);
  return arr.map(a => ({
    id: a.id ?? a.accountId ?? a.uuid,
    type: a.accountType ?? a.type ?? "ACCOUNT",
    iban: a.iban ?? a.ibanNumber ?? a.accountIban ?? "",
    balance: Number(a.balance ?? a.currentBalance ?? a.amount ?? 0),
    currency: a.currencyCode ?? a.currency ?? "BGN",
  }));
}

function pickTransactions(resp) {
  const d = resp?.data;
  const arr = Array.isArray(d) ? d : (d?.content ?? d?.items ?? []);
  return arr.map(t => ({
    id: t.id ?? t.txId,
    date: t.date ?? t.timestamp ?? t.createdAt ?? "",
    amount: Number(t.amount ?? t.value ?? 0),
    description: t.description ?? t.note ?? "",
    type: t.type ?? t.category ?? "OTHER",
    accountIban: t.accountIban ?? t.iban ?? "",
  }));
}

function toBars(values) {
  const w = 240, h = 72, gap = 6;
  const n = Math.max(values?.length || 0, 1);
  const max = Math.max(...values, 1);
  const barW = Math.max((w - gap * (n - 1)) / n, 8);
  return values.map((v, i) => {
    const hh = Math.round((v / max) * (h - 8));
    return { x: i * (barW + gap), y: h - hh, w: barW, h: hh };
  });
}

async function load() {
  if (!userId.value) return;
  loading.value = true;
  try {
    const [accRes, balRes] = await Promise.all([
      getUserAccounts(userId.value),
      getUserTotalBalance(userId.value),
    ]);

    accounts.value = pickAccounts(accRes);
    totalBalance.value = pickTotal(balRes);

    try {
      const txRes = await getUserTransactions(userId.value);
      lastTx.value = pickTransactions(txRes).slice(0, 5);
    } catch { lastTx.value = []; }

    nextInstallment.value = null;

    // Зареждаме mini цените в режим top3 (BTC, ETH, BNB)
    await loadCryptoMini("top3");

    if (!inc6.value.length) inc6.value = [0, 0, 0, 0, 0, 0];
    if (!exp6.value.length) exp6.value = [0, 0, 0, 0, 0, 0];

    const hasCredit = accounts.value.some(a => String(a.type).toUpperCase().includes("CREDIT"));
    riskLevel.value = hasCredit ? "Medium" : "Low";
  } finally {
    loading.value = false;
  }
}

// ЗАМЕНИ цялата функция с тази
async function loadCryptoMini(mode = "top3", dynamic = []) {
  cryptoMini.value = [];
  try {
    // 1) idsCsv според режима
    const idBySym = Object.fromEntries(CRYPTO_ASSETS.map(a => [a.sym, a.id]));
    let idsCsv = "";

    if (mode === "top3") {
      idsCsv = CRYPTO_ASSETS.slice(0, 3).map(c => c.id).join(","); // bitcoin,ethereum,binancecoin
    } else if (mode === "top20") {
      idsCsv = CRYPTO_ASSETS.map(c => c.id).join(",");
    } else if (mode === "dynamic") {
      const pickedIds = (dynamic || [])
        .map(x => String(x).trim())
        .map(x => idBySym[x.toUpperCase()] || x.toLowerCase())
        .filter(Boolean);
      idsCsv = pickedIds.join(",");
    }

    // 2) Викаме ендпойнта /api/crypto/simple-price?ids=...&vs=usd
    const resp = await getSimplePrices(idsCsv, "usd");
    const d = resp?.data;

    // 3) Мап обратно към sym (BTC/ETH/BNB) и цена в USD
    const symById = Object.fromEntries(CRYPTO_ASSETS.map(a => [a.id, a.sym]));
    const arr = [];

    if (Array.isArray(d)) {
      // формат: [{ id, prices: { usd } }, ...]
      for (const row of d) {
        const id = row?.id;
        if (!id) continue;
        const sym = symById[id] || id.toUpperCase();
        const price = Number(row?.prices?.usd ?? row?.usd ?? 0);
        arr.push({ sym, price, change: 0 });
      }
    } else if (d && typeof d === "object") {
      // формат: { id: { usd }, ... }
      for (const [id, obj] of Object.entries(d)) {
        const sym = symById[id] || id.toUpperCase();
        const price = Number(obj?.usd ?? 0);
        arr.push({ sym, price, change: 0 });
      }
    }

    // 4) Редът да следва заявката
    const idsOrder = idsCsv ? idsCsv.split(",") : [];
    if (idsOrder.length) {
      const rank = Object.fromEntries(idsOrder.map((id, i) => [id, i]));
      arr.sort((a, b) => (rank[idBySym[a.sym] || ""] ?? 0) - (rank[idBySym[b.sym] || ""] ?? 0));
    }

    cryptoMini.value = mode === "top3" ? arr.slice(0, 3) : arr;
  } catch {
    cryptoMini.value = [];
  }
}

onMounted(load);

// по желание да можеш да презареждаш мини списъка отвън
defineExpose({ load, loadCryptoMini, toBars });
</script>

<template>
  <div class="space-y-6">
    <!-- KPI Row -->
    <section class="grid grid-cols-12 gap-4">
      <div class="col-span-12 md:col-span-4">
        <div class="bg-white border rounded-2xl p-5">
          <div class="text-slate-500 text-xs">Общ баланс</div>
          <div class="text-2xl font-semibold tabular-nums mt-1">
            {{ loading ? '—' : totalBalance.toLocaleString('bg-BG') }} $
          </div>
          <div class="text-xs text-slate-500 mt-2">Сметки: {{ accounts.length }}</div>
          <div class="mt-3 flex gap-2">
            <RouterLink to="/transfer" class="px-3 py-1.5 rounded-md border hover:bg-slate-50 text-sm">Нов превод</RouterLink>
            <RouterLink to="/accounts" class="px-3 py-1.5 rounded-md border hover:bg-slate-50 text-sm">Нова сметка</RouterLink>
          </div>
        </div>
      </div>

      <div class="col-span-12 md:col-span-4">
        <div class="bg-white border rounded-2xl p-5">
          <div class="text-slate-500 text-xs">Следваща вноска по кредит</div>
          <div class="mt-1">
            <div v-if="nextInstallment" class="text-2xl font-semibold tabular-nums">
              {{ (nextInstallment.amount||0).toLocaleString('bg-BG') }} $
            </div>
            <div v-else class="text-2xl font-semibold">—</div>
          </div>
          <div class="text-xs text-slate-500 mt-2">
            Дата: {{ nextInstallment?.date || '—' }}
          </div>
          <RouterLink v-if="nextInstallment" to="/loans" class="mt-3 inline-block text-sm underline decoration-dotted">
            Виж кредитите
          </RouterLink>
        </div>
      </div>

      <div class="col-span-12 md:col-span-4">
        <div class="bg-white border rounded-2xl p-5">
          <div class="text-slate-500 text-xs">Риск ниво</div>
          <div
            class="inline-flex items-center gap-2 mt-2 text-sm px-2.5 py-1 rounded-full border"
            :class="riskLevel==='High' ? 'text-rose-700 border-rose-300 bg-rose-50'
                  : riskLevel==='Medium' ? 'text-amber-700 border-amber-300 bg-amber-50'
                  : 'text-emerald-700 border-emerald-300 bg-emerald-50'">
            <span class="w-1.5 h-1.5 rounded-full"
              :class="riskLevel==='High' ? 'bg-rose-500' : riskLevel==='Medium' ? 'bg-amber-500' : 'bg-emerald-500'"></span>
            {{ riskLevel }}
          </div>
          <RouterLink to="/reports" class="mt-3 block text-sm underline decoration-dotted">Генерирай отчет</RouterLink>
        </div>
      </div>
    </section>

        <!-- Crypto Prices Row (left: 3 цени, right: линкове) -->
    <section class="grid grid-cols-12 gap-4">
      <!-- LEFT: 3 цени -->
      <div class="col-span-12 lg:col-span-8">
        <div class="bg-white border rounded-2xl p-5">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-semibold">Крипто цени</h3>
            <button
              class="text-sm underline decoration-dotted"
              @click="loadCryptoMini('top3')"
              :disabled="loading"
            >
              {{ loading ? 'Зареждане...' : 'Обнови' }}
            </button>
          </div>

          <div class="grid sm:grid-cols-3 gap-4">
            <div
              v-for="c in cryptoMini"
              :key="c.sym"
              class="rounded-xl border p-4"
            >
              <div class="text-slate-500 text-xs">{{ c.sym }}</div>
              <div class="text-2xl font-semibold tabular-nums mt-1">
                {{ c.price.toLocaleString('bg-BG') }} $
              </div>
              <div
                class="text-xs"
                :class="c.change>=0 ? 'text-emerald-600' : 'text-rose-600'"
              >
                {{ c.change>=0?'+':'' }}{{ (c.change ?? 0).toFixed?.(2) ?? '0.00' }}%
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- RIGHT: линкове -->
      <div class="col-span-12 lg:col-span-4">
        <div class="bg-white border rounded-2xl p-5 h-full">
          <h3 class="font-semibold mb-3">Крипто</h3>
          <div class="space-y-2">
            <RouterLink
              to="/crypto/trade"
              class="block text-center px-3 py-2 rounded-md bg-emerald-600 text-white hover:bg-emerald-700"
            >
              Купи / Продай
            </RouterLink>
            <RouterLink
              to="/crypto/wallet"
              class="block text-center px-3 py-2 rounded-md border hover:bg-slate-50"
            >
              Крипто портфейл
            </RouterLink>
          </div>
        </div>
      </div>
    </section>


    <!-- Latest Transactions -->
    <section class="bg-white border rounded-2xl p-5">
      <div class="flex items-center justify-between mb-3">
        <h3 class="font-semibold">Последни транзакции</h3>
        <RouterLink to="/transactions" class="text-sm underline decoration-dotted">Всички</RouterLink>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="text-left text-slate-500">
              <th class="py-2">Дата</th>
              <th class="py-2">Тип</th>
              <th class="py-2">Описание</th>
              <th class="py-2">IBAN</th>
              <th class="py-2 text-right">Сума</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in lastTx" :key="t.id" class="border-t">
              <td class="py-2 whitespace-nowrap">{{ t.date || '-' }}</td>
              <td class="py-2">{{ t.type }}</td>
              <td class="py-2">{{ t.description || '-' }}</td>
              <td class="py-2">{{ t.accountIban || t.iban || '-' }}</td>
              <td class="py-2 text-right" :class="t.type==='EXPENSE' ? 'text-rose-600' : t.type==='INCOME' ? 'text-emerald-600' : ''">
                {{ (t.amount ?? 0).toLocaleString('bg-BG', { minimumFractionDigits: 2 }) }} BGN
              </td>
            </tr>
            <tr v-if="!lastTx.length && !loading">
              <td colspan="5" class="py-4 text-center text-slate-500">Няма транзакции.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<style scoped>
.tabular-nums{ font-variant-numeric: tabular-nums; }
.fill-emerald-400 { fill: #34d399; }
.fill-rose-400 { fill: #fb7185; }
</style>
