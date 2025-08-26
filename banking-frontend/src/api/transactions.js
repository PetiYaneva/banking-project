import { http } from "./http";
export const createTransaction = (payload) => http.post("/api/transactions/new", payload);
export const getTransactionById = (id) => http.get(`/api/transactions/${id}`);
export const getUserTransactions = (userId) => http.get(`/api/transactions/user/${userId}`);
export const getByType = (userId, type) => http.get(`/api/transactions/type`, { params:{ userId, type } });
export const getByPeriod = (userId, startDate, endDate) =>
  http.get(`/api/transactions/period`, { params:{ userId, startDate, endDate } });
export const getIncomeSummary = (userId, monthsBack=6) =>
  http.get(`/api/transactions/income/summary`, { params:{ userId, monthsBack } });
export const getExpenseSummary = (userId, monthsBack=6) =>
  http.get(`/api/transactions/expense/summary`, { params:{ userId, monthsBack } });
