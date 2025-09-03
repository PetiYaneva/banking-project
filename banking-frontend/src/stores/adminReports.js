import { defineStore } from "pinia";
import {
  getOverviewStats,
  getMonthlyTransactions,
  getIncomeExpenseSeries,
  getLoansSummary,
  getTopExpenseCategories,
  getActiveUsersSeries,
} from "../api/admin";

export const useAdminReports = defineStore("adminReports", {
  state: () => ({
    loading: false,
    error: "",
    overview: null,
    monthlyTx: [],
    incomeExpense: [],
    loans: null,
    topCategories: [],
    activeUsers: [],
  }),
  actions: {
    async loadAll() {
      this.loading = true;
      this.error = "";
      try {
        const [o, tx, ie, ls, cat, au] = await Promise.all([
          getOverviewStats(),
          getMonthlyTransactions(),
          getIncomeExpenseSeries(),
          getLoansSummary(),
          getTopExpenseCategories(),
          getActiveUsersSeries(),
        ]);
        this.overview = o.data;
        this.monthlyTx = tx.data || [];
        this.incomeExpense = ie.data || [];
        this.loans = ls.data;
        this.topCategories = cat.data || [];
        this.activeUsers = au.data || [];
      } catch (e) {
        console.error(e);
        this.error = e?.response?.data?.message || e.message || "Failed to load admin stats";
      } finally {
        this.loading = false;
      }
    },
  },
});
