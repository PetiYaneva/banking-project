<template>
  <div><canvas ref="canvas"></canvas></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from "vue";
import { Chart, DoughnutController, ArcElement, Tooltip, Legend } from "chart.js";

Chart.register(DoughnutController, ArcElement, Tooltip, Legend);

const props = defineProps({
  labels: { type: Array, default: () => [] },
  datasets: { type: Array, default: () => [] },
});

const canvas = ref(null);
let chart;

function draw() {
  if (chart) chart.destroy();
  chart = new Chart(canvas.value, {
    type: "doughnut",
    data: { labels: props.labels, datasets: props.datasets },
    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: "bottom" } } },
  });
}

onMounted(draw);
onBeforeUnmount(() => { if (chart) chart.destroy(); });
watch(() => [props.labels, props.datasets], draw, { deep: true });
</script>

<style scoped>
div { height: 300px; }
</style>
