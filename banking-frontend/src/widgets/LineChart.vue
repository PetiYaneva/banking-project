<template>
  <div><canvas ref="canvas"></canvas></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from "vue";
import {
  Chart, LineController, LineElement, PointElement,
  LinearScale, CategoryScale, Tooltip, Legend,
} from "chart.js";

Chart.register(LineController, LineElement, PointElement, LinearScale, CategoryScale, Tooltip, Legend);

const props = defineProps({
  labels: { type: Array, default: () => [] },
  datasets: { type: Array, default: () => [] },
});

const canvas = ref(null);
let chart;

function draw() {
  if (chart) chart.destroy();
  const ds = (props.datasets || []).map(d => ({ ...d, fill: false, tension: 0.25 }));
  chart = new Chart(canvas.value, {
    type: "line",
    data: { labels: props.labels, datasets: ds },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: "index", intersect: false },
      plugins: { legend: { display: true } },
      scales: { x: { grid: { display: false } }, y: { beginAtZero: true } },
    },
  });
}

onMounted(draw);
onBeforeUnmount(() => { if (chart) chart.destroy(); });
watch(() => [props.labels, props.datasets], draw, { deep: true });
</script>

<style scoped>
div { height: 300px; }
</style>
