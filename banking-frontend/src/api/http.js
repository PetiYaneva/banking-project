// src/api/http.js
import axios from "axios";

export const http = axios.create({
  headers: { "Content-Type": "application/json" },
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// ↓↓↓ ДОБАВИ ТОВА ↓↓↓
export function decodeJwt(token) {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const json = atob(base64);
    // безопасно към UTF-8
    const utf8 = decodeURIComponent(
      Array.from(json, c => "%" + c.charCodeAt(0).toString(16).padStart(2, "0")).join("")
    );
    return JSON.parse(utf8); // { userId, role, profileCompleted, ... }
  } catch {
    return {};
  }
}

export default http;
