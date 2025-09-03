<script setup>
import { ref, computed, onMounted } from "vue";
import { useAuth } from "../stores/auth";
import { getUserAccounts } from "../api/account";
import { getLoansByUser, assessRisk, applyLoan } from "../api/loans";

const auth = useAuth();
const userId = computed(() => auth.user?.id || auth.userId);

const accounts = ref([]);
const loans = ref([]);
const expandId = ref(null);

const loadingAccounts = ref(false);
const loadingLoans = ref(false);
const assessing = ref(false);
const applying = ref(false);
const message = ref("");

// формата за заявка
const form = ref({
  repaymentAccountId: "",   // ще задаваме ID на сметката (UUID)
  totalAmount: null,
  termMonths: null,
  interestRate: 0.04,       // по подразбиране 4% (0.04)
  startDate: ""             // ISO yyyy-mm-dd
});

// налични опции за годишна лихва
const interestOptions = [0.03, 0.04, 0.05, 0.06];

// резултат от risk assess
const risk = ref(null); // {score, riskClass, recommendation, dti, monthlyPayment, ...}

// зареждане на сметки за падащия списък
async function loadAccounts() {
  if (!userId.value) return;
  loadingAccounts.value = true;
  try {
    const { data } = await getUserAccounts(userId.value);
    accounts.value = Array.isArray(data) ? data : [];
    // ако още няма избрана сметка – предложи първата
    if (!form.value.repaymentAccountId && accounts.value.length) {
      form.value.repaymentAccountId = accounts.value[0].id;
    }
  } catch (e) {
    console.error("loadAccounts error:", e);
    accounts.value = [];
  } finally {
    loadingAccounts.value = false;
  }
}

// зареждане на кредити (user)
async function loadLoans() {
  if (!userId.value) return;
  loadingLoans.value = true;
  try {
    const { data } = await getLoansByUser(userId.value);
    // бекендът връща Optional<List<Loan>> → защитено четене
    loans.value = Array.isArray(data) ? data : (data?.content ?? []);
  } catch (e) {
    console.error("loadLoans error:", e);
    loans.value = [];
  } finally {
    loadingLoans.value = false;
  }
}

function validate() {
  if (!form.value.repaymentAccountId) return "Моля, избери сметка.";
  if (!form.value.totalAmount || Number(form.value.totalAmount) <= 0) return "Въведи коректна обща сума.";
  if (!form.value.termMonths || Number(form.value.termMonths) <= 0) return "Въведи коректен срок (месеци).";
  if (!form.value.interestRate || Number(form.value.interestRate) < 0.01) return "Избери годишна лихва.";
  return "";
}

function buildPayload() {
  return {
    userId: userId.value,
    totalAmount: Number(form.value.totalAmount),
    interestRate: Number(form.value.interestRate),   
    termMonths: Number(form.value.termMonths),
    finalDate: null,                                  
    repaymentAccountId: form.value.repaymentAccountId,
    monthlyIncome: null, monthlyObligations: null,  
    employmentYears: 0, employmentType: null, creditHistory: null, collateral: null,
  };
}

async function onAssess() {
  message.value = "";
  risk.value = null;
  const err = validate();
  if (err) { message.value = err; return; }

  assessing.value = true;
  try {
    const payload = buildPayload();
    const { data } = await assessRisk(payload);
    risk.value = data || null;
    if (!risk.value) message.value = "Няма върнат риск резултат.";
  } catch (e) {
    console.error("assess error:", e);
    message.value = e?.response?.data?.message || "Грешка при оценка на риска.";
  } finally {
    assessing.value = false;
  }
}

async function onApply() {
  message.value = "";
  const err = validate();
  if (err) { message.value = err; return; }

  applying.value = true;
  try {
    const payload = buildPayload();
    const { data } = await applyLoan(payload);
    if (data?.approved) {
      message.value = "Заявката е одобрена.";
      await loadLoans();
      risk.value = null;
    } else {
      message.value = data?.recommendation || "Заявката е подадена (очаква одобрение).";
    }
  } catch (e) {
    console.error("apply error:", e);
    message.value = e?.response?.data?.message || "Грешка при заявката.";
  } finally {
    applying.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadAccounts(), loadLoans()]);
});
</script>

