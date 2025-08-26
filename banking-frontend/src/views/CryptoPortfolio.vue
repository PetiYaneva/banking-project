<script setup>
import { ref } from "vue";
import { getPortfolioByAccount, getOrdersByAccount } from "../api/crypto";

const accountId = ref("");
const portfolio = ref(null);
const orders = ref([]);

async function load() {
  const [p, o] = await Promise.all([
    getPortfolioByAccount(accountId.value, "usd"),
    getOrdersByAccount(accountId.value),
  ]);
  portfolio.value = p.data;
  orders.value = o.data || [];
}
</script>

<template>
  <div class="space-y-6">
    <div class="bg-white border rounded-2xl p-5 max-w-xl">
      <h2 class="font-semibold mb-3">Crypto — Portfolio</h2>

      <div class="flex gap-2">
        <input v-model="accountId" class="flex-1 border rounded-md px-3 py-2" placeholder="Account ID / IBAN" />
        <button class="rounded-md bg-blue-600 text-white px-4 py-2 hover:bg-blue-700" @click="load">Load</button>
      </div>

      <div v-if="portfolio" class="mt-4 text-sm">
        <div class="font-medium mb-1">
          Total value: {{ portfolio.totalValue?.toLocaleString("bg-BG") }} USD
        </div>

        <ul class="divide-y mt-2">
          <li v-for="a in portfolio.assets" :key="a.symbol" class="py-2 flex justify-between">
            <div>{{ a.symbol }}</div>
            <div>{{ a.amount }} ({{ a.usdValue?.toLocaleString("bg-BG") }} $)</div>
          </li>
        </ul>
      </div>
    </div>

    <div class="bg-white border rounded-2xl p-5">
      <h2 class="font-semibold mb-3">Orders</h2>
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500">
            <th class="py-2">ID</th>
            <th class="py-2">Symbol</th>
            <th class="py-2">Side</th>
            <th class="py-2">Amount</th>
            <th class="py-2">Price</th>
            <th class="py-2">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id" class="border-t">
            <td class="py-2">{{ o.id }}</td>
            <td class="py-2">{{ o.symbol }}</td>
            <td class="py-2">{{ o.side }}</td>
            <td class="py-2">{{ o.amount }}</td>
            <td class="py-2">{{ o.price }}</td>
            <td class="py-2">{{ o.status }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
table {
  border-collapse: collapse;
}
th, td {
  padding: 0.25rem 0.5rem;
}
</style>