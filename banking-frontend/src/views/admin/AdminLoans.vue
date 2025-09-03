<script setup>
import { ref, onMounted } from 'vue';
import {
  getLoansByUser,
  getLoansByStatus,
  getLoansSortedNextPaymentAsc,
} from '../../api/admin';

const userId = ref('');
const status = ref(''); // '', 'ACTIVE', 'PAID_OFF', 'OVERDUE'
const loading = ref(false);
const rows = ref([]);
const errorMsg = ref('');

function formatDate(x){ return x ? new Date(x).toLocaleDateString() : ''; }
function statusLabel(s) {
  if (s === 'PAID_OFF') return 'Paid off';
  if (s === 'OVERDUE')  return 'Overdue';
  if (s === 'ACTIVE')   return 'Active';
  return s || '—';
}
function statusClass(s) {
  // нежни цветове за баджовете
  if (s === 'PAID_OFF') return 'bg-emerald-50 text-emerald-700 border-emerald-200';
  if (s === 'OVERDUE')  return 'bg-red-50 text-red-700 border-red-200';
  return 'bg-slate-50 text-slate-700 border-slate-200'; // ACTIVE/other
}

async function reload(){
  loading.value = true; errorMsg.value = ''; rows.value = [];
  try {
    if (userId.value?.trim()) {
      rows.value = await getLoansByUser(userId.value.trim());
    } else if (status.value) {
      rows.value = await getLoansByStatus(status.value); // изпраща точно ACTIVE/PAID_OFF/OVERDUE
    } else {
      rows.value = await getLoansSortedNextPaymentAsc();
    }
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e?.message || 'Failed to load loans';
    console.error(e);
  } finally { loading.value = false; }
}

onMounted(reload);
</script>

<template>
  <section class="space-y-4">
    <h1 class="text-xl font-semibold">Loans</h1>

    <div class="flex flex-wrap gap-2 items-end">
      <label class="text-sm">User ID
        <input v-model="userId" class="border rounded px-2 py-1 font-mono text-xs" placeholder="UUID" />
      </label>
      <label class="text-sm">Status
        <select v-model="status" class="border rounded px-2 py-1">
          <option value="">All</option>
          <option value="ACTIVE">Active</option>
          <option value="PAID_OFF">Paid off</option>
          <option value="OVERDUE">Overdue</option>
        </select>
      </label>
      <button @click="reload" class="h-9 px-4 rounded bg-slate-900 text-white">Apply</button>
    </div>

    <p v-if="errorMsg" class="text-red-600 text-sm">{{ errorMsg }}</p>

    <div class="overflow-auto border rounded-2xl">
      <table class="w-full text-sm">
        <thead class="bg-slate-50">
          <tr>
            <th class="p-2 text-left">Loan ID</th>
            <th class="p-2 text-left">User</th>
            <th class="p-2 text-right">Total</th>
            <th class="p-2 text-right">Monthly</th>
            <th class="p-2 text-right">Remaining</th>
            <th class="p-2 text-left">Next payment</th>
            <th class="p-2 text-left">Final date</th>
            <th class="p-2 text-left">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="l in rows" :key="l.id" class="border-t">
            <td class="p-2 font-mono text-xs">{{ l.id }}</td>
            <td class="p-2 font-mono text-xs">{{ l.userId }}</td>
            <td class="p-2 text-right">{{ l.totalAmount }}</td>
            <td class="p-2 text-right">{{ l.monthlyPayment }}</td>
            <td class="p-2 text-right">{{ l.remainingBalance }}</td>
            <td class="p-2">{{ formatDate(l.nextPaymentDate) }}</td>
            <td class="p-2">{{ formatDate(l.finalLoanDate) }}</td>
            <td class="p-2">
              <span :class="statusClass(l.status)"
                    class="inline-flex items-center px-2 py-0.5 rounded-full text-xs border">
                {{ statusLabel(l.status) }}
              </span>
            </td>
          </tr>
          <tr v-if="!loading && rows.length===0 && !errorMsg">
            <td class="p-3" colspan="8">No data</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
