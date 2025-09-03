<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useAuth } from "../stores/auth";
import { getUserAccounts } from "../api/account";
import { placeOrder, getPortfolioByUser } from "../api/cryptoTrade";
import { getSimplePrices } from "../api/crypto";
import { CRYPTO_ASSETS } from "../constants/crypto";

const route = useRoute();
const auth = useAuth();
const userId = computed(() => auth.user?.id || auth.userId);

const accounts = ref([]);
const selectedAccountId = ref("");
const mode = ref(String(route.query.mode || "buy").toLowerCase());
const side = computed(() => (mode.value === "sell" ? "SELL" : "BUY"));

const buySym = ref(route.query.sym || "BTC");
const sellSym = ref(route.query.sym || "");
const quantityStr = ref("");

const message = ref("");
const loading = ref(false);

// котировка и тотал в BGN
const unitPriceBgn = ref(0);
const totalBgn = ref(0);

// наличности за „Продай“
const holdings = ref([]);

// map: "BTC" -> "bitcoin"
const idBySym = Object.fromEntries(CRYPTO_ASSETS.map(a => [a.sym, a.id]));

// fallback курс
const FX_USD_BGN = 1.80;

// ---------- helpers ----------
function accLabel(a) {
  const iban = a.iban ? ` (${a.iban})` : "";
  const bal = Number(a.balance ?? 0).toFixed(2);
  return `${a.accountType || "ACCOUNT"}${iban} — ${bal}`;
}
function getNameBySym(sym) {
  return CRYPTO_ASSETS.find(c => c.sym === sym)?.name || sym;
}
function parseQtyToNumber(s) {
  if (!s) return NaN;
  const norm = String(s).trim().replace(",", ".");
  if (!/^\d+(\.\d{1,8})?$/.test(norm)) return NaN;
  return Number(norm);
}
function recalcTotal() {
  const qty = parseQtyToNumber(quantityStr.value);
  totalBgn.value =
    Number.isFinite(qty) && qty > 0 && unitPriceBgn.value > 0
      ? Number((qty * unitPriceBgn.value).toFixed(2))
      : 0;
}

// ---------- quotes ----------
function pickPriceFromResponse(d, id, preferKey) {
  if (!d) return 0;
  if (Array.isArray(d)) {
    const row = d.find(x => x?.id === id) || d[0];
    if (!row) return 0;
    const prices = row?.prices && typeof row.prices === "object" ? row.prices : row;
    const keys = Object.keys(prices || {});
    const k =
      keys.find(k => k.toLowerCase() === preferKey) ||
      keys.find(k => typeof prices[k] === "number") ||
      keys[0];
    return Number(prices?.[k] ?? 0);
  }
  if (typeof d === "object") {
    const node = d[id];
    if (!node) return 0;
    if (typeof node === "number") return node;
    const keys = Object.keys(node || {});
    const k =
      keys.find(k => k.toLowerCase() === preferKey) ||
      keys.find(k => typeof node[k] === "number") ||
      keys[0];
    return Number(node?.[k] ?? 0);
  }
  return 0;
}

async function loadQuote() {
  try {
    const sym = mode.value === "sell" ? sellSym.value : buySym.value;
    const id = idBySym[sym] || "";
    unitPriceBgn.value = 0;
    if (!id) return;
    let resp = await getSimplePrices(id, "bgn");
    let price = pickPriceFromResponse(resp?.data, id, "bgn");
    if (!(price > 0)) {
      resp = await getSimplePrices(id, "usd");
      const usd = pickPriceFromResponse(resp?.data, id, "usd");
      if (usd > 0) price = usd * FX_USD_BGN;
    }
    unitPriceBgn.value = Number.isFinite(price) ? price : 0;
  } catch {
    unitPriceBgn.value = 0;
  } finally {
    recalcTotal();
  }
}

// ---------- API ----------
async function loadAccounts() {
  if (!userId.value) return;
  const { data } = await getUserAccounts(userId.value);
  accounts.value = data || [];
  if (!selectedAccountId.value && accounts.value.length) {
    selectedAccountId.value = String(route.query.accountId || accounts.value[0].id);
  }
}
async function loadHoldingsByUser() {
  if (!userId.value) return;
  const { data } = await getPortfolioByUser(userId.value, "usd");
  const rows = Array.isArray(data) ? data : (data?.positions || []);

  holdings.value = rows
    .map(r => ({
      symbol: r.symbol || r.sym || r.asset,   // <--- ТУК добавихме r.asset
      quantity: Number(r.quantity ?? r.qty ?? 0),
    }))
    .filter(r => r.symbol && r.quantity > 0);

  if (!sellSym.value && holdings.value.length) {
    sellSym.value = holdings.value[0].symbol;
  }
}
const selectedHoldingQty = computed(() => {
  const h = holdings.value.find(x => x.symbol === sellSym.value);
  return Number(h?.quantity || 0);
});

// ---------- helpers UI ----------
function addQty(delta) {
  const cur = parseQtyToNumber(quantityStr.value) || 0;
  let next = cur + delta;
  if (mode.value === "sell" && sellSym.value) {
    const max = selectedHoldingQty.value;
    if (next > max) next = max;
  }
  quantityStr.value = Number(next.toFixed(8)).toString();
  recalcTotal();
}
function setPercent(pct) {
  if (!sellSym.value) return;
  const max = selectedHoldingQty.value;
  quantityStr.value = Number((max * pct).toFixed(8)).toString();
  recalcTotal();
}
function clearQty() {
  quantityStr.value = "";
  recalcTotal();
}

