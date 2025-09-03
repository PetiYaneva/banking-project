import axios from "axios";

export function decodeJwt(token) {
  const [, payload] = String(token || "").split(".");
  if (!payload) return {};
  try { return JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/"))); }
  catch { return {}; }
}

export const http = axios.create({
  baseURL: "http://localhost:8080",
  headers: { "Content-Type": "application/json" }
});

http.interceptors.request.use(cfg => {
  const t = localStorage.getItem("token");
  if (t) cfg.headers.Authorization = `Bearer ${t}`;
  return cfg;
});

http.interceptors.response.use(
  r => r,
  err => {
    const url = err?.config?.baseURL + (err?.config?.url || "");
    const status = err?.response?.status;
    console.error("HTTP error:", status, url, err?.response?.data || err?.message);
    throw err;
  }
);
