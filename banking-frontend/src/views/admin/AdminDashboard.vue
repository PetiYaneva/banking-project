<template>
  <div class="space-y-6">
    <!-- KPI cards -->
    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <div class="p-4 rounded-2xl border shadow-sm"><p class="text-sm text-slate-500">Users</p><p class="text-2xl font-semibold">{{ show(stats.users) }}</p></div>
      <div class="p-4 rounded-2xl border shadow-sm"><p class="text-sm text-slate-500">Accounts</p><p class="text-2xl font-semibold">{{ show(stats.accounts) }}</p></div>
      <div class="p-4 rounded-2xl border shadow-sm"><p class="text-sm text-slate-500">Transactions (30d)</p><p class="text-2xl font-semibold">{{ show(stats.txn30) }}</p></div>
      <div class="p-4 rounded-2xl border shadow-sm"><p class="text-sm text-slate-500">Active loans</p><p class="text-2xl font-semibold">{{ show(stats.loans) }}</p></div>
    </div>

    <!-- Charts row -->
    <div class="grid gap-4 md:grid-cols-2">
      <!-- Transactions -->
      <div class="p-4 rounded-2xl border shadow-sm">
        <div class="flex items-center justify-between mb-3 gap-3">
          <div>
            <p class="text-sm font-medium">Transactions per day (last 30d)</p>
            <p class="text-xs text-slate-500">{{ txDates[0] }} → {{ txDates.at(-1) }}</p>
          </div>
          <div class="flex items-center gap-2">
            <label class="text-xs text-slate-500">Type</label>
            <select v-model="txChartType" @change="renderTxChart" class="border rounded px-2 py-1 text-sm">
              <option value="line">Line</option>
              <option value="bar">Bar</option>
            </select>
          </div>
        </div>
        <div class="chart-box"><canvas ref="txCanvas"></canvas></div>
      </div>

      <!-- Loans -->
      <div class="p-4 rounded-2xl border shadow-sm">
        <div class="flex items-center justify-between mb-3 gap-3">
          <div>
            <p class="text-sm font-medium">Loans by status</p>
            <p class="text-xs text-slate-500">ACTIVE / PAID_OFF / OVERDUE</p>
          </div>
        <div class="flex items-center gap-2">
            <label class="text-xs text-slate-500">Chart</label>
            <select v-model="loanChartType" @change="renderLoanChart" class="border rounded px-2 py-1 text-sm">
              <option value="doughnut">Doughnut</option>
              <option value="pie">Pie</option>
              <option value="polarArea">Polar area</option>
            </select>
          </div>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 items-center">
          <div class="chart-box"><canvas ref="loanCanvas"></canvas></div>
        </div>
      </div>
    </div>

    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
  </div>
</template>

<script setup>
import { onMounted, ref, nextTick, onBeforeUnmount } from 'vue';
import { getAdminStats, getAllTransactionsByPeriod, getLoansByStatus } from '../../api/admin';

/* Chart.js – ЯВНА РЕГИСТРАЦИЯ */
import {
  Chart,
  // контролери
  LineController, BarController, PieController, DoughnutController, PolarAreaController,
  // елементи
  ArcElement, LineElement, BarElement, PointElement,
  // скали
  CategoryScale, LinearScale, RadialLinearScale,
  // плъгини
  Tooltip, Legend, Filler, Title,
} from 'chart.js';

Chart.register(
  LineController, BarController, PieController, DoughnutController, PolarAreaController,
  ArcElement, LineElement, BarElement, PointElement,
  CategoryScale, LinearScale, RadialLinearScale,
  Tooltip, Legend, Filler, Title
);

const colors = {
  blue:  '#60a5fa',
  blueLine: 'rgba(14,165,233,1)',
  blueFill: 'rgba(14,165,233,0.15)',
  green: '#34d399',
  red:   '#f87171',
  gray:  '#94a3b8',
};

const stats = ref({});
const error = ref('');

const txCanvas = ref(null);
const loanCanvas = ref(null);
let txChart = null;
let loanChart = null;

const txDates = ref([]);
const txCounts = ref([]);
const loanCounts = ref({ ACTIVE: 0, PAID_OFF: 0, OVERDUE: 0 });

const txChartType = ref('line');
const loanChartType = ref('doughnut');

function show(v){ return (v===0 || Number.isFinite(v)) ? v : '—'; }
function isoDay(d){ return d.toISOString().slice(0,10); }
function addDays(d,n){ const x=new Date(d); x.setDate(x.getDate()+n); return x; }

