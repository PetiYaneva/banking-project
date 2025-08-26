import { http } from "./http";
export const getIncomesByPeriod = (userId, startDate, endDate) =>
  http.get(`/api/incomes/user/${userId}/period`, { params:{ startDate, endDate }});
export const getExpensesByPeriod = (userId, startDate, endDate) =>
  http.get(`/api/expenses/user/${userId}/period`, { params:{ startDate, endDate }});
