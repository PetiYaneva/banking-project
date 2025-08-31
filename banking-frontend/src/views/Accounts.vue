<script setup>
import { ref, computed, onMounted } from "vue";
import { getUserAccounts, getUserTotalBalance, createAccount } from "../api/account";
import { useAuth } from "../stores/auth";

const auth = useAuth(); 
const userId = computed(() => auth.user?.id || auth.userId);

const loading = ref(false);
const accounts = ref([]);
const total = ref(null);
const q = ref("");             
const type = ref("ALL");

const newAcc = ref({ accountType: "SAVING", initialBalance: 0 });
const creating = ref(false);
const msg = ref("");

const filtered = computed(() =>
  accounts.value.filter(a => {
    const passType = type.value === "ALL" || a.accountType === type.value;
    const passQ = !q.value || a.iban?.toLowerCase().includes(q.value.toLowerCase());
    return passType && passQ;
  })
);

async function load() {
  if (!userId.value) return;
  loading.value = true;
  try {
    const [acc, bal] = await Promise.all([
      getUserAccounts(userId.value),
      getUserTotalBalance(userId.value),
    ]);
    accounts.value = acc.data || [];
    total.value = bal.data ?? null;
  } finally {
    loading.value = false;
  }
}

async function createNew() {
  creating.value = true; msg.value = "";
  try {
    const { data } = await createAccount(newAcc.value);
    accounts.value.unshift(data);
    msg.value = "Сметката е създадена успешно.";
    newAcc.value = { accountType: "SAVING", initialBalance: 0 };
  } catch (e) {
    msg.value = e?.response?.data?.message || "Грешка при създаване.";
  } finally {
    creating.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="space-y-6">
    <div class="bg-white border rounded-2xl p-5">
      <div class="flex items-center justify-between mb-3">
        <h2 class="font-semibold">Сметки</h2>
        <button class="text-sm underline decoration-dotted" :disabled="loading" @click="load">
          {{ loading ? "Зареждане..." : "Обнови" }}
        </button>
      </div>

      <div class="grid sm:grid-cols-3 gap-3 mb-3">
        <input v-model="q" class="border rounded-md px-3 py-2 text-sm" placeholder="Търси по IBAN" />
        <select v-model="type" class="border rounded-md px-3 py-2 text-sm">
          <option value="ALL">Всички типове</option>
            <option value="DEPOSIT">DEPOSIT</option>
            <option value="CREDIT">CREDIT</option>
            <option value="SAVING">SAVING</option>
        </select>
        <div class="text-sm flex items-center">
          <span class="text-slate-500 mr-1">Общ баланс:</span>
          <strong v-if="total !== null">{{ total.toLocaleString('bg-BG') }} BGN</strong>
          <span v-else class="text-slate-400">—</span>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="text-left text-slate-500">
              <th class="py-2">IBAN</th>
              <th class="py-2">Тип</th>
              <th class="py-2">Баланс</th>
              <th class="py-2">Създадена</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in filtered" :key="a.id || a.iban" class="border-t">
              <td class="py-2 font-medium">{{ a.iban }}</td>
              <td class="py-2">{{ a.accountType }}</td>
              <td class="py-2 tabular-nums">{{ (a.balance ?? 0).toLocaleString('bg-BG') }} BGN</td>
              <td class="py-2 whitespace-nowrap">{{ a.createdAt || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Създай сметка -->
    <div class="bg-white border rounded-2xl p-5 max-w-xl">
      <h3 class="font-semibold mb-3">Нова сметка</h3>
      <div class="grid sm:grid-cols-2 gap-3 text-sm">
        <label>
          Тип
          <select v-model="newAcc.accountType" class="mt-1 w-full border rounded-md px-3 py-2">
            <option value="DEPOSIT">DEPOSIT</option>
            <option value="CREDIT">CREDIT</option>
            <option value="SAVING">SAVING</option>
          </select>
        </label>
        <label>
          Начален баланс (BGN)
          <input type="number" step="0.01" v-model.number="newAcc.initialBalance" class="mt-1 w-full border rounded-md px-3 py-2"/>
        </label>
      </div>
      <button :disabled="creating" @click="createNew" class="mt-3 rounded-md bg-blue-600 text-white px-4 py-2 hover:bg-blue-700">
        {{ creating ? "Създаване..." : "Създай" }}
      </button>
      <p v-if="msg" class="text-sm mt-2">{{ msg }}</p>
    </div>
  </div>
</template>

<style scoped>
.tabular-nums{ font-variant-numeric: tabular-nums; }
</style>
