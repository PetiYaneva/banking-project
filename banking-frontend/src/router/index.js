import { createRouter, createWebHistory } from "vue-router";
import { useAuth } from "../stores/auth";
import { adminRoutes } from "./admin-routes";

const routes = [
  { path: "/login", name: "login", component: () => import("../views/Login.vue"), meta: { public: true, guestOnly: true } },
  { path: "/register", name: "register", component: () => import("../views/Register.vue"), meta: { public: true, guestOnly: true } },
  { path: "/before", name: "before", component: () => import("../views/HomeBeforeComplete.vue"), meta: { public: true } },
  { path: "/complete-profile", name: "complete-profile", component: () => import("../views/CompleteProfile.vue"), meta: { public: true }, alias: ["/complete"] },

  {
    path: "/",
    component: () => import("../components/AppLayout.vue"),
    children: [
      { path: "", name: "landing", component: () => import("../views/Landing.vue"), meta: { public: true, guestOnly: true } },
      { path: "dashboard", name: "dashboard", component: () => import("../views/Dashboard.vue") },
      { path: "accounts", name: "accounts", component: () => import("../views/Accounts.vue") },
      { path: "transfer", name: "transfer", component: () => import("../views/Transfer.vue") },
      { path: "transactions", name: "transactions", component: () => import("../views/Transactions.vue") },
      { path: "loans", name: "loans", component: () => import("../views/Loans.vue") },
      { path: "crypto/trade", name: "crypto-trade", component: () => import("../views/CryptoTrade.vue") },
      { path: "crypto/wallet", name: "crypto-wallet", component: () => import("../views/CryptoPortfolio.vue") },
      { path: "crypto/live", name: "crypto-live", component: () => import("../views/Crypto.vue") },
    ],
  },

  ...adminRoutes,

  { path: "/logout", name: "logout", meta: { public: true }, beforeEnter: () => { const a = useAuth(); a.logout?.(); return { name: "landing" }; } },
  { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const auth = useAuth();

  if (!auth.initialized && typeof auth.init === "function") {
    try { await auth.init(); } catch {}
  }

  const isPublic    = to.matched.some(r => r.meta?.public);
  const isGuestOnly = to.matched.some(r => r.meta?.guestOnly);
  const hasToken    = !!auth?.token;
  const isAdmin     = !!auth?.isAdmin;
  const completed   = isAdmin ? true : !!auth?.profileCompleted;

  if (hasToken && (to.name === "login" || to.name === "register" || to.name === "landing")) {
    return isAdmin
      ? { name: "admin-dashboard", replace: true }
      : { name: completed ? "dashboard" : "before", replace: true };
  }

  if (isGuestOnly && hasToken && completed)
    return isAdmin
      ? { name: "admin-dashboard", replace: true }
      : { name: "dashboard", replace: true };

  if (!isPublic && !hasToken)
    return { name: "login", replace: true };

  if (to.matched.some(r => r.meta?.requiresAdmin)) {
    if (!auth.isAdmin) return { name: "dashboard", replace: true };
    return true;
  }

  if (hasToken && !completed && !isAdmin && !["before", "complete-profile"].includes(to.name)) {
    return { name: "before", replace: true };
  }

  if (hasToken && completed && ["landing", "login", "register", "before", "complete-profile"].includes(to.name)) {
    return isAdmin
      ? { name: "admin-dashboard", replace: true }
      : { name: "dashboard", replace: true };
  }

  // админ на user dashboard → към admin dashboard
  if (auth.isAdmin && to.name === "dashboard") {
    return { name: "admin-dashboard", replace: true };
  }

  return true;
});

export default router;
