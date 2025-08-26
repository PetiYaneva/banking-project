<template>
  <div class="min-h-screen bg-white">
    <NavBar/>
    <main class="max-w-6xl mx-auto p-4">
      <transition name="fade" mode="out-in">
        <router-view />
      </transition>
    </main>
  </div>
</template>
<script setup>
import NavBar from "../components/NavBar.vue";
</script>
