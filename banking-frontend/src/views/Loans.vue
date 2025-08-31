<script setup>
import { ref, computed, onMounted } from "vue";
import { useAuth } from "../stores/auth";
import { getUserAccounts } from "../api/account"; 
import http from "../api/http";

const auth = useAuth();
const userId = computed(() => auth.user?.id || auth.userId);
const loading = ref(false);
const listLoading = ref(false);
const message = ref("");
const loans = ref([]);
const expandId = ref(null);

const form = ref({
  accountRef: "", 
  totalAmount: null,
  termMonths: null,
  interestAnnual: null,
  startDate: ""
});

form.value.annualInterestRate = form.value.interestAnnual ?? form.value.annualInterestRate ?? null;
delete form.value.interestAnnual;

const accounts = ref([]);
async function loadAccounts() {
  if (!userId.value) return;
  try {
    const { data } = await getUserAccounts(userId.value);
    accounts.value = Array.isArray(data) ? data : [];
  } catch (e) {
    accounts.value = [];
    console.error("loadAccounts error:", e);
  }
}

async function submit() {
  message.value = "";
  if (!form.value.accountRef) {
    message.value = "Моля, избери сметка (IBAN или ID).";
    return;
  }
  if (!form.value.totalAmount || !form.value.termMonths || !form.value.annualInterestRate) {
    message.value = "Моля, попълни сума, срок и годишна лихва.";
    return;
  }

  const payload = {
    accountRef: form.value.accountRef,                 // IBAN или UUID
    totalAmount: Number(form.value.totalAmount),
    termMonths: Number(form.value.termMonths),
    annualInterestRate: Number(form.value.annualInterestRate),
    startDate: form.value.startDate || null,           // ISO yyyy-mm-dd от input[type=date]
  };

  loading.value = true;
  try {
    const { data } = await http.post("/api/loans/apply", payload);
    message.value = data?.approved
      ? "Заявката е одобрена."
      : "Заявката е подадена.";
    await loadLoans();
  } catch (e) {
    message.value = e?.response?.data?.message || "Грешка при заявката.";
    console.error("apply loan error:", e);
  } finally {
    loading.value = false;
  }
}

async function loadLoans() {
  if (!userId.value) return;
  listLoading.value = true;
  try {
    const { data } = await http.get(`/api/loans ${userId.value}`);
    loans.value = Array.isArray(data) ? data : (data?.content ?? []);
  } catch (e) {
    loans.value = [];
    console.error("load loans error:", e);
  } finally {
    listLoading.value = false;
  }
}

onMounted(() => {
  loadLoans();
  loadAccounts();
});
</script>