// ---------- submit ----------
async function submit() {
  message.value = "";
  const qty = parseQtyToNumber(quantityStr.value);
  if (!Number.isFinite(qty) || qty <= 0) {
    message.value = "Въведете валидно количество.";
    return;
  }
  const sym = mode.value === "sell" ? sellSym.value : buySym.value;
  const finalQty = Number(qty.toFixed(8));
  const ibanVal = accounts.value.find(a => a.id === selectedAccountId.value)?.iban || "";
  if (!ibanVal) {
    message.value = "Липсва IBAN за избраната сметка.";
    return;
  }
  const payload = {
    userId: userId.value,
    iban: ibanVal,
    symbol: sym,
    side: side.value,
    quantity: finalQty,
  };
  try {
    loading.value = true;
    await placeOrder(payload);
    message.value = mode.value === "sell" ? "Продажбата е успешна." : "Покупката е успешна.";
    quantityStr.value = "";
    totalBgn.value = 0;
    if (mode.value === "sell") await loadHoldingsByUser();
  } catch (e) {
    message.value = e?.response?.data?.message || "Възникна грешка.";
  } finally {
    loading.value = false;
  }
}

// watchers
watch(() => mode.value, async () => {
  quantityStr.value = "";
  message.value = "";
  if (mode.value === "sell") await loadHoldingsByUser();
  await loadQuote();
});
watch(() => buySym.value, loadQuote);
watch(() => sellSym.value, loadQuote);
watch(() => quantityStr.value, recalcTotal);

onMounted(async () => {
  await loadAccounts();
  if (mode.value === "sell") await loadHoldingsByUser();
  await loadQuote();
});
</script>

<template>
  <div class="p-5 space-y-6">
    <h1 class="text-2xl font-semibold">Купи / Продай криптовалути</h1>

    <section class="bg-white border rounded-2xl p-4 space-y-4">
      <!-- Buy / Sell toggle -->
      <div class="flex gap-2">
        <button :class="mode==='buy' ? 'bg-emerald-600 text-white px-4 py-2 rounded' : 'bg-slate-100 px-4 py-2 rounded'" @click="mode='buy'">Купи</button>
        <button :class="mode==='sell' ? 'bg-rose-600 text-white px-4 py-2 rounded' : 'bg-slate-100 px-4 py-2 rounded'" @click="mode='sell'">Продай</button>
      </div>

      <!-- Accounts -->
      <div>
        <label>Сметка</label>
        <select v-model="selectedAccountId" class="border rounded px-2 py-1 w-full">
          <option v-for="a in accounts" :key="a.id" :value="a.id">{{ accLabel(a) }}</option>
        </select>
      </div>

      <!-- BUY -->
      <div v-if="mode==='buy'" class="grid sm:grid-cols-3 gap-3">
        <div>
          <label>Криптовалута</label>
          <select v-model="buySym" class="border rounded px-2 py-1 w-full">
            <option v-for="c in CRYPTO_ASSETS" :key="c.sym" :value="c.sym">{{ c.sym }} — {{ c.name }}</option>
          </select>
        </div>
        <div>
          <label>Количество</label>
          <input v-model="quantityStr" type="text" inputmode="decimal" class="border rounded px-2 py-1 w-full" placeholder="0.00000000"/>
          <div class="flex gap-1 mt-2">
            <button @click="addQty(0.001)" class="border rounded px-2">+0.001</button>
            <button @click="addQty(0.01)" class="border rounded px-2">+0.01</button>
            <button @click="addQty(0.1)" class="border rounded px-2">+0.1</button>
            <button @click="clearQty" class="border rounded px-2">Изчисти</button>
          </div>
          <p class="text-xs">Цена (1 {{ buySym }}) ≈ {{ unitPriceBgn.toFixed(2) }} BGN</p>
          <p>Сума за плащане: <b>{{ totalBgn.toFixed(2) }} BGN</b></p>
        </div>
        <div class="flex items-end">
          <button class="w-full bg-emerald-600 text-white px-4 py-2 rounded" @click="submit">Купи</button>
        </div>
      </div>

      <!-- SELL -->
      <div v-else class="grid sm:grid-cols-3 gap-3">
        <div>
          <label>Криптовалута (налични)</label>
         <select v-model="sellSym" class="border rounded px-2 py-1 w-full">
            <option v-for="h in holdings" :key="h.symbol" :value="h.symbol">
              {{ h.symbol }} — {{ getNameBySym(h.symbol) }}
            </option>
          </select>
          <p v-if="sellSym" class="text-xs">
            Налично: {{ selectedHoldingQty.toFixed(8) }}
          </p>
        </div>
        <div>
          <label>Количество</label>
          <input v-model="quantityStr" type="text" inputmode="decimal" class="border rounded px-2 py-1 w-full" placeholder="0.00000000"/>
          <div class="flex gap-1 mt-2">
            <button @click="addQty(0.001)" class="border rounded px-2">+0.001</button>
            <button @click="addQty(0.01)" class="border rounded px-2">+0.01</button>
            <button @click="addQty(0.1)" class="border rounded px-2">+0.1</button>
            <button @click="setPercent(0.5)" class="border rounded px-2">50%</button>
            <button @click="setPercent(1)" class="border rounded px-2">MAX</button>
            <button @click="clearQty" class="border rounded px-2">Изчисти</button>
          </div>
          <p class="text-xs">Цена (1 {{ sellSym }}) ≈ {{ unitPriceBgn.toFixed(2) }} BGN</p>
          <p>Очаквана сума: <b>{{ totalBgn.toFixed(2) }} BGN</b></p>
        </div>
        <div class="flex items-end">
          <button class="w-full bg-rose-600 text-white px-4 py-2 rounded" @click="submit">Продай</button>
        </div>
      </div>

      <p v-if="message" class="text-sm text-red-600">{{ message }}</p>
    </section>
  </div>
</template>
