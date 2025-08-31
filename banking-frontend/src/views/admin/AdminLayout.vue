<template>
  <div class="min-h-screen grid grid-cols-[260px_1fr]">
    <AdminSidebar class="border-r" />
    <main class="min-h-screen flex flex-col">
      <AppHeader />
      <div class="p-6">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import AdminSidebar from "../../components/AdminSideBar.vue";
import AppHeader from "../../components/AppHeader.vue";
</script>
