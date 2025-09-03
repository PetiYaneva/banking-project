<template>
  <nav class="space-y-1">
    <RouterLink
      v-for="item in items"
      :key="item.to"
      :to="item.to"
      class="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-slate-100"
      :class="{ 'bg-slate-900 text-white hover:bg-slate-800': $route.path.startsWith(item.to) }"
    >
      <span>{{ item.icon }}</span>
      <span class="font-medium">{{ item.label }}</span>
    </RouterLink>
  </nav>
</template>

<script setup>
const items = [
  { to: "/admin/dashboard", label: "Dashboard", icon: "📊" },
  { to: "/admin/reports", label: "Reports", icon: "📈" },
  { to: "/admin/users", label: "Users", icon: "👤" },
  { to: "/admin/accounts", label: "Accounts", icon: "🏦" },
  { to: "/admin/loans", label: "Loans", icon: "💳" },
  { to: "/admin/transactions", label: "Transactions", icon: "🔁" },
];
</script>
