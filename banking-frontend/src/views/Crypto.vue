<script setup>
import { onMounted, onBeforeUnmount, ref } from "vue";
import { getSimplePrices, getHistory, streamLivePrices } from "../api/crypto";

const rows = ref([
  { id: "bitcoin",   sym: "BTC", name: "Bitcoin",  price: 0, change: 0, hist: [] },
  { id: "ethereum",  sym: "ETH", name: "Ethereum", price: 0, change: 0, hist: [] },
  { id: "litecoin",  sym: "LTC", name: "Litecoin", price: 0, change: 0, hist: [] },
]);

let stopStream = null;

async function loadOnce() {
  const ids = rows.value.map(r => r.id).join(",");
  const { data } = await getSimplePrices(ids, "usd");
  rows.value = rows.value.map(r => ({
    ...r,
    price: data[r.id]?.usd ?? r.price,
    change: data[r.id]?.usd_24h_change ?? r.change,
  }));
}

async function loadHistory() {
  // days=7 за лек sparkline
  const promises = rows.value.map(async (r) => {
    const { data } = await getHistory(r.id, "usd", 7);
    // очакваме масив от { t, p } или подобно; ако е друга форма, адаптирай:
    const series = (data?.prices || data)?.map(pt => Array.isArray(pt) ? pt[1] : pt.p) || [];
    r.hist = series;
  });
  await Promise.all(promises);
}

function toSparkPath(series, w = 120, h = 36, pad = 2) {
  if (!series?.length) return "";
  const min = Math.min(...series);
  const max = Math.max(...series);
  const span = Math.max(1e-9, max - min);
  const stepX = (w - pad * 2) / (series.length - 1);
  return series.map((v, i) => {
    const x = pad + i * stepX;
    const y = h - pad - ((v - min) / span) * (h - pad * 2);
    return `${i ? "L" : "M"}${x.toFixed(1)},${y.toFixed(1)}`;
  }).join(" ");
}

onMounted(async () => {
  await Promise.all([loadOnce(), loadHistory()]);
  stopStream = await streamLivePrices((msg) => {
    const i = rows.value.findIndex(r => r.id === msg.id);
    if (i >= 0) {
      rows.value[i].price = msg.price;
      rows.value[i].change = msg.change;
    }
  });
});

onBeforeUnmount(() => { if (stopStream) stopStream(); });
</script>

<template>
  <div class="bg-white border rounded-2xl p-5">
    <div class="flex items-center justify-between mb-3">
      <h2 class="font-semibold">Crypto — Live Prices</h2>
      <button class="text-sm text-slate-600 hover:text-slate-900 underline decoration-dotted" @click="() => { loadOnce(); loadHistory(); }">
        Refresh
      </button>
    </div>

    <div class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead><tr class="text-left text-slate-500">
          <th class="py-2">Asset</th><th class="py-2">Price</th><th class="py-2">24h</th><th class="py-2">7d</th>
        </tr></thead>
        <tbody>
          <tr v-for="r in rows" :key="r.id" class="border-t">
            <td class="py-2 font-medium">{{ r.name }} ({{ r.sym }})</td>
            <td class="py-2 tabular-nums">{{ r.price.toLocaleString('bg-BG') }} $</td>
            <td class="py-2">
              <span :class="r.change >= 0 ? 'text-emerald-600' : 'text-rose-600'">
                {{ r.change >= 0 ? '+' : '' }}{{ r.change.toFixed(2) }}%
              </span>
            </td>
            <td class="py-2">
              <svg :width="120" :height="36" class="overflow-visible">
                <path :d="toSparkPath(r.hist)" fill="none" stroke="currentColor" stroke-width="1.5"
                      :class="(r.hist.at(-1) ?? 0) - (r.hist[0] ?? 0) >= 0 ? 'text-emerald-600' : 'text-rose-600'"/>
              </svg>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.tabular-nums { font-variant-numeric: tabular-nums; }
</style>