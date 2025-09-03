<!-- src/views/Transfer.vue -->
<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useAuth } from "../stores/auth";
import {
  getUserAccounts,
  transferMoney,
  getAccountTransactions,
} from "../api/account";

const auth = useAuth();
const userId = computed(() => auth.user?.id || auth.userId);

const loading = ref(false);
const submitting = ref(false);
const message = ref("");
const errorMsg = ref("");
const accounts = ref([]);
const transfers = ref([]);

// всички преводи са в лева
const CURRENCY = "BGN";

const form = ref({
  fromAccountId: "",
  toIban: "",
  amount: null,
  description: "",
  date: "",
});

// --- ФИЛТЪР ПО ТИП ---
const filterType = ref("ALL"); // ALL | TRANSFER | WITHDRAWAL | DEPOSIT

const selectedFrom = computed(() =>
  accounts.value.find((a) => a.id === form.value.fromAccountId)
);

function accLabel(a) {
  const iban = a.iban ? ` (${a.iban})` : "";
  const bal = Number(a.balance ?? 0).toFixed(2);
  return `${a.accountType || "ACCOUNT"}${iban} — ${bal} ${a.currency || "BGN"}`;
}

// ---------- Loaders ----------
async function loadAccounts() {
  if (!userId.value) return;
  const res = await getUserAccounts(userId.value);
  accounts.value = res.data || [];
  if (!form.value.fromAccountId && accounts.value.length) {
    form.value.fromAccountId = accounts.value[0].id;
  }
}

/** Нормализиране на типа от бекенда към: TRANSFER | WITHDRAWAL | DEPOSIT */
function normalizeType(raw) {
  const s = (raw || "").toString().toUpperCase();
  if (s.includes("WITHDRAW")) return "WITHDRAWAL";
  if (s.includes("DEPOSIT")) return "DEPOSIT";
  if (s.includes("TRANSFER")) return "TRANSFER";
  return "TRANSFER";
}

/** Нормализирай / намапвай транзакция от бекенда към общ формат */
function mapTx(t) {
  const type = normalizeType(t.transactionType) ||
    (t.expense ? "WITHDRAWAL" : t.income ? "DEPOSIT" : "TRANSFER");

  const description =
    t.description?.toString().trim() ||
    (type === "TRANSFER" ? "Transfer" : "");

  const amount = Number(t.amount ?? t.value ?? t.transactionAmount ?? 0);

  const date = (t.createdOn || t.date || t.createdAt || t.timestamp || "")
    .toString()
    .slice(0, 19)
    .replace("T", " ");

  const fromIban = t.fromIban || t.accountIban || t.senderIban || t.fromAccount?.iban || "";
  const toIban = t.toIban || t.receiverIban || t.toAccount?.iban || "";

  const status = (t.status || t.transactionStatus || "COMPLETED").toString();

  return { id: t.id, type, description, amount, date, fromIban, toIban, status };
}

async function loadTransfers() {
  transfers.value = [];
  if (!form.value.fromAccountId) return;
  const tx = await getAccountTransactions(form.value.fromAccountId);
  const list = Array.isArray(tx.data) ? tx.data : [];
  transfers.value = list.map(mapTx);
}

async function load() {
  loading.value = true;
  message.value = "";
  errorMsg.value = "";
  try {
    await loadAccounts();
    await loadTransfers();
  } finally {
    loading.value = false;
  }
}

// ---------- Submit ----------
function validate() {
  errorMsg.value = "";
  if (!form.value.fromAccountId) {
    errorMsg.value = "Избери сметка подател.";
    return false;
  }
  if (!form.value.toIban || form.value.toIban.trim().length < 10) {
    errorMsg.value = "Въведи валиден IBAN.";
    return false;
  }
  const amt = Number(form.value.amount);
  if (!amt || amt <= 0) {
    errorMsg.value = "Сумата трябва да е положително число.";
    return false;
  }
  return true;
}

async function submit() {
  if (!validate()) return;
  submitting.value = true;
  message.value = "";
  errorMsg.value = "";
  try {
    await transferMoney({
      senderIban: selectedFrom.value?.iban,
      receiverIban: form.value.toIban.trim(),
      amount: Number(form.value.amount),
      description: form.value.description?.trim() || "Transfer",
      currency: CURRENCY,
    });

    message.value = "Преводът е изпратен успешно.";
    form.value.toIban = "";
    form.value.amount = null;
    form.value.description = "";
    form.value.date = "";
    await Promise.all([loadAccounts(), loadTransfers()]);
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || "Грешка при превода.";
  } finally {
    submitting.value = false;
  }
}

// ---------- UI helpers ----------
function typeBadgeClasses(t) {
  // бледи цветове:
  if (t === "DEPOSIT") return "bg-emerald-50 text-emerald-700 border border-emerald-200";
  if (t === "WITHDRAWAL") return "bg-rose-50 text-rose-700 border border-rose-200";
  // TRANSFER – бледо жълто
  return "bg-amber-50 text-amber-700 border border-amber-200";
}

function amountTextClasses(t) {
  if (t === "DEPOSIT") return "text-emerald-700";
  if (t === "WITHDRAWAL") return "text-rose-700";
  return "text-amber-700"; // TRANSFER
}