/* ---------- TX CHART (fixed Y 0..niceMax) ---------- */
function createTxChart(type) {
  if (!txCanvas.value) return;
  const maxVal = Math.max(1, ...txCounts.value);
  const niceMax = Math.ceil(maxVal * 1.1);
  const step = Math.max(1, Math.ceil(niceMax / 6));

  txChart = new Chart(txCanvas.value.getContext('2d'), {
    type,
    data: {
      labels: txDates.value,
      datasets: [{
        label: 'Transactions',
        data: txCounts.value,
        borderColor: colors.blueLine,
        backgroundColor: type === 'line' ? colors.blueFill : colors.blue,
        fill: type === 'line',
        tension: 0.35,
        pointRadius: 0,
        borderWidth: 2,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false }, tooltip: { mode: 'index', intersect: false } },
      scales: {
        x: { grid: { color: 'rgba(148,163,184,0.15)' }, ticks: { autoSkip: true, maxTicksLimit: 6 } },
        y: { min: 0, suggestedMax: niceMax, grid: { color: 'rgba(148,163,184,0.15)' }, ticks: { stepSize: step } },
      },
    },
  });
}

function renderTxChart() {
  const type = txChartType.value;
  const maxVal = Math.max(1, ...txCounts.value);
  const niceMax = Math.ceil(maxVal * 1.1);
  const step = Math.max(1, Math.ceil(niceMax / 6));

  if (!txChart) { createTxChart(type); return; }
  if (txChart.config.type === type) {
    txChart.data.labels = txDates.value;
    txChart.data.datasets[0].data = txCounts.value;
    txChart.data.datasets[0].backgroundColor = type === 'line' ? colors.blueFill : colors.blue;
    txChart.data.datasets[0].fill = (type === 'line');
    txChart.options.scales.y.min = 0;
    txChart.options.scales.y.suggestedMax = niceMax;
    txChart.options.scales.y.ticks.stepSize = step;
    txChart.update();
  } else {
    txChart.destroy();
    createTxChart(type);
  }
}

/* ---------- LOAN CHART (smart update) ---------- */
function createLoanChart(type) {
  if (!loanCanvas.value) return;
  const labels = ['ACTIVE', 'PAID_OFF', 'OVERDUE'];
  const data = [loanCounts.value.ACTIVE, loanCounts.value.PAID_OFF, loanCounts.value.OVERDUE];

  loanChart = new Chart(loanCanvas.value.getContext('2d'), {
    type,
    data: { labels, datasets: [{ data, backgroundColor: [colors.blue, colors.green, colors.red], borderWidth: 0 }] },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: true, position: 'bottom', labels: { boxWidth: 10, usePointStyle: true } },
        tooltip: { callbacks: { label: (ctx)=> `${ctx.label}: ${ctx.raw}` } }
      },
      scales: type === 'polarArea' ? { r: { grid: { color: 'rgba(148,163,184,0.15)' } } } : {},
      cutout: type === 'doughnut' ? '60%' : undefined,
      animation: { animateRotate: true, animateScale: true },
    },
  });
}

function renderLoanChart() {
  const type = loanChartType.value;
  const labels = ['ACTIVE', 'PAID_OFF', 'OVERDUE'];
  const data = [loanCounts.value.ACTIVE, loanCounts.value.PAID_OFF, loanCounts.value.OVERDUE];

  if (!loanChart) { createLoanChart(type); return; }
  if (loanChart.config.type === type) {
    loanChart.data.labels = labels;
    loanChart.data.datasets[0].data = data;
    loanChart.update();
  } else {
    loanChart.destroy();
    createLoanChart(type);
  }
}

/* --------- load data --------- */
onMounted(async () => {
  try {
    stats.value = await getAdminStats();

    const end = new Date();
    const start = new Date(); start.setDate(end.getDate()-29);
    const tx = await getAllTransactionsByPeriod(isoDay(start), isoDay(end));

    const labels = []; const map = new Map();
    for(let d=new Date(start); d<=end; d=addDays(d,1)){ const k=isoDay(d); labels.push(k); map.set(k,0); }
    tx.forEach(t=>{ const k=isoDay(new Date(t.createdOn||t.createdAt||t.date)); if(map.has(k)) map.set(k,map.get(k)+1); });
    txDates.value = labels;
    txCounts.value = labels.map(k=>map.get(k)||0);

    const [a,p,o] = await Promise.all([ getLoansByStatus('ACTIVE'), getLoansByStatus('PAID_OFF'), getLoansByStatus('OVERDUE') ]);
    loanCounts.value = { ACTIVE:(a||[]).length, PAID_OFF:(p||[]).length, OVERDUE:(o||[]).length };

    await nextTick();
    renderTxChart();
    renderLoanChart();

    // просто resize (без recreate)
    window.addEventListener('resize', onResize, { passive: true });
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load dashboard';
    console.error(e);
  }
});

function onResize(){ if (txChart) txChart.resize(); if (loanChart) loanChart.resize(); }

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize);
  if (txChart) txChart.destroy();
  if (loanChart) loanChart.destroy();
});
</script>

<style scoped>
/* фиксиран, приятен размер на картите с графики */
.chart-box{
  height: 320px;               /* мобилни / sm */
}
@media (min-width: 1024px){
  .chart-box{ height: 380px; } /* ≥lg */
}

/* canvas запълва контейнера изцяло */
.chart-box canvas{
  width: 100% !important;
  height: 100% !important;
  display: block;
}
</style>
