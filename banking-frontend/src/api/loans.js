import { http } from "./http";

export const getLoansByUser = (userId) =>
  http.get("/api/loans/loans", { params: { userId } });

export const assessRisk = (loanReq) =>
  http.post("/api/loans/risk-assessment", loanReq);

export const applyLoan = (loanReq) =>
  http.post("/api/loans/apply", loanReq);

export const getUserObligations = (userId) =>
  http.get(`/api/loans/obligations/user/${userId}`);

export const getLoanObligations = (loanId) =>
  http.get(`/api/loans/obligations/loan/${loanId}`);
