import { http } from "./http";
export const placeOrder = (payload) => http.post(`/api/crypto/trade/order`, payload);
export const getPortfolioByAccount = (accountId, vs="usd") =>
  http.get(`/api/crypto/trade/portfolio/by-account`, { params:{ accountId, vs }});
export const getOrdersByAccount = (accountId) =>
  http.get(`/api/crypto/trade/orders/by-account`, { params:{ accountId }});
