<script setup>
import { ref, onMounted, computed } from "vue";
import { placeOrder } from "../api/crypto";
import { getUserAccounts } from "../api/accounts";
import { useAuth } from "../stores/auth";

const auth = useAuth();
const userId = computed(() => auth.user?.id || auth.userId);

const sym = ref("BTC"); // избран символ
const symbolToAsset = { BTC: "bitcoin", ETH: "ethereum", LTC: "litecoin" };

const ibans = ref([]);
const selectedIban = ref("");

const form = ref({
  side: "BUY",        // BUY | SELL
  quantity: 0.01,     // количество (не в $)
});

const loading = ref(false);
const message = ref("");

async function loadAccounts() {
  if (!userId.value) return;
  const { data } = await getUserAccounts(userId.value);
  ibans.value = (data || []).map(a => a.iban);
  if (!selectedIban.value && ibans.value.length) selectedIban.value = ibans.value[0];
}

async function submit() {
  loading.value = true; message.value = "";
  try {
    const payload = {
      iban: selectedIban.value,
      asset: symbolToAsset[sym.value] || sym.value.toLowerCase(),
      side: form.value.side,
      quantity: form.value.quantity,
    };
    const { data } = await placeOrder(payload);
    message.value = `Поръчката е изпратена: ${data?.status || "OK"}`;
  } catch (e) {
    message.value = e?.response?.data?.message || "Error placing order";
  } finally {
    loading.value = false;
  }
}

onMounted(loadAccounts);
</script>

<template>
  <div class="bg-white border rounded-2xl p-5 max-w-xl">
    <h2 class="font-semibold mb-3">Crypto — Trade</h2>
    <div class="grid gap-3">
      <label class="text-sm">
        IBAN
        <select v-model="selectedIban" class="mt-1 w-full border rounded-md px-3 py-2">
          <option v-for="i in ibans" :key="i" :value="i">{{ i }}</option>
        </select>
      </label>

      <label class="text-sm">
        Symbol
        <select v-model="sym" class="mt-1 w-full border rounded-md px-3 py-2">
          <option>BTC</option><option>ETH</option><option>LTC</option>
        </select>
      </label>

      <label class="text-sm">
        Side
        <select v-model="form.side" class="mt-1 w-full border rounded-md px-3 py-2">
          <option>BUY</option><option>SELL</option>
        </select>
      </label>

      <label class="text-sm">
        Quantity
        <input type="number" step="0.0001" v-model.number="form.quantity" class="mt-1 w-full border rounded-md px-3 py-2" />
      </label>

      <button :disabled="loading || !selectedIban" @click="submit"
        class="mt-2 inline-flex items-center justify-center rounded-md bg-blue-600 text-white px-4 py-2 hover:bg-blue-700">
        {{ loading ? 'Изпращане...' : 'Place order' }}
      </button>

      <p v-if="message" class="text-sm mt-2">{{ message }}</p>
    </div>
  </div>
</template>