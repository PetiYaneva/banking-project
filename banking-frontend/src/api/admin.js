// src/api/admin.js
import { http } from "./http";

// helper за ISO дати yyyy-mm-dd
const iso = (d) => d.toISOString().slice(0, 10);

// централен префикс
const API = "/api";

/* -------------------- STATS -------------------- */

// Ако имаш бекенд ендпойнт /api/admin/stats – използвай него.
// Иначе тази функция агрегира по наличните админ ендпойнти като fallback.
export async function getAdminStats() {
  const today = new Date();
  const from = new Date(today);
  from.setDate(today.getDate() - 30);

  const [usersRes, accountsRes, loansRes, txRes] = await Promise.allSettled([
    http.get(`${API}/admin/users`), // всички потребители
    http.get(`${API}/account/admin/sorted/balance-desc`), // всички акаунти
    http.get(`${API}/loans/admin/status/ACTIVE`), // активни кредити
    http.get(`${API}/transactions/admin/period`, {
      params: { startDate: iso(from), endDate: iso(today) },
    }), // транзакции 30д
  ]);

  const users    = usersRes.status    === "fulfilled" ? (usersRes.value.data    || []) : [];
  const accounts = accountsRes.status === "fulfilled" ? (accountsRes.value.data || []) : [];
  const loans    = loansRes.status    === "fulfilled" ? (loansRes.value.data    || []) : [];
  const tx       = txRes.status       === "fulfilled" ? (txRes.value.data       || []) : [];

  return { users: users.length, accounts: accounts.length, loans: loans.length, txn30: tx.length };
}

/* -------------------- USERS -------------------- */

export async function getAdminUsersAll() {
  const { data } = await http.get(`${API}/admin/users`);
  return data || [];
}

export async function getAdminUsersById(id) {
  const { data } = await http.get(`${API}/admin/users/${id}`);
  // бекенд връща един User → правим масив за таблицата
  return data ? [data] : [];
}

export async function getAdminUsersByPeriod(from, to) {
  const { data } = await http.get(`${API}/admin/users/period`, { params: { from, to } });
  return data || [];
}

export async function searchAdminUsersByEmail(emailPart) {
  const { data } = await http.get(`${API}/admin/users/search`, { params: { email: emailPart } });
  return data || [];
}

export async function getAdminUsersUncompleted() {
  const { data } = await http.get(`${API}/admin/users/uncompleted`);
  return data || [];
}

/* -------------------- ACCOUNTS -------------------- */

export async function getAccountsByUser(userId) {
  const { data } = await http.get(`${API}/account/${userId}/accounts`);
  return data || [];
}

export async function getAccountByIban(iban) {
  const { data } = await http.get(`${API}/account/${iban}`);
  // бекенд връща 1 акаунт – правим го на масив за таблицата
  return Array.isArray(data) ? data : [data].filter(Boolean);
}

export async function getAccountByIdAdmin(id) {
  const { data } = await http.get(`${API}/account/admin/id/${id}`);
  return Array.isArray(data) ? data : [data].filter(Boolean);
}

export async function getAccountsHighBalance(min) {
  const { data } = await http.get(`${API}/account/admin/high-balance`, { params: { min } });
  return data || [];
}

export async function getAccountsLowBalance(max) {
  const { data } = await http.get(`${API}/account/admin/low-balance`, { params: { max } });
  return data || [];
}

export async function getAccountsByCurrency(curr) {
  const { data } = await http.get(`${API}/account/admin/currency/${curr}`);
  return data || [];
}

export async function getAccountsSortedBalanceDesc() {
  const { data } = await http.get(`${API}/account/admin/sorted/balance-desc`);
  return data || [];
}

export async function getAccountsSortedCreatedAsc() {
  const { data } = await http.get(`${API}/account/admin/sorted/created-asc`);
  return data || [];
}

/* -------------------- LOANS (ADMIN + USER) -------------------- */

export async function getLoansByUser(userId) {
  const { data } = await http.get(`${API}/loans/loans`, { params: { userId } });
  return data || [];
}

// ADMIN: по статус
export async function getLoansByStatus(status) {
  const { data } = await http.get(`${API}/loans/admin/status/${status}`);
  return data || [];
}

// ADMIN: по статус + интервал за next payment
export async function getLoansByStatusAndNextPayment(status, from, to) {
  const { data } = await http.get(`${API}/loans/admin/status/${status}/next-payment`, {
    params: { from, to },
  });
  return data || [];
}

// ADMIN: подадени между дати
export async function getLoansAppliedBetween(from, to) {
  const { data } = await http.get(`${API}/loans/admin/applied-between`, { params: { from, to } });
  return data || [];
}

