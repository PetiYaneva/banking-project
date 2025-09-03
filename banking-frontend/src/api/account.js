import { http } from "./http";
export const getUserAccounts = (userId) => http.get(`/api/account/${userId}/accounts`);
export const getUserTotalBalance = (userId) => http.get(`/api/account/${userId}/balance`);
export const createAccount = (payload) => http.post("/api/account/new", payload);
export const transferMoney = (payload) => http.post("/api/account/transfer", payload);
export const getAccountByIban = (iban) => http.get(`/api/account/${encodeURIComponent(iban)}`);
export const getAccountTransactions = (id) => http.get(`/api/account/${id}/transactions`);