<template>
  <div class="space-y-6">
    <div class="bg-white border rounded-2xl p-5 max-w-2xl">
      <h2 class="font-semibold mb-3">Нов кредит</h2>
      <div class="grid sm:grid-cols-2 gap-3 text-sm">
        <div>
          <label class="text-sm text-slate-600">Account ID / IBAN</label>
          <input
            v-model="form.accountRef"
            list="accountsDatalist"            
            type="text"
            class="mt-1 w-full rounded-md border px-3 py-2"
            placeholder="UUID или IBAN"
          />
          <datalist id="accountsDatalist">
            <option
              v-for="a in accounts"
              :key="a.id"
              :value="a.iban || a.id"
            >
              {{ a.accountType }} — {{ a.iban }} ({{ Number(a.balance||0).toFixed(2) }} {{ a.currencyCode || 'BGN' }})
            </option>
          </datalist>
        </div>


        <label>
          Обща сума (BGN)
          <input type="number" step="0.01" v-model.number="form.totalAmount" class="mt-1 w-full border rounded-md px-3 py-2" />
        </label>

        <label>
          Срок (месеци)
          <input type="number" v-model.number="form.termMonths" class="mt-1 w-full border rounded-md px-3 py-2" />
        </label>

        <label>
          Годишна лихва (%)
          <input type="number" step="0.01" v-model.number="form.annualInterestRate" class="mt-1 w-full border rounded-md px-3 py-2" />
        </label>

        <label>
          Начална дата
          <input type="date" v-model="form.startDate" class="mt-1 w-full border rounded-md px-3 py-2" />
        </label>
      </div>

      <button
        :disabled="loading"
        @click="submit"
        class="mt-4 inline-flex items-center justify-center rounded-md bg-blue-600 text-white px-4 py-2 hover:bg-blue-700"
      >
        {{ loading ? "Изпращане..." : "Заяви кредит" }}
      </button>

      <p v-if="message" class="text-sm mt-3">{{ message }}</p>
    </div>

    <div class="bg-white border rounded-2xl p-5">
      <div class="flex items-center justify-between mb-3">
        <h2 class="font-semibold">Моите кредити</h2>
        <button
          class="text-sm text-slate-600 hover:text-slate-900 underline decoration-dotted"
          :disabled="listLoading"
          @click="loadLoans"
        >
          {{ listLoading ? "Зареждане..." : "Обнови" }}
        </button>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="text-left text-slate-500">
              <th class="py-2">ID</th>
              <th class="py-2">Account</th>
              <th class="py-2">Обща сума</th>
              <th class="py-2">Месечна вноска</th>
              <th class="py-2">Оставащ баланс</th>
              <th class="py-2">Следваща дата</th>
              <th class="py-2">Финална дата</th>
              <th class="py-2">Детайли</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="l in loans" :key="l.id" class="border-t align-top">
              <td class="py-2">{{ l.id }}</td>
              <td class="py-2">
                <div class="whitespace-nowrap">{{ l.accountId || l.account?.iban || '-' }}</div>
              </td>
              <td class="py-2 tabular-nums">{{ (l.totalAmount ?? 0).toLocaleString("bg-BG") }} $</td>
              <td class="py-2 tabular-nums">{{ (l.monthlyPayment ?? 0).toLocaleString("bg-BG") }} $</td>
              <td class="py-2 tabular-nums">{{ (l.remainingBalance ?? 0).toLocaleString("bg-BG") }} $</td>
              <td class="py-2 whitespace-nowrap">{{ l.nextPaymentDate || "-" }}</td>
              <td class="py-2 whitespace-nowrap">{{ l.finalLoanDate || "-" }}</td>
              <td class="py-2">
                <button
                  class="px-2 py-1 rounded-md border hover:bg-slate-50"
                  @click="expandId = expandId === l.id ? null : l.id"
                >
                  {{ expandId === l.id ? "Скрий" : "Покажи" }}
                </button>
              </td>
            </tr>
            <tr v-for="l in loans" v-show="expandId === l.id" :key="l.id + '-details'" class="border-t bg-slate-50/60">
              <td colspan="8" class="p-3">
                <div class="grid sm:grid-cols-3 gap-3 text-sm">
                  <div class="p-3 border rounded-lg bg-white">
                    <div class="text-slate-500">Лихва (год.)</div>
                    <div class="font-medium">{{ l.annualInterestRate ?? form.interestRate }} %</div>
                  </div>
                  <div class="p-3 border rounded-lg bg-white">
                    <div class="text-slate-500">Срок</div>
                    <div class="font-medium">{{ l.termMonths ?? form.termMonths }} месеца</div>
                  </div>
                  <div class="p-3 border rounded-lg bg-white">
                    <div class="text-slate-500">Старт</div>
                    <div class="font-medium">{{ l.startDate || form.startDate }}</div>
                  </div>
                </div>

                <div v-if="l.schedule?.length" class="mt-3">
                  <div class="text-slate-600 mb-1">Погасителен план</div>
                  <div class="overflow-x-auto">
                    <table class="w-full text-xs">
                      <thead>
                        <tr class="text-left text-slate-500">
                          <th class="py-1">№</th>
                          <th class="py-1">Дата</th>
                          <th class="py-1">Вноска</th>
                          <th class="py-1">Главница</th>
                          <th class="py-1">Лихва</th>
                          <th class="py-1">Остатък</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(r, i) in l.schedule" :key="i" class="border-t">
                          <td class="py-1">{{ i + 1 }}</td>
                          <td class="py-1">{{ r.date }}</td>
                          <td class="py-1 tabular-nums">{{ (r.installment ?? 0).toLocaleString('bg-BG') }} $</td>
                          <td class="py-1 tabular-nums">{{ (r.principal ?? 0).toLocaleString('bg-BG') }} $</td>
                          <td class="py-1 tabular-nums">{{ (r.interest ?? 0).toLocaleString('bg-BG') }} $</td>
                          <td class="py-1 tabular-nums">{{ (r.remaining ?? 0).toLocaleString('bg-BG') }} $</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>

                <div v-else class="mt-2 text-xs text-slate-500">
                  Няма предоставен погасителен план от бекенда.
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

    </div>
  </div>
</template>

<style scoped>
.tabular-nums { font-variant-numeric: tabular-nums; }
table { border-collapse: collapse; }
th, td { padding: 0.25rem 0.5rem; }
</style>
