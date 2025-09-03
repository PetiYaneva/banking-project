import http from "./http";

export async function getEmploymentTypes() {
  const { data } = await http.get("/api/enums/employment");
  return data;
}
