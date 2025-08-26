import { createRouter, createWebHistory } from "vue-router";
import { useAuth } from "../stores/auth";

const routes = [
  { path: "/login",    name: "login",    component: () => import("../views/Login.vue"),    meta: { public: true, guestOnly: true } },
  { path: "/register", name: "register", component: () => import("../views/Register.vue"), meta: { public: true, guestOnly: true } },
  { path: "/before",   name: "before",   component: () => import("../views/HomeBeforeComplete.vue"), meta: { public: true } },

  { path: "/complete-profile", name: "complete-profile", component: () => import("../views/CompleteProfile.vue"), meta: { public: true }, alias: ["/complete"] },

  {
    path: "/",
    component: () => import("../components/AppLayout.vue"),
    children: [
      { path: "",            name: "landing",      component: () => import("../views/Landing.vue"),     meta: { public: true, guestOnly: true } }, 
      { path: "dashboard",   name: "dashboard",    component: () => import("../views/Dashboard.vue") },
      { path: "accounts",    name: "accounts",     component: () => import("../views/Accounts.vue") },
      { path: "transfer",    name: "transfer",     component: () => import("../views/Transfer.vue") },
      { path: "transactions",name: "transactions", component: () => import("../views/Transactions.vue") },
      { path: "loans",       name: "loans",        component: () => import("../views/Loans.vue") },
    ],
  },

  { path: "/logout", name: "logout", meta: { public: true }, beforeEnter: () => { const a = useAuth(); a.logout?.(); return { name: "landing" }; } },

  { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const auth = useAuth();

  if (typeof auth.bootstrap === "function" && !auth.initialized) {
    try { await auth.bootstrap(); } catch {}
  }

  const isPublic    = to.matched.some(r => r.meta?.public);
  const isGuestOnly = to.matched.some(r => r.meta?.guestOnly);
  const hasToken    = !!auth?.token;
  const completed   = !!auth?.profileCompleted;

  if (isGuestOnly && hasToken && completed) {
    return { name: "dashboard", replace: true };
  }

  if (!isPublic && !hasToken) {
    return { name: "login", replace: true };
  }

  if (hasToken && !completed && !["before", "complete-profile"].includes(to.name)) {
    return { name: "before", replace: true };
  }

  if (hasToken && completed && ["landing", "login", "register", "before", "complete-profile"].includes(to.name)) {
    return { name: "dashboard", replace: true };
  }

  return true;
});

export default router;
