import { http } from "./http";

export const completeProfile = (payload) =>
  http.post("/api/users/profile/complete", payload);

export const isProfileCompleted = async () => {
  const { data } = await http.get("/api/users/profile/completed");
  return typeof data === "boolean" ? data : !!data?.completed;
};
