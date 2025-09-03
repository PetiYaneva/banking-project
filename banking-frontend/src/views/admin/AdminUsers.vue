<script setup>
import { ref, onMounted, computed } from 'vue';
import {
  getAdminUsersAll,
  getAdminUsersById,
  getAdminUsersByPeriod,
  searchAdminUsersByEmail,
  getAdminUsersUncompleted,
} from '../../api/admin';

const mode = ref('ALL'); // ALL | ID | PERIOD | EMAIL | UNCOMPLETED

// inputs per mode
const id = ref('');
const from = ref(''); // datetime-local
const to = ref('');
const email = ref('');

const loading = ref(false);
const rows = ref([]);
const errorMsg = ref('');

// helpers
function toIsoDateTimeLocal(dt) {
  // от <input type="datetime-local"> идва 'YYYY-MM-DDTHH:mm'
  if (!dt) return '';
  return dt.length === 16 ? dt + ':00' : dt;
}
function formatDateTime(x) { return x ? new Date(x).toLocaleString() : ''; }
function displayName(u) {
  if (u && typeof u.name === 'string' && u.name.trim()) return u.name;
  const full = [u?.firstName, u?.lastName].filter(Boolean).join(' ').trim();
  return full || '(n/a)';
}
function displayRoles(u) {
  if (Array.isArray(u?.roles)) return u.roles.join(', ');
  if (Array.isArray(u?.authorities)) return u.authorities.join(', ');
  return u?.role || '';
}

async function reload() {
  loading.value = true; errorMsg.value = ''; rows.value = [];
  try {
    switch (mode.value) {
      case 'ID': {
        const v = id.value.trim();
        rows.value = v ? await getAdminUsersById(v) : [];
        break;
      }
      case 'PERIOD': {
        const f = toIsoDateTimeLocal(from.value);
        const t = toIsoDateTimeLocal(to.value);
        rows.value = (f && t) ? await getAdminUsersByPeriod(f, t) : [];
        break;
      }
      case 'EMAIL': {
        const v = email.value.trim();
        rows.value = v ? await searchAdminUsersByEmail(v) : [];
        break;
      }
      case 'UNCOMPLETED': {
        rows.value = await getAdminUsersUncompleted();
        break;
      }
      default: { // ALL
        rows.value = await getAdminUsersAll();
      }
    }
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e?.message || 'Failed to load users';
    console.error(e);
  } finally { loading.value = false; }
}

const showId = computed(() => mode.value === 'ID');
const showPeriod = computed(() => mode.value === 'PERIOD');
const showEmail = computed(() => mode.value === 'EMAIL');

onMounted(reload);
</script>

<template>
  <section class="space-y-4">
    <h1 class="text-xl font-semibold">Users</h1>

    <!-- Filter bar -->
    <div class="flex flex-wrap gap-2 items-end">
      <label class="text-sm">
        Filter
        <select v-model="mode" class="border rounded px-2 py-1">
          <option value="ALL">All</option>
          <option value="ID">By ID</option>
          <option value="PERIOD">By period</option>
          <option value="EMAIL">By email contains</option>
          <option value="UNCOMPLETED">Uncompleted profiles</option>
        </select>
      </label>

      <label v-if="showId" class="text-sm">
        User ID
        <input v-model="id" class="border rounded px-2 py-1 font-mono text-xs" placeholder="UUID" />
      </label>

      <template v-if="showPeriod">
        <label class="text-sm">
          From
          <input v-model="from" type="datetime-local" class="border rounded px-2 py-1" />
        </label>
        <label class="text-sm">
          To
          <input v-model="to" type="datetime-local" class="border rounded px-2 py-1" />
        </label>
      </template>

      <label v-if="showEmail" class="text-sm">
        Email contains
        <input v-model="email" class="border rounded px-2 py-1" placeholder="e.g. @gmail.com" />
      </label>

      <button @click="reload" class="h-9 px-4 rounded bg-slate-900 text-white">Apply</button>
    </div>

    <p v-if="errorMsg" class="text-red-600 text-sm">{{ errorMsg }}</p>

    <!-- Table -->
    <div class="overflow-auto border rounded-2xl">
      <table class="w-full text-sm">
        <thead class="bg-slate-50">
          <tr>
            <th class="p-2 text-left">ID</th>
            <th class="p-2 text-left">Name</th>
            <th class="p-2 text-left">Email</th>
            <th class="p-2 text-left">Roles</th>
            <th class="p-2 text-left">Created</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in rows" :key="u.id" class="border-t">
            <td class="p-2 font-mono text-xs">{{ u.id }}</td>
            <td class="p-2">{{ displayName(u) }}</td>
            <td class="p-2">{{ u.email }}</td>
            <td class="p-2">{{ displayRoles(u) }}</td>
            <td class="p-2">{{ formatDateTime(u.createdAt || u.createdOn) }}</td>
          </tr>
          <tr v-if="!loading && rows.length===0 && !errorMsg">
            <td class="p-3" colspan="5">No data</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
