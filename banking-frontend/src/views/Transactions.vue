<!-- src/views/Transactions.vue -->
<script setup>
import { ref, computed, onMounted } from "vue";
import { useAuth } from "../stores/auth";
import { getUserTransactions } from "../api/transactions";

const auth = useAuth();
const userId = computed(() => auth.user?.id || auth.userId);

const loading = ref(false);
const rows = ref([]);

// Филтри (вече включват и TRANSFER)
const type = ref("ALL"); // ALL | DEPOSIT | WITHDRAWAL | TRANSFER
const from = ref("");
const to = ref("");
const min = ref(null);
const max = ref(null);
const q = ref("");

// -------- Helpers --------
function normalizeType(raw, x) {
  const s = (raw || "").toString().toUpperCase();
  if (s.includes("WITHDRAW")) return "WITHDRAWAL";
  if (s.includes("DEPOSIT")) return "DEPOSIT";
  if (s.includes("TRANSFER")) return "TRANSFER";

  // fallback: използвай income/expense флагове
  if (x?.expense === true) return "WITHDRAWAL";
  if (x?.income === true) return "DEPOSIT";

  // fallback по знак на amount ако е наличен
  const num = Number(x?.amount ?? x?.value ?? 0);
  if (num < 0) return "WITHDRAWAL";
  if (num > 0) return "DEPOSIT";

  return "TRANSFER";
}

function normalizeTxn(x) {
  const t = normalizeType(x.transactionType, x);

  // amount може да идва винаги положителен от бекенда
  const rawAmount = Number(x.amount ?? x.value ?? 0);
  // Поставяме знак само за DEPOSIT/WITHDRAWAL; TRANSFER е без знак
  const signed =
    t === "DEPOSIT"
      ? Math.abs(rawAmount)
      : t === "WITHDRAWAL"
      ? -Math.abs(rawAmount)
      : Math.abs(rawAmount);

  const date = (x.createdOn || x.date || x.createdAt || x.timestamp || "")
    .toString()
    .slice(0, 19)
    .replace("T", " ");

  return {
    id: x.id || x.txId || (crypto?.randomUUID ? crypto.randomUUID() : String(Math.random())),
    date,
    type: t, // DEPOSIT | WITHDRAWAL | TRANSFER
    description: x.description || x.merchant || x.note || (t === "TRANSFER" ? "Transfer" : ""),
    accountIban: x.accountIban || x.iban || x.fromIban || x.toIban || "",
    currency: (x.currency || "BGN").toUpperCase(),
    amount: signed,
  };
}

function withinDate(d, fromStr, toStr) {
  if (!d) return true;
  const dt = new Date(d);
  if (fromStr && dt < new Date(fromStr)) return false;
  if (toStr && dt > new Date(toStr)) return false;
  return true;
}

const filtered = computed(() => {
  return rows.value.filter((r) => {
    const passType = type.value === "ALL" || r.type === type.value;
    const passDate = withinDate(r.date, from.value, to.value);
    const passMin = min.value == null || (r.amount != null && Math.abs(r.amount) >= +min.value);
    const passMax = max.value == null || (r.amount != null && Math.abs(r.amount) <= +max.value);
    const hay = (r.description + " " + r.accountIban).toLowerCase();
    const passQ = !q.value || hay.includes(q.value.toLowerCase());
    return passType && passDate && passMin && passMax && passQ;
  });
});

async function load() {
  if (!userId.value) return;
  loading.value = true;
  try {
    const { data } = await getUserTransactions(userId.value);
    const arr = Array.isArray(data) ? data : (data?.content ?? data?.items ?? []);
    rows.value = arr.map(normalizeTxn);
  } finally {
    loading.value = false;
  }
}

onMounted(load);

// ---- UI helpers ----
function rowBgClass(t) {
  if (t === "DEPOSIT") return "bg-emerald-50";
  if (t === "WITHDRAWAL") return "bg-rose-50";
  return "bg-amber-50"; // TRANSFER
}
function amountTextClass(t) {
  if (t === "DEPOSIT") return "text-emerald-700";
  if (t === "WITHDRAWAL") return "text-rose-600";
  return "text-amber-700";
}
function amountPrefix(t, amt) {
  if (t === "DEPOSIT") return "+";
  if (t === "WITHDRAWAL") return "−";
  return ""; // TRANSFER
}
</script>

<template>
  <div class="bg-white border rounded-2xl p-5">
    <div class="flex items-center justify-between mb-3">
      <h2 class="font-semibold">Транзакции</h2>
      <button class="text-sm underline decoration-dotted" :disabled="loading" @click="load">
        {{ loading ? "Зареждане..." : "Обнови" }}
      </button>
    </div>

    <!-- Филтри -->
    <div class="grid md:grid-cols-7 gap-3 mb-3 text-sm">
      <select v-model="type" class="border rounded-md px-3 py-2">
        <option value="ALL">Всички</option>
        <option value="DEPOSIT">DEPOSIT</option>
        <option value="WITHDRAWAL">WITHDRAWAL</option>
        <option value="TRANSFER">TRANSFER</option>
      </select>
      <input type="date" v-model="from" class="border rounded-md px-3 py-2" />
      <input type="date" v-model="to" class="border rounded-md px-3 py-2" />
      <input type="number" step="0.01" v-model.number="min" class="border rounded-md px-3 py-2" placeholder="Мин. сума" />
      <input type="number" step="0.01" v-model.number="max" class="border rounded-md px-3 py-2" placeholder="Макс. сума" />
      <input v-model="q" class="border rounded-md px-3 py-2 md:col-span-2" placeholder="Търсене (описание/IBAN)" />
    </div>

    <!-- Таблица -->
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
          <tr
            v-for="t in filtered"
            :key="t.id"
            class="border-t"
            :class="rowBgClass(t.type)"
          >
            <td class="py-2 whitespace-nowrap">{{ t.date || '—' }}</td>
            <td class="py-2">
              <span
                class="px-2 py-0.5 rounded-full text-xs border"
                :class="{
                  'bg-emerald-50 text-emerald-700 border-emerald-200': t.type==='DEPOSIT',
                  'bg-rose-50 text-rose-700 border-rose-200': t.type==='WITHDRAWAL',
                  'bg-amber-50 text-amber-700 border-amber-200': t.type==='TRANSFER'
                }"
              >
                {{ t.type }}
              </span>
            </td>
            <td class="py-2">{{ t.description || '—' }}</td>
            <td class="py-2">{{ t.accountIban || '—' }}</td>
            <td
              class="py-2 text-right tabular-nums font-medium"
              :class="amountTextClass(t.type)"
            >
              {{ amountPrefix(t.type, t.amount) }}
              {{ Math.abs(t.amount).toLocaleString('bg-BG', { minimumFractionDigits: 2 }) }} {{ t.currency }}
            </td>
          </tr>

          <tr v-if="!filtered.length && !loading">
            <td colspan="5" class="py-6 text-center text-slate-500">Няма транзакции по зададените филтри.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.tabular-nums { font-variant-numeric: tabular-nums; }
</style>
