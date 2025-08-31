import { http } from "./http";                // ← именуван импорт
import { EventSourcePolyfill } from "event-source-polyfill";

// Live prices via SSE
export function openLiveStream(onMessage) {
  const token = localStorage.getItem("token");
  if (!token) return () => {};
  const es = new EventSourcePolyfill("/api/crypto/live", {
    headers: { Authorization: `Bearer ${token}` },
    heartbeatTimeout: 60000,
  });
  es.onmessage = (e) => {
    try { onMessage?.(JSON.parse(e.data)); } catch {}
  };
  es.onerror = (e) => console.warn("[SSE] error", e);
  return () => es.close();
}

export async function getSimplePrices(idsCsv, vs = "usd") {
  return http.get(`/api/crypto/simple-price`, { params: { ids: idsCsv, vs } });
}

// /api/crypto/history/{id}/{vs}/{days}
export async function getHistory(id, vs = "usd", days = 7) {
  const normId   = String(id).toLowerCase().trim();
  const normVs   = (String(vs).toLowerCase().trim() || "usd");
  const normDays = (String(days).trim() || "7");
  return http.get(`/api/crypto/history/${normId}/${normVs}/${normDays}`);
}
