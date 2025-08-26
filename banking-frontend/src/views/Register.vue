<template>
  <div class="max-w-md mx-auto bg-white rounded-2xl shadow p-6 mt-10">
    <h1 class="text-2xl font-semibold mb-1">Регистрация</h1>
    <p class="text-sm text-gray-600 mb-6">Създай акаунт за секунди.</p>

    <form class="grid gap-4" @submit.prevent="submit">
      <input class="inp" type="email" placeholder="Email" v-model="form.email" required />
      <input class="inp" type="password" placeholder="Парола" v-model="form.password" required />

      <button class="btn" :disabled="loading">
        {{ loading ? "Създавам…" : "Регистрирай" }}
      </button>
    </form>

    <p class="text-sm mt-4">
      Имаш акаунт?
      <RouterLink to="/login" class="underline">Вход</RouterLink>
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

const loading = ref(false);
const error = ref("");
const form = ref({
  email: "",
  password: "",
});

async function submit() {
  try {
    loading.value = true;
    error.value = "";

    await auth.register({ email: form.value.email, password: form.value.password });
    router.push("/before"); 
  } catch (e) {
    error.value = e?.response?.data?.message || "Регистрацията не успя.";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.inp {
  @apply border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-black;
}
.btn {
  @apply bg-black text-white py-2 rounded-lg hover:bg-gray-800 disabled:opacity-50;
}
</style>