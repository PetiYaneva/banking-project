<script setup>
import { ref, onMounted } from 'vue';
import {
  getAccountsByUser,
  getAccountByIban,
  getAccountsHighBalance,
  getAccountsLowBalance,
  getAccountsByCurrency,
  getAccountsSortedBalanceDesc,
  getAccountsSortedCreatedAsc,
} from '../../api/admin';

const userId = ref('');
const iban = ref('');
const mode = ref('NONE'); 
const threshold = ref(null); 
const currency = ref('');   

const loading = ref(false);
const rows = ref([]);

function formatDate(x){ if(!x) return ''; return new Date(x).toLocaleString(); }

async function reload(){
  loading.value = true;
  try {
    if (userId.value) {
      rows.value = await getAccountsByUser(userId.value.trim());
      return;
    }
    if (iban.value) {
      rows.value = await getAccountByIban(iban.value.trim());
      return;
    }
    switch (mode.value) {
      case 'HIGH':
        rows.value = await getAccountsHighBalance(threshold.value ?? 0);
        break;
      case 'LOW':
        rows.value = await getAccountsLowBalance(threshold.value ?? 0);
        break;
      case 'CURR':
        rows.value = await getAccountsByCurrency((currency.value || '').trim());
        break;
      case 'SORT_BAL_DESC':
        rows.value = await getAccountsSortedBalanceDesc();
        break;
      case 'SORT_CREATED_ASC':
        rows.value = await getAccountsSortedCreatedAsc();
        break;
      default:
        rows.value = [];
    }
  } finally { loading.value = false; }
}

onMounted(reload);
</script>

<template>
  <section class="space-y-4">
    <h1 class="text-xl font-semibold">Accounts</h1>

    <div class="flex flex-wrap gap-2 items-end">
      <label class="text-sm">
        User ID
        <input v-model="userId" class="border rounded px-2 py-1 font-mono text-xs" placeholder="UUID" />
      </label>
      <label class="text-sm">
        IBAN
        <input v-model="iban" class="border rounded px-2 py-1 font-mono text-xs" placeholder="BG..." />
      </label>

      <select v-model="mode" class="border rounded px-2 py-1">
        <option value="NONE">— Admin filter —</option>
        <option value="HIGH">Balance ≥ min</option>
        <option value="LOW">Balance ≤ max</option>
        <option value="SORT_BAL_DESC">Sort: Balance desc</option>
        <option value="SORT_CREATED_ASC">Sort: Created asc</option>
      </select>

      <input
        v-if="mode==='HIGH' || mode==='LOW'"
        v-model.number="threshold"
        type="number" step="0.01"
        class="border rounded px-2 py-1"
        :placeholder="mode==='HIGH' ? 'min' : 'max'"
      />

      <input
        v-if="mode==='CURR'"
        v-model="currency"
        class="border rounded px-2 py-1"
        placeholder="BGN / EUR / USD"
      />

      <button @click="reload" class="h-9 px-4 rounded bg-slate-900 text-white">Apply</button>
    </div>

    <div class="overflow-auto border rounded-2xl">
      <table class="w-full text-sm">
        <thead class="bg-slate-50">
          <tr>
            <th class="p-2 text-left">Account ID</th>
            <th class="p-2 text-left">User ID</th>
            <th class="p-2 text-left">IBAN</th>
            <th class="p-2 text-left">Balance</th>
            <th class="p-2 text-left">Currency</th>
            <th class="p-2 text-left">Created</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in rows" :key="a.id" class="border-t">
            <td class="p-2 font-mono text-xs">{{ a.id }}</td>
            <td class="p-2 font-mono text-xs">{{ a.userId }}</td>
            <td class="p-2 font-mono text-xs">{{ a.iban }}</td>
            <td class="p-2">{{ a.balance }}</td>
            <td class="p-2">{{ a.currency ?? a.currencyCode }}</td>
            <td class="p-2">{{ formatDate(a.createdOn || a.createdAt) }}</td>
          </tr>
          <tr v-if="!loading && rows.length===0">
            <td class="p-3" colspan="6">No data</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
