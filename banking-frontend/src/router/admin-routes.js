// src/router/admin-routes.js
export const adminRoutes = [
  {
    path: "/admin",
    component: () => import("../views/admin/AdminLayout.vue"),
    meta: { requiresAdmin: true },
    children: [
      { path: "", redirect: "/admin/dashboard" },
      { path: "dashboard",    name: "admin-dashboard",    component: () => import("../views/admin/AdminDashboard.vue") },
      { path: "users",        name: "admin-users",        component: () => import("../views/admin/AdminUsers.vue") },
      { path: "accounts",     name: "admin-accounts",     component: () => import("../views/admin/AdminAccounts.vue") },
      { path: "loans",        name: "admin-loans",        component: () => import("../views/admin/AdminLoans.vue") },
      { path: "transactions", name: "admin-transactions", component: () => import("../views/admin/AdminTransactions.vue") },
      { path: "reports",      name: "admin-reports",      component: () => import("../views/admin/AdminReports.vue") },
    ],
  },
];
