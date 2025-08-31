<template>
  <div class="max-w-md mx-auto bg-white rounded-2xl shadow p-6 mt-10">
    <h1 class="text-2xl font-semibold mb-1">Вход</h1>
    <p class="text-sm text-gray-600 mb-6">Влез, за да продължиш.</p>

    <form class="grid gap-4" @submit.prevent="submit">
      <input class="inp" type="email" placeholder="Email" v-model="email" required />
      <input class="inp" type="password" placeholder="Password" v-model="password" required />
      <button class="btn" :disabled="loading">{{ loading ? "Влизам…" : "Влез" }}</button>
    </form>

    <p class="text-sm mt-4">
      Нямаш акаунт?
      <RouterLink to="/register" class="underline">Регистрация</RouterLink>
    </p>

    <p v-if="error" class="mt-4 text-sm text-red-600">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuth } from "../stores/auth";

const router = useRouter();
const auth = useAuth();

const email = ref("");
const password = ref("");
const loading = ref(false);
const error = ref("");

async function submit() {
  try {
    loading.value = true;
    error.value = "";
    await auth.login({ email: email.value, password: password.value });

    if (auth.isAdmin) {
      router.push({ name: "admin-dashboard" });
    } else {
      router.push(auth.profileCompleted ? { name: "dashboard" } : { name: "before" });
    }
  } catch (e) {
    error.value = e?.response?.data?.message || "Invalid credentials";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.inp { @apply w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring; }
.btn { @apply w-full px-4 py-2 rounded-lg bg-black text-white hover:opacity-90 disabled:opacity-60; }
</style>
