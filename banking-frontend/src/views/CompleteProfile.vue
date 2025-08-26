<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { completeProfile } from "../api/profile";
import { useAuth } from "../stores/auth";
import { EMPLOYMENT_OPTIONS } from "../constants/employment"; 

const router = useRouter();
const auth = useAuth();

const form = ref({
  firstName: "",
  lastName: "",
  dateOfBirth: "",
  phoneNumber: "",
  address: "",
  employment: EMPLOYMENT_OPTIONS[0].value, 
  declaredIncome: null,
});

const loading = ref(false);
const error = ref("");

async function submit() {
  try {
    loading.value = true;
    if (form.value.declaredIncome === null || form.value.declaredIncome === "") {
      form.value.declaredIncome = 0;
    }
    await completeProfile(form.value);
    auth.profileCompleted = true;
    router.push("/dashboard");

  } catch {
    error.value = "Could not complete profile.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="max-w-2xl mx-auto bg-white rounded-2xl shadow p-6">
    <h1 class="text-2xl font-semibold mb-2">Завърши профила</h1>
    <p class="text-sm text-gray-600 mb-6">Отключва всички функционалности.</p>

    <form class="grid gap-4" @submit.prevent="submit">
      <div class="grid md:grid-cols-2 gap-4">
        <div>
          <label class="lbl">First name</label>
          <input class="inp" v-model="form.firstName" required />
        </div>

        <div>
          <label class="lbl">Last name</label>
          <input class="inp" v-model="form.lastName" required />
        </div>

        <div>
          <label class="lbl">Date of birth</label>
          <input class="inp" type="date" v-model="form.dateOfBirth" required />
        </div>

        <div>
          <label class="lbl">Phone</label>
          <input class="inp" v-model="form.phoneNumber" />
        </div>

        <div class="md:col-span-2">
          <label class="lbl">Address</label>
          <input class="inp" v-model="form.address" />
        </div>

        <div>
          <label class="lbl">Employment</label>
          <select class="inp" v-model="form.employment">
            <option v-for="opt in EMPLOYMENT_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>

        <div>
          <label class="lbl">Declared income</label>
          <input
            class="inp"
            type="number"
            inputmode="decimal"
            min="0"
            step="0.01"
            v-model.number="form.declaredIncome"
            placeholder="напр. 1500.00"
          />
        </div>
      </div>

      <button class="btn w-fit" :disabled="loading">
        {{ loading ? "Записвам…" : "Запази и продължи" }}
      </button>
    </form>

    <p v-if="error" class="mt-4 text-sm text-red-600">{{ error }}</p>
  </div>
</template>

<style scoped>
.inp { @apply w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring; }
.lbl { @apply block text-sm text-gray-600 mb-1; }
.btn { @apply px-4 py-2 rounded-lg bg-black text-white hover:opacity-90 disabled:opacity-60; }
</style>