<template>
  <div class="space-y-6">
    <!-- Форма: Risk + Apply -->
    <div class="bg-white border rounded-2xl p-5 max-w-3xl">
      <h2 class="font-semibold mb-3">Нов кредит</h2>

      <div class="grid sm:grid-cols-2 gap-3 text-sm">
        <label class="block">
          <span class="text-slate-600">Сметка (за погасяване)</span>
          <select v-model="form.repaymentAccountId" class="mt-1 w-full rounded-md border px-3 py-2">
            <option v-for="a in accounts" :key="a.id" :value="a.id">
              {{ a.iban }} — {{ a.accountType }} ({{ Number(a.balance||0).toFixed(2) }} {{ a.currencyCode || 'BGN' }})
            </option>
          </select>
        </label>

        <label class="block">
          <span class="text-slate-600">Обща сума (BGN)</span>
          <input type="number" step="0.01" v-model.number="form.totalAmount"
                 class="mt-1 w-full border rounded-md px-3 py-2" />
        </label>

        <label class="block">
          <span class="text-slate-600">Срок (месеци)</span>
          <input type="number" v-model.number="form.termMonths"
                 class="mt-1 w-full border rounded-md px-3 py-2" />
        </label>

        <label class="block">
          <span class="text-slate-600">Годишна лихва</span>
          <select v-model.number="form.interestRate" class="mt-1 w-full rounded-md border px-3 py-2">
            <option v-for="r in interestOptions" :key="r" :value="r">{{ (r*100).toFixed(0) }}%</option>
          </select>
        </label>

        <label class="block">
          <span class="text-slate-600">Начална дата</span>
          <input type="date" v-model="form.startDate"
                 class="mt-1 w-full border rounded-md px-3 py-2" />
        </label>
      </div>

      <div class="flex flex-wrap gap-2 mt-4">
        <button :disabled="assessing" @click="onAssess"
                class="inline-flex items-center rounded-md bg-slate-700 text-white px-4 py-2 hover:bg-slate-800">
          {{ assessing ? "Оценяване..." : "Risk assess" }}
        </button>
        <button :disabled="applying" @click="onApply"
                class="inline-flex items-center rounded-md bg-blue-600 text-white px-4 py-2 hover:bg-blue-700">
          {{ applying ? "Изпращане..." : "Apply" }}
        </button>
      </div>

      <p v-if="message" class="text-sm mt-3">{{ message }}</p>

      <!-- Risk preview -->
      <div v-if="risk" class="mt-4 grid sm:grid-cols-2 gap-3">
        <div class="p-3 border rounded-lg bg-slate-50">
          <div class="text-slate-500 text-sm">Risk class</div>
          <div class="font-medium">{{ risk.riskClass || "-" }}</div>
        </div>
        <div class="p-3 border rounded-lg bg-slate-50">
          <div class="text-slate-500 text-sm">Score</div>
          <div class="font-medium">{{ risk.score ?? "-" }}</div>
        </div>
        <div class="p-3 border rounded-lg bg-slate-50">
          <div class="text-slate-500 text-sm">DTI</div>
          <div class="font-medium">{{ risk.dti ?? "-" }}</div>
        </div>
        <div class="p-3 border rounded-lg bg-slate-50">
          <div class="text-slate-500 text-sm">Estimated monthly</div>
          <div class="font-medium">{{ risk.monthlyPayment ?? "-" }}</div>
        </div>
        <div class="sm:col-span-2 p-3 border rounded-lg bg-slate-50">
          <div class="text-slate-500 text-sm">Recommendation</div>
          <div class="font-medium">{{ risk.recommendation || "-" }}</div>
        </div>
      </div>
    </div>

    <!-- Моите кредити -->
    <div class="bg-white border rounded-2xl p-5">
      <div class="flex items-center justify-between mb-3">
        <h2 class="font-semibold">Моите кредити</h2>
        <button class="text-sm text-slate-600 hover:text-slate-900 underline decoration-dotted"
                :disabled="loadingLoans" @click="loadLoans">
          {{ loadingLoans ? "Зареждане..." : "Обнови" }}
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
            <template v-for="l in loans" :key="l.id">
              <tr class="border-t align-top">
                <td class="py-2">{{ l.id }}</td>
                <td class="py-2 whitespace-nowrap">
                  {{ l.account?.iban || l.accountId || "-" }}
                </td>
                <td class="py-2 tabular-nums">{{ (l.totalAmount ?? 0).toLocaleString("bg-BG") }} $</td>
                <td class="py-2 tabular-nums">{{ (l.monthlyPayment ?? 0).toLocaleString("bg-BG") }} $</td>
                <td class="py-2 tabular-nums">{{ (l.remainingBalance ?? 0).toLocaleString("bg-BG") }} $</td>
                <td class="py-2 whitespace-nowrap">{{ l.nextPaymentDate || "-" }}</td>
                <td class="py-2 whitespace-nowrap">{{ l.finalLoanDate || "-" }}</td>
                <td class="py-2">
                  <button class="px-2 py-1 rounded-md border hover:bg-slate-50"
                          @click="expandId = expandId === l.id ? null : l.id">
                    {{ expandId === l.id ? "Скрий" : "Покажи" }}
                  </button>
                </td>
              </tr>

              <tr v-show="expandId === l.id" class="border-t bg-slate-50/60">
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
                      <div class="font-medium">{{ l.startDate || form.startDate || "-" }}</div>
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
            </template>

            <tr v-if="!loadingLoans && loans.length===0">
              <td colspan="8" class="p-3 text-slate-500">Няма кредити.</td>
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