// ADMIN: финална дата между
export async function getLoansFinalBetween(from, to) {
  const { data } = await http.get(`${API}/loans/admin/final-between`, { params: { from, to } });
  return data || [];
}

// ADMIN: лихва между
export async function getLoansInterestBetween(minRate, maxRate) {
  const { data } = await http.get(`${API}/loans/admin/interest-between`, { params: { minRate, maxRate } });
  return data || [];
}

// ADMIN: totalAmount между
export async function getLoansTotalBetween(minAmount, maxAmount) {
  const { data } = await http.get(`${API}/loans/admin/total-between`, { params: { minAmount, maxAmount } });
  return data || [];
}

// ADMIN: remaining >= X
export async function getLoansRemainingGte(minRemaining) {
  const { data } = await http.get(`${API}/loans/admin/remaining/gte`, { params: { minRemaining } });
  return data || [];
}

// ADMIN: remaining <= X
export async function getLoansRemainingLte(maxRemaining) {
  const { data } = await http.get(`${API}/loans/admin/remaining/lte`, { params: { maxRemaining } });
  return data || [];
}

// ADMIN: пропуснати > N
export async function getLoansMissedPaymentsGt(minMissed) {
  const { data } = await http.get(`${API}/loans/admin/missed-payments`, { params: { minMissed } });
  return data || [];
}

// ADMIN: дължими към дата (обекти)
export async function getLoansDue(date) {
  const { data } = await http.get(`${API}/loans/admin/due`, { params: { date } });
  return data || [];
}

// ADMIN: по акаунт за погасяване
export async function getLoansByRepaymentAccount(accountId) {
  const { data } = await http.get(`${API}/loans/admin/repayment/account/${accountId}`);
  return data || [];
}

// ADMIN: по IBAN за погасяване
export async function getLoansByRepaymentIban(iban) {
  const { data } = await http.get(`${API}/loans/admin/repayment/iban/${iban}`);
  return data || [];
}

// ADMIN: сортирано
export async function getLoansSortedNextPaymentAsc() {
  const { data } = await http.get(`${API}/loans/admin/sorted/next-payment-asc`);
  return data || [];
}
export async function getLoansSortedRemainingDesc() {
  const { data } = await http.get(`${API}/loans/admin/sorted/remaining-desc`);
  return data || [];
}

/* -------------------- TRANSACTIONS (ADMIN) -------------------- */

// ADMIN: всички (внимание – може да е много)
export async function getAllTransactions() {
  const { data } = await http.get(`${API}/transactions/admin/all`);
  return data || [];
}

// ADMIN: по период (startDate, endDate – ISO yyyy-mm-dd)
export async function getAllTransactionsByPeriod(startDate, endDate) {
  const { data } = await http.get(`${API}/transactions/admin/period`, {
    params: { startDate, endDate },
  });
  return data || [];
}

// ADMIN: по период + тип
export async function getAllTransactionsByPeriodAndType(startDate, endDate, type) {
  const { data } = await http.get(`${API}/transactions/admin/period/type`, {
    params: { startDate, endDate, type },
  });
  return data || [];
}

// ADMIN: по период + статус
export async function getAllTransactionsByPeriodAndStatus(startDate, endDate, status) {
  const { data } = await http.get(`${API}/transactions/admin/period/status`, {
    params: { startDate, endDate, status },
  });
  return data || [];
}

// ADMIN: по период + валута
export async function getAllTransactionsByPeriodAndCurrency(startDate, endDate, currency) {
  const { data } = await http.get(`${API}/transactions/admin/period/currency`, {
    params: { startDate, endDate, currency },
  });
  return data || [];
}

// ADMIN: по акаунт + период
export async function getTransactionsByAccountAndPeriod(accountId, startDate, endDate) {
  const { data } = await http.get(`${API}/transactions/admin/account/${accountId}/period`, {
    params: { startDate, endDate },
  });
  return data || [];
}

// ADMIN: по период + minAmount
export async function getAllTransactionsByMinAmount(startDate, endDate, minAmount) {
  const { data } = await http.get(`${API}/transactions/admin/period/min-amount`, {
    params: { startDate, endDate, minAmount },
  });
  return data || [];
}

// ADMIN: по период + amount between
export async function getAllTransactionsByAmountBetween(startDate, endDate, minAmount, maxAmount) {
  const { data } = await http.get(`${API}/transactions/admin/period/amount-between`, {
    params: { startDate, endDate, minAmount, maxAmount },
  });
  return data || [];
}

// ADMIN: търсене в период
export async function searchAllTransactionsInPeriod(startDate, endDate, q) {
  const { data } = await http.get(`${API}/transactions/admin/period/search`, {
    params: { startDate, endDate, q },
  });
  return data || [];
}
