<script setup>
import { ref, onMounted } from 'vue';
import {
  getAllTransactions,
  getAllTransactionsByPeriod,
  getAllTransactionsByPeriodAndType,
  getAllTransactionsByPeriodAndStatus,
  getAllTransactionsByPeriodAndCurrency,
  getAllTransactionsByMinAmount,
  getAllTransactionsByAmountBetween,
  searchAllTransactionsInPeriod,
} from '../../api/admin';

const from = ref('');
const to = ref('');
const type = ref('ALL');      // INCOME | EXPENSE | TRANSFER | DEPOSIT | WITHDRAWAL | ALL
const status = ref('');       // ако имате статуси, иначе остави празно
const currency = ref('');
const min = ref(null);
const max = ref(null);
const q = ref('');

const loading = ref(false);
const rows = ref([]);
const errorMsg = ref('');

function normalizeType(t){
  if (t?.transactionType) return t.transactionType;
  if (t?.isIncome) return 'INCOME';
  if (t?.isExpense) return 'EXPENSE';
  return 'TRANSFER';
}
function badgeClass(t){
  const v = normalizeType(t);
  const base = 'inline-flex items-center px-2 py-0.5 rounded-full text-xs border';
  if (v === 'WITHDRAWAL') return base + ' bg-red-50 border-red-200 text-red-700';
  if (v === 'DEPOSIT' || v === 'INCOME') return base + ' bg-emerald-50 border-emerald-200 text-emerald-700';
  if (v === 'TRANSFER') return base + ' bg-pink-50 border-pink-200 text-pink-700';
  if (v === 'EXPENSE') return base + ' bg-amber-50 border-amber-200 text-amber-700';
  return base + ' bg-slate-50 border-slate-200 text-slate-700';
}
function formatDate(x){ return x ? new Date(x).toLocaleString() : ''; }

function normalizeAmount(x) {
  const a = x?.amount ?? x?.value ?? 0;
  const n = typeof a === "string" ? Number(a) : Number(a ?? 0);
  return Number.isFinite(n) ? n : 0;
}

async function reload(){
  loading.value = true; errorMsg.value = ''; rows.value = [];
  try {
    const hasPeriod = !!(from.value && to.value);
    if (!hasPeriod) {
      // няма дати → всички (админ)
      rows.value = await getAllTransactions();
      return;
    }

    // приоритет по филтри (най-специфичен пръв)
    if (type.value && type.value !== 'ALL') {
      rows.value = await getAllTransactionsByPeriodAndType(from.value, to.value, type.value);
      return;
    }
    if (status.value) {
      rows.value = await getAllTransactionsByPeriodAndStatus(from.value, to.value, status.value);
      return;
    }
    if (currency.value) {
      rows.value = await getAllTransactionsByPeriodAndCurrency(from.value, to.value, currency.value);
      return;
    }
    if (min.value != null && max.value != null) {
      rows.value = await getAllTransactionsByAmountBetween(from.value, to.value, min.value, max.value);
      return;
    }
    if (min.value != null && (max.value == null)) {
      rows.value = await getAllTransactionsByMinAmount(from.value, to.value, min.value);
      return;
    }
    if (q.value) {
      rows.value = await searchAllTransactionsInPeriod(from.value, to.value, q.value);
      return;
    }
    // само период
    rows.value = await getAllTransactionsByPeriod(from.value, to.value);
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e?.message || 'Failed to load transactions';
    console.error(e);
  } finally { loading.value = false; }
}

onMounted(reload);
</script>

<template>
  <section class="space-y-4">
    <h1 class="text-xl font-semibold">Transactions</h1>

    <div class="grid md:grid-cols-6 gap-2 items-end">
      <label class="text-sm">From <input v-model="from" type="date" class="border rounded px-2 py-1" /></label>
      <label class="text-sm">To   <input v-model="to"   type="date" class="border rounded px-2 py-1" /></label>
      <label class="text-sm">Type
        <select v-model="type" class="border rounded px-2 py-1">
          <option value="ALL">All</option>
          <option value="TRANSFER">Transfer</option>
          <option value="DEPOSIT">Deposit</option>
          <option value="WITHDRAWAL">Withdrawal</option>
        </select>
      </label>
      <label class="text-sm">Status <input v-model="status" class="border rounded px-2 py-1" placeholder="e.g. SUCCESS" /></label>
      <label class="text-sm">Currency <input v-model="currency" class="border rounded px-2 py-1" placeholder="BGN/EUR/USD" /></label>
      <div class="md:col-span-6 flex gap-2">
        <input v-model.number="min" type="number" step="0.01" class="border rounded px-2 py-1" placeholder="Min amount" />
        <input v-model.number="max" type="number" step="0.01" class="border rounded px-2 py-1" placeholder="Max amount" />
        <input v-model="q" class="border rounded px-2 py-1 w-full" placeholder="Search description" />
        <button @click="reload" class="h-9 px-4 rounded bg-slate-900 text-white">Apply</button>
      </div>
    </div>

    <p v-if="errorMsg" class="text-red-600 text-sm">{{ errorMsg }}</p>

    <div class="overflow-auto border rounded-2xl">
      <table class="w-full text-sm">
        <thead class="bg-slate-50">
          <tr>
            <th class="p-2 text-left">Date</th>
            <th class="p-2 text-left">User</th>
            <th class="p-2 text-left">Account</th>
            <th class="p-2 text-left">Type</th>
            <th class="p-2 text-right">Amount</th>
            <th class="p-2 text-left">Currency</th>
            <th class="p-2 text-left">Description</th>
            <th class="p-2 text-left">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in rows" :key="t.id" class="border-t">
            <td class="p-2 whitespace-nowrap">{{ formatDate(t.createdOn || t.createdAt) }}</td>
            <td class="p-2 font-mono text-xs">{{ t.userId }}</td>
            <td class="p-2 font-mono text-xs">{{ t.accountId }}</td>
            <td class="p-2"><span :class="badgeClass(t)">{{ normalizeType(t) }}</span></td>
            <td class="p-2 text-right">{{ Number(t.amount ?? 0).toFixed(2) }}</td>
            <td class="p-2">{{ t.currency }}</td>
            <td class="p-2">{{ t.description }}</td>
            <td class="p-2">{{ t.transactionStatus || t.status }}</td>
          </tr>
          <tr v-if="!loading && rows.length===0 && !errorMsg">
            <td class="p-3" colspan="8">No data</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
