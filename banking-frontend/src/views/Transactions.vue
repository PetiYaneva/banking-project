<script setup>
import { ref, computed, onMounted } from "vue";
import { getUserTransactions } from "../api/transactions";
import { useAuth } from "../stores/auth";

const auth = useAuth();
const userId = computed(() => auth.user?.id || auth.userId);

const loading = ref(false);
const rows = ref([]);

const type = ref("ALL"); // INCOME/EXPENSE/TRANSFER/ALL
const from = ref("");
const to = ref("");
const min = ref(null);
const max = ref(null);
const q = ref(""); // търсене в описание/категория/контрагент (ако има)

const filtered = computed(() => {
  return rows.value.filter(r => {
    const passType = type.value === "ALL" || r.type === type.value;
    const d = r.date ? new Date(r.date) : null;
    const passFrom = !from.value || (d && d >= new Date(from.value));
    const passTo = !to.value || (d && d <= new Date(to.value));
    const passMin = min.value == null || (r.amount != null && r.amount >= +min.value);
    const passMax = max.value == null || (r.amount != null && r.amount <= +max.value);
    const txt = (r.description || r.merchant || r.category || "").toLowerCase();
    const passQ = !q.value || txt.includes(q.value.toLowerCase());
    return passType && passFrom && passTo && passMin && passMax && passQ;
  });
});

async function load() {
  if (!userId.value) return;
  loading.value = true;
  try {
    const { data } = await getUserTransactions(userId.value);
    rows.value = Array.isArray(data) ? data : [];
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="bg-white border rounded-2xl p-5">
    <div class="flex items-center justify-between mb-3">
      <h2 class="font-semibold">Транзакции</h2>
      <button class="text-sm underline decoration-dotted" :disabled="loading" @click="load">
        {{ loading ? "Зареждане..." : "Обнови" }}
      </button>
    </div>

    <div class="grid md:grid-cols-6 gap-3 mb-3 text-sm">
      <select v-model="type" class="border rounded-md px-3 py-2">
        <option value="ALL">Всички</option>
        <option value="INCOME">INCOME</option>
        <option value="EXPENSE">EXPENSE</option>
        <option value="TRANSFER">TRANSFER</option>
      </select>
      <input type="date" v-model="from" class="border rounded-md px-3 py-2" />
      <input type="date" v-model="to" class="border rounded-md px-3 py-2" />
      <input type="number" step="0.01" v-model.number="min" class="border rounded-md px-3 py-2" placeholder="Мин. сума" />
      <input type="number" step="0.01" v-model.number="max" class="border rounded-md px-3 py-2" placeholder="Макс. сума" />
      <input v-model="q" class="border rounded-md px-3 py-2" placeholder="Търсене (описание/категория)" />
    </div>

    <div class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500">
            <th class="py-2">Дата</th>
            <th class="py-2">Тип</th>
            <th class="py-2">Категория</th>
            <th class="py-2">Описание</th>
            <th class="py-2">IBAN</th>
            <th class="py-2 text-right">Сума</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in filtered" :key="t.id" class="border-t">
            <td class="py-2 whitespace-nowrap">{{ t.date || '-' }}</td>
            <td class="py-2">{{ t.type }}</td>
            <td class="py-2">{{ t.category || '-' }}</td>
            <td class="py-2">{{ t.description || '-' }}</td>
            <td class="py-2">{{ t.accountIban || t.iban || '-' }}</td>
            <td class="py-2 text-right" :class="t.type==='EXPENSE' ? 'text-rose-600' : t.type==='INCOME' ? 'text-emerald-600' : ''">
              {{ (t.amount ?? 0).toLocaleString('bg-BG', { minimumFractionDigits: 2 }) }} $
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>