function signedAmount(t) {
  if (t.type === "DEPOSIT") return `+${t.amount.toFixed(2)} BGN`;
  if (t.type === "WITHDRAWAL") return `-${t.amount.toFixed(2)} BGN`;
  return `${t.amount.toFixed(2)} BGN`; // трансфер – без знак
}

// Филтрирани за таблицата
const filteredTransfers = computed(() => {
  if (filterType.value === "ALL") return transfers.value;
  return transfers.value.filter((x) => x.type === filterType.value);
});

watch(() => form.value.fromAccountId, loadTransfers);
onMounted(load);
</script>

<template>
  <div class="p-6 space-y-6">
    <h1 class="text-2xl font-bold">Transfer</h1>

    <!-- Форма за превод -->
    <div class="bg-white border rounded-2xl p-5">
      <h2 class="font-semibold mb-3">Нов превод</h2>

      <div class="grid sm:grid-cols-2 gap-3 text-sm">
        <div>
          <label class="text-sm text-slate-600">From account</label>
          <select v-model="form.fromAccountId" class="mt-1 w-full rounded-md border px-3 py-2">
            <option value="" disabled>Избери сметка</option>
            <option v-for="a in accounts" :key="a.id" :value="a.id">
              {{ accLabel(a) }}
            </option>
          </select>
        </div>

        <div>
          <label class="text-sm text-slate-600">To IBAN</label>
          <input v-model="form.toIban" placeholder="BGxx XXXX ...." class="mt-1 w-full rounded-md border px-3 py-2" />
        </div>

        <div>
          <label class="text-sm text-slate-600">Amount (BGN)</label>
          <input v-model.number="form.amount" type="number" step="0.01" min="0" class="mt-1 w-full rounded-md border px-3 py-2" />
        </div>

        <div>
          <label class="text-sm text-slate-600">Description (optional)</label>
          <input v-model="form.description" maxlength="120" class="mt-1 w-full rounded-md border px-3 py-2" />
          <p class="text-xs text-slate-500 mt-1">Ако е празно, ще се запише като “Transfer”.</p>
        </div>

        <div>
          <label class="text-sm text-slate-600">Date (optional)</label>
          <input v-model="form.date" type="date" class="mt-1 w-full rounded-md border px-3 py-2" />
        </div>

        <div class="flex items-end">
          <button
            class="w-full sm:w-auto rounded-md bg-emerald-600 text-white px-4 py-2 hover:bg-emerald-700 disabled:opacity-60"
            :disabled="submitting"
            @click="submit"
          >
            {{ submitting ? "Изпращане..." : "Изпрати превод" }}
          </button>
        </div>
      </div>

      <p v-if="message" class="text-emerald-700 mt-3">{{ message }}</p>
      <p v-if="errorMsg" class="text-red-600 mt-3">{{ errorMsg }}</p>
    </div>

    <!-- История за избраната сметка -->
    <div class="bg-white border rounded-2xl p-5">
      <div class="flex items-center justify-between gap-3 flex-wrap">
        <h2 class="font-semibold">
          Транзакции за сметка:
          <span class="text-slate-600">{{ selectedFrom?.iban || "—" }}</span>
        </h2>

        <!-- Контроли за филтър -->
        <div class="flex items-center gap-2 text-sm">
          <label class="text-slate-600">Филтър:</label>
          <select v-model="filterType" class="rounded-md border px-2 py-1">
            <option value="ALL">Всички</option>
            <option value="DEPOSIT">DEPOSIT</option>
            <option value="WITHDRAWAL">WITHDRAWAL</option>
            <option value="TRANSFER">TRANSFER</option>
          </select>
          <button class="text-sm underline decoration-dotted" @click="loadTransfers" :disabled="loading">
            {{ loading ? "Зареждане..." : "Обнови" }}
          </button>
        </div>
      </div>

      <div class="overflow-x-auto mt-3">
        <table class="w-full text-sm">
          <thead class="text-left text-slate-500">
            <tr>
              <th class="py-2">Дата</th>
              <th class="py-2">Тип</th>
              <th class="py-2">Описание</th>
              <th class="py-2">От IBAN</th>
              <th class="py-2">Към IBAN</th>
              <th class="py-2 text-right">Сума</th>
              <th class="py-2">Статус</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in filteredTransfers" :key="t.id" class="border-t">
              <td class="py-2 whitespace-nowrap">{{ t.date || "-" }}</td>
              <td class="py-2">
                <span class="px-2 py-0.5 rounded-full text-xs" :class="typeBadgeClasses(t.type)">
                  {{ t.type }}
                </span>
              </td>
              <td class="py-2">{{ t.description || (t.type==='TRANSFER' ? 'Transfer' : '-') }}</td>
              <td class="py-2">{{ t.fromIban || "—" }}</td>
              <td class="py-2">{{ t.toIban || "—" }}</td>
              <td class="py-2 text-right tabular-nums" :class="amountTextClasses(t.type)">
                {{ signedAmount(t) }}
              </td>
              <td class="py-2">{{ t.status }}</td>
            </tr>

            <tr v-if="!loading && filteredTransfers.length === 0">
              <td colspan="7" class="py-4 text-slate-500">Няма транзакции за избрания филтър.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tabular-nums { font-variant-numeric: tabular-nums; }
</style>
