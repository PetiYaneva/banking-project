<script setup>
import { ref, computed, onMounted } from "vue";
import { useAuth } from "../stores/auth";
import { getUserAccounts, getUserTotalBalance } from "../api/account"; // <-- поправено
import { getUserTransactions, getIncomeSummary, getExpenseSummary } from "../api/transactions";
import { getLoansByUser } from "../api/loans";
import { getSimplePrices } from "../api/crypto";
import { RouterLink } from "vue-router";

const auth = useAuth();
const userId = computed(() => auth.user?.id || auth.userId);

// state
const loading = ref(true);
const totalBalance = ref(0);
const accounts = ref([]);
const lastTx = ref([]);
const nextInstallment = ref(null);
const riskLevel = ref("—");
const inc6 = ref([]);
const exp6 = ref([]);
const cryptoMini = ref([]);

async function load() {
  if (!userId.value) return;
  try {
    const [accRes, balRes, txRes, incRes, expRes, loansRes, pricesRes] = await Promise.all([
      getUserAccounts(userId.value),
      getUserTotalBalance(userId.value),
      getUserTransactions(userId.value),
      getIncomeSummary(userId.value, 6),
      getExpenseSummary(userId.value, 6),
      getLoansByUser(),
      getSimplePrices("bitcoin,ethereum,litecoin","usd"),
    ]);

    accounts.value = accRes.data || [];
    totalBalance.value = balRes.data?.total ?? 0;
    lastTx.value = (txRes.data || []).slice(0, 5);

    inc6.value = Array.isArray(incRes.data) ? incRes.data : [];
    exp6.value = Array.isArray(expRes.data) ? expRes.data : [];

    const loans = loansRes.data || [];
    const upcoming = loans
      .map(l => ({ id: l.id, date: new Date(l.nextPaymentDate), amount: l.monthlyPayment }))
      .filter(x => x.date && !Number.isNaN(+x.date))
      .sort((a,b) => +a.date - +b.date)[0];
    nextInstallment.value = upcoming ? {
      amount: upcoming.amount, date: upcoming.date.toISOString().slice(0,10), loanId: upcoming.id
    } : null;

    const prices = pricesRes.data || {};
    cryptoMini.value = [
      { id: "bitcoin", sym: "BTC", price: prices.bitcoin?.usd ?? 0, change: prices.bitcoin?.usd_24h_change ?? 0 },
      { id: "ethereum", sym: "ETH", price: prices.ethereum?.usd ?? 0, change: prices.ethereum?.usd_24h_change ?? 0 },
      { id: "litecoin", sym: "LTC", price: prices.litecoin?.usd ?? 0, change: prices.litecoin?.usd_24h_change ?? 0 },
    ];

    const sum = (arr)=>arr.reduce((a,b)=>a+(b.amount||0),0);
    const incTotal = sum(inc6.value);
    const expTotal = sum(exp6.value);
    const ratio = incTotal ? (expTotal/incTotal) : 0;
    riskLevel.value = ratio >= 0.6 ? "High" : ratio >= 0.4 ? "Medium" : "Low";
  } finally {
    loading.value = false;
  }
}

function toBars(series, w=220, h=56, pad=4) {
  if (!series?.length) return [];
  const max = Math.max(...series.map(s=>s.amount||0), 1);
  const bw = (w - pad*2) / series.length;
  return series.map((s,i) => {
    const val = (s.amount||0) / max;
    const bh = val*(h - pad*2);
    return { x: pad + i*bw, y: h - pad - bh, w: Math.max(2, bw*0.6), h: bh, label: s.month || (i+1) };
  });
}

onMounted(load);
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
                  : 'text-emerald-700 border-emerald-300 bg-emerald-50'"
          >
            <span class="w-1.5 h-1.5 rounded-full"
              :class="riskLevel==='High' ? 'bg-rose-500' : riskLevel==='Medium' ? 'bg-amber-500' : 'bg-emerald-500'"></span>
            {{ riskLevel }}
          </div>
          <RouterLink to="/reports" class="mt-3 block text-sm underline decoration-dotted">Генерирай отчет</RouterLink>
        </div>
      </div>
    </section>

    <!-- Trend Row (6 months income/expense) + Crypto mini -->
    <section class="grid grid-cols-12 gap-4">
      <div class="col-span-12 lg:col-span-8">
        <div class="bg-white border rounded-2xl p-5">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-semibold">Приходи / Разходи (последни 6 месеца)</h3>
            <button class="text-sm underline decoration-dotted" @click="load" :disabled="loading">
              {{ loading ? 'Зареждане...' : 'Обнови' }}
            </button>
          </div>
          <div class="grid md:grid-cols-2 gap-4">
            <div>
              <div class="text-xs text-slate-500 mb-1">Приходи</div>
              <svg :width="240" :height="72">
                <g v-for="(b,i) in toBars(inc6)" :key="i">
                  <rect :x="b.x" :y="b.y" :width="b.w" :height="b.h" class="fill-emerald-400"></rect>
                </g>
              </svg>
            </div>
            <div>
              <div class="text-xs text-slate-500 mb-1">Разходи</div>
              <svg :width="240" :height="72">
                <g v-for="(b,i) in toBars(exp6)" :key="i">
                  <rect :x="b.x" :y="b.y" :width="b.w" :height="b.h" class="fill-rose-400"></rect>
                </g>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <div class="col-span-12 lg:col-span-4">
        <div class="bg-white border rounded-2xl p-5 h-full">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-semibold">Крипто (mini)</h3>
            <RouterLink to="/crypto/live" class="text-sm underline decoration-dotted">Live</RouterLink>
          </div>
          <div class="space-y-2">
            <div v-for="c in cryptoMini" :key="c.sym" class="flex items-center justify-between">
              <div class="font-medium">{{ c.sym }}</div>
              <div class="text-right">
                <div class="tabular-nums">{{ c.price.toLocaleString('bg-BG') }} $</div>
                <div class="text-xs" :class="c.change>=0 ? 'text-emerald-600' : 'text-rose-600'">
                  {{ c.change>=0?'+':'' }}{{ c.change.toFixed(2) }}%
                </div>
              </div>
            </div>
          </div>
          <RouterLink to="/crypto/trade" class="mt-3 inline-block text-sm px-3 py-1.5 rounded-md border hover:bg-slate-50">
            Търгувай
          </RouterLink>
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
                {{ (t.amount ?? 0).toLocaleString('bg-BG', { minimumFractionDigits: 2 }) }} $
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
