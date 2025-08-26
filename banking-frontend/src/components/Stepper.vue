<script setup>
defineProps({
  steps: { type: Array, required: true },
  current: { type: Number, default: 1 } // 1-based
});
</script>

<template>
  <ol class="space-y-3">
    <li v-for="(s,i) in steps" :key="i" class="flex items-start gap-3">
      <div
        class="mt-0.5 grid place-items-center w-7 h-7 rounded-full border text-[13px] font-semibold"
        :class="i+1<=current ? 'bg-black text-white border-black' : 'border-gray-300 text-gray-500'"
      >
        {{ i+1 }}
      </div>
      <div class="flex-1 border-b border-gray-100 pb-3">
        <div class="font-semibold text-gray-900">{{ s.title }}</div>
        <div class="text-sm text-gray-500">{{ s.desc }}</div>
      </div>
    </li>
  </ol>
</template>
