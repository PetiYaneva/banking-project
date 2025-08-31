// src/api/loans.js
import { http } from "./http";

// Вариант А: класически create/list (ако бекендът ти поддържа /api/loans)
export const createLoan = (payload) => http.post("/api/loans", payload);
export const getLoansByUser = (userId) => http.get("/api/loans", { params: { userId } });

// Вариант Б: risk / apply / obligations (ако това е твоят текущ бекенд)
export const assessRisk = (loanReq) => http.post("/api/loans/risk-assessment", loanReq);
export const applyLoan = (loanReq) => http.post("/api/loans/apply", loanReq);
export const getUserObligations = (userId) =>
  http.get(`/api/loans/obligations/user/${userId}`);
export const getLoanObligations = (loanId) =>
  http.get(`/api/loans/obligations/loan/${loanId}`);

