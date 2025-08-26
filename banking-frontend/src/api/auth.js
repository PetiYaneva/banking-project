import { http } from "./http";

export const login    = (payload) => http.post("/api/login", payload);
export const register = (payload) => http.post("/api/register", payload);