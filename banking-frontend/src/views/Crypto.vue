<script setup>
import { onMounted, onBeforeUnmount, ref } from "vue";
import { getSimplePrices, openLiveStream } from "../api/crypto";
import { CRYPTO_ASSETS, IDS_CSV } from "../constants/crypto";

const rows = ref(CRYPTO_ASSETS.map(a => ({ ...a, price: 0 })));

let stopStream = null;

// еднократно зареждане на текущите цени (ако е позволено)
async function loadOnce() {
  const { data } = await getSimplePrices(IDS_CSV, "usd");

  const getById = (id) => {
    if (Array.isArray(data)) {
      const hit = data.find(p => p.id === id);
      return hit?.prices?.usd;
    } else {
      return data?.[id]?.usd;
    }
  };

  rows.value = rows.value.map(r => ({
    ...r,
    price: typeof getById(r.id) === "number" ? getById(r.id) : r.price,
  }));
}

onMounted(async () => {
  // 1) Отвори live стрийма веднага
  stopStream = openLiveStream((msg) => {
    const id = msg.id || msg.asset || msg.symbol; // в случай че полетата са различни
    const i = rows.value.findIndex(r => r.id === id);
    if (i >= 0 && typeof msg.price === "number") {
      rows.value[i].price = msg.price;
    }
  });

  // 2) Опитай snapshot цените, но не блокирай екрана при грешка
  try {
    await loadOnce();
  } catch (e) {
    console.warn("[prices] simple-price недостъпен – работим само с live", e);
  }
});

onBeforeUnmount(() => { if (stopStream) stopStream(); });
</script>

<template>
  <div class="bg-white border rounded-2xl p-5">
    <div class="flex items-center justify-between mb-3">
      <h2 class="font-semibold">Crypto — Live Prices (20)</h2>
      <button
        class="text-sm text-slate-600 hover:text-slate-900 underline decoration-dotted"
        @click="loadOnce"
      >
        Refresh
      </button>
    </div>

    <div class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500">
            <th class="py-2">Asset</th>
            <th class="py-2">Price</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.id" class="border-t">
            <td class="py-2 font-medium">{{ r.name }} ({{ r.sym }})</td>
            <td class="py-2 tabular-nums">
              {{ Number(r.price || 0).toLocaleString("bg-BG") }} $
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
