<template>
  <div class="space-y-6">
    <header class="flex items-center justify-between">
      <h1 class="text-2xl font-semibold">Reports</h1>
      <div class="flex gap-2">
        <select v-model="months" class="px-3 py-2 rounded-lg border">
          <option :value="3">Last 3 months</option>
          <option :value="6">Last 6 months</option>
          <option :value="12">Last 12 months</option>
        </select>
        <button class="px-3 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800" @click="reload" :disabled="loading">
          Reload
        </button>
      </div>
    </header>

    <div v-if="error" class="p-4 rounded-lg bg-rose-50 text-rose-700 border border-rose-200">
      {{ error }}
    </div>

    <section class="grid lg:grid-cols-2 gap-6">
      <div class="p-4 rounded-2xl border shadow-sm">
        <p class="text-sm mb-2 text-slate-500">Income vs Expense (per month)</p>
        <LineChart :labels="ieLabels" :datasets="ieDatasets" />
      </div>
      <div class="p-4 rounded-2xl border shadow-sm">
        <p class="text-sm mb-2 text-slate-500">Top Expense Categories</p>
        <BarChart :labels="catLabels" :datasets="catDatasets" />
      </div>
      <div class="p-4 rounded-2xl border shadow-sm">
        <p class="text-sm mb-2 text-slate-500">Loans by Status</p>
        <DoughnutChart :labels="loanLabels" :datasets="loanDatasets" />
      </div>
      <div class="p-4 rounded-2xl border shadow-sm">
        <p class="text-sm mb-2 text-slate-500">Delinquency Buckets</p>
        <BarChart :labels="delqLabels" :datasets="delqDatasets" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import LineChart from "../../widgets/LineChart.vue";
import BarChart from "../../widgets/BarChart.vue";
import DoughnutChart from "../../widgets/DoughnutChart.vue";
import { getIncomeExpenseSeries, getTopExpenseCategories, getLoansSummary } from "../../api/admin";
import { useAdminReports } from "../../stores/adminReports";

const store = useAdminReports();
const loading = computed(() => store.loading);
const error = computed(() => store.error);
const months = ref(6);

const ie = ref([]);
const cats = ref([]);
const loans = ref(null);

const ieLabels = computed(() => ie.value.map(x => x.month));
const ieDatasets = computed(() => [
  { label: "Income (BGN)", data: ie.value.map(x => x.income) },
  { label: "Expense (BGN)", data: ie.value.map(x => x.expense) },
]);

const catLabels = computed(() => cats.value.map(x => x.category || x.merchant || "Other"));
const catDatasets = computed(() => [{ label: "Spend (BGN)", data: cats.value.map(x => x.amount) }]);

const loanLabels = computed(() => Object.keys(loans?.value?.byStatus || {}));
const loanDatasets = computed(() => [{ label: "Loans", data: loanLabels.value.map(k => loans.value.byStatus[k]) }]);

const delqLabels = computed(() => Object.keys(loans?.value?.delinquency || {}));
const delqDatasets = computed(() => [{ label: "Count", data: delqLabels.value.map(k => loans.value.delinquency[k]) }]);

async function reload(){
  try{
    store.loading = true;
    const [ieRes, catRes, loanRes] = await Promise.all([
      getIncomeExpenseSeries(months.value),
      getTopExpenseCategories(10, months.value),
      getLoansSummary(),
    ]);
    ie.value = ieRes.data || [];
    cats.value = catRes.data || [];
    loans.value = loanRes.data || { byStatus: {}, delinquency: {} };
  } catch(e){
    store.error = e?.response?.data?.message || e.message || "Failed to load";
  } finally {
    store.loading = false;
  }
}

watch(months, reload);
onMounted(reload);
</script>
