import { http } from "./http";

function iso(d) { return d.toISOString().slice(0, 10); }


export async function getAdminStats() {
  const today = new Date();
  const from = new Date(today);
  from.setDate(today.getDate() - 30);

  const [usersRes, accountsRes, loansRes, txRes] = await Promise.allSettled([
    http.get(`/admin/users`),
    http.get(`/account/admin/sorted/balance-desc`),
    http.get(`/loans/admin/status/ACTIVE`),
    http.get(`/transactions/admin/period`, { params: { startDate: iso(from), endDate: iso(today) } }),
  ]);

  const users    = usersRes.status    === "fulfilled" ? (usersRes.value.data    || []) : [];
  const accounts = accountsRes.status === "fulfilled" ? (accountsRes.value.data || []) : [];
  const loans    = loansRes.status    === "fulfilled" ? (loansRes.value.data    || []) : [];
  const tx       = txRes.status       === "fulfilled" ? (txRes.value.data       || []) : [];

  return {
    users: users.length,
    accounts: accounts.length,
    loans: loans.length,
    txn30: tx.length,
  };
}

export async function getAdminUsersAll() {
  const { data } = await http.get(`/admin/users`);
  return data || [];
}

export async function getAdminUsersById(id) {
  const { data } = await http.get(`/admin/users/${id}`);
  return data ? [data] : [];
}

export async function getAdminUsersByPeriod(from, to) {
  const { data } = await http.get(`/admin/users/period`, { params: { from, to } });
  return data || [];
}

export async function searchAdminUsersByEmail(emailPart) {
  const { data } = await http.get(`/admin/users/search`, { params: { email: emailPart } });
  return data || [];
}

export async function getAdminUsersUncompleted() {
  const { data } = await http.get(`/admin/users/uncompleted`);
  return data || [];
}

/* -------------------- ACCOUNTS -------------------- */

export async function getAccountsByUser(userId) {
  const { data } = await http.get(`/account/${userId}/accounts`);
  return data || [];
}

export async function getAccountByIban(iban) {
  const { data } = await http.get(`/account/${iban}`);
  return Array.isArray(data) ? data : [data].filter(Boolean);
}

export async function getAccountByIdAdmin(id) {
  const { data } = await http.get(`/account/admin/id/${id}`);
  return Array.isArray(data) ? data : [data].filter(Boolean);
}

export async function getAccountsHighBalance(min) {
  const { data } = await http.get(`/account/admin/high-balance`, { params: { min } });
  return data || [];
}

export async function getAccountsLowBalance(max) {
  const { data } = await http.get(`/account/admin/low-balance`, { params: { max } });
  return data || [];
}

export async function getAccountsByCurrency(curr) {
  const { data } = await http.get(`/account/admin/currency/${curr}`);
  return data || [];
}

export async function getAccountsSortedBalanceDesc() {
  const { data } = await http.get(`/account/admin/sorted/balance-desc`);
  return data || [];
}

export async function getAccountsSortedCreatedAsc() {
  const { data } = await http.get(`/account/admin/sorted/created-asc`);
  return data || [];
}

/* -------------------- LOANS -------------------- */

export async function getLoansByUser(userId) {
  const { data } = await http.get(`/loans/loans`, { params: { userId } });
  return data || [];
}

export async function getLoansByStatus(status) {
  // status: ACTIVE | PAID_OFF | OVERDUE
  const { data } = await http.get(`/loans/admin/status/${status}`);
  return data || [];
}

export async function getLoansByStatusAndNextPayment(status, from, to) {
  const { data } = await http.get(`/loans/admin/status/${status}/next-payment`, {
    params: { from, to },
  });
  return data || [];
}

export async function getLoansAppliedBetween(from, to) {
  const { data } = await http.get(`/loans/admin/applied-between`, { params: { from, to } });
  return data || [];
}

export async function getLoansFinalBetween(from, to) {
  const { data } = await http.get(`/loans/admin/final-between`, { params: { from, to } });
  return data || [];
}

export async function getLoansInterestBetween(minRate, maxRate) {
  const { data } = await http.get(`/loans/admin/interest-between`, { params: { minRate, maxRate } });
  return data || [];
}

export async function getLoansTotalBetween(minAmount, maxAmount) {
  const { data } = await http.get(`/loans/admin/total-between`, { params: { minAmount, maxAmount } });
  return data || [];
}

export async function getLoansRemainingGte(minRemaining) {
  const { data } = await http.get(`/loans/admin/remaining/gte`, { params: { minRemaining } });
  return data || [];
}

export async function getLoansRemainingLte(maxRemaining) {
  const { data } = await http.get(`/loans/admin/remaining/lte`, { params: { maxRemaining } });
  return data || [];
}

export async function getLoansMissedPaymentsGt(minMissed) {
  const { data } = await http.get(`/loans/admin/missed-payments`, { params: { minMissed } });
  return data || [];
}

export async function getLoansDue(date) {
  const { data } = await http.get(`/loans/admin/due`, { params: { date } });
  return data || [];
}

export async function getLoansByRepaymentAccount(accountId) {
  const { data } = await http.get(`/loans/admin/repayment/account/${accountId}`);
  return data || [];
}

export async function getLoansByRepaymentIban(iban) {
  const { data } = await http.get(`/loans/admin/repayment/iban/${iban}`);
  return data || [];
}

export async function getLoansSortedNextPaymentAsc() {
  const { data } = await http.get(`/loans/admin/sorted/next-payment-asc`);
  return data || [];
}

export async function getLoansSortedRemainingDesc() {
  const { data } = await http.get(`/loans/admin/sorted/remaining-desc`);
  return data || [];
}

/* -------------------- TRANSACTIONS (ADMIN) -------------------- */

export async function getAllTransactions() {
  const { data } = await http.get(`/transactions/admin/all`);
  return data || [];
}

export async function getAllTransactionsByPeriod(startDate, endDate) {
  const { data } = await http.get(`/transactions/admin/period`, { params: { startDate, endDate } });
  return data || [];
}

export async function getAllTransactionsByPeriodAndType(startDate, endDate, type) {
  const { data } = await http.get(`/transactions/admin/period/type`, {
    params: { startDate, endDate, type },
  });
  return data || [];
}

export async function getAllTransactionsByPeriodAndStatus(startDate, endDate, status) {
  const { data } = await http.get(`/transactions/admin/period/status`, {
    params: { startDate, endDate, status },
  });
  return data || [];
}

export async function getAllTransactionsByPeriodAndCurrency(startDate, endDate, currency) {
  const { data } = await http.get(`/transactions/admin/period/currency`, {
    params: { startDate, endDate, currency },
  });
  return data || [];
}

export async function getTransactionsByAccountAndPeriod(accountId, startDate, endDate) {
  const { data } = await http.get(`/transactions/admin/account/${accountId}/period`, {
    params: { startDate, endDate },
  });
  return data || [];
}

export async function getAllTransactionsByMinAmount(startDate, endDate, minAmount) {
  const { data } = await http.get(`/transactions/admin/period/min-amount`, {
    params: { startDate, endDate, minAmount },
  });
  return data || [];
}

export async function getAllTransactionsByAmountBetween(startDate, endDate, minAmount, maxAmount) {
  const { data } = await http.get(`/transactions/admin/period/amount-between`, {
    params: { startDate, endDate, minAmount, maxAmount },
  });
  return data || [];
}

// ADMIN: търсене в период
export async function searchAllTransactionsInPeriod(startDate, endDate, q) {
  const { data } = await http.get(`/transactions/admin/period/search`, {
    params: { startDate, endDate, q },
  });
  return data || [];
}