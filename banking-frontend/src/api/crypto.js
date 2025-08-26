import { http } from "./http";
export const getSimplePrices = (ids, vs="usd") =>
  http.get(`/api/crypto/simple-price`, { params:{ ids: Array.isArray(ids)? ids.join(","): ids, vs }});
export const getHistory = (id, vs="usd", days="30") =>
  http.get(`/api/crypto/history`, { params:{ id, vs, days }});

// Live prices (SSE)
export function openLiveStream(onMsg){
  const es = new EventSource("/api/crypto/live");
  es.onmessage = (e) => onMsg(JSON.parse(e.data));
  return () => es.close();
}
