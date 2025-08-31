import { http } from "./http";

export const placeOrder = (payload) =>
  http.post(`/api/crypto/trade/order`, payload);

export const getPortfolioByUser = (userId, vs = "usd") =>
  http.get(`/api/crypto/trade/portfolio/by-user`, { params: { userId, vs } });

export const getOrdersByAccount = (accountId) =>
  http.get(`/api/crypto/trade/orders/by-account`, { params: { accountId } });

export const getOrdersByUser = (userId) =>
  http.get(`/api/crypto/trade/orders/by-user`, { params: { userId } });
