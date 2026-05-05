import type { CreateTeamData, Team, TeamActivity, UpdateTeamData } from "./types";

const API_BASE = "http://localhost:8080/api";

export type UserRole = "JUNIOR" | "COMPANY";

function buildHeaders(userId: string, userName: string, role: UserRole): HeadersInit {
  return {
    "Content-Type": "application/json",
    "X-User-Id": userId,
    "X-User-Name": userName,
    "X-User-Role": role
  };
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, init);
  if (!response.ok) {
    const payload = await response.json().catch(() => ({ message: "Request failed" }));
    throw new Error(payload.message ?? "Request failed");
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const contentLength = response.headers.get("content-length");
  if (contentLength === "0") {
    return undefined as T;
  }
  const text = await response.text();
  if (!text) {
    return undefined as T;
  }
  return JSON.parse(text) as T;
}

export async function createTeam(payload: CreateTeamData, userId: string, userName: string) {
  return request<Team>("/teams", {
    method: "POST",
    headers: buildHeaders(userId, userName, "JUNIOR"),
    body: JSON.stringify(payload)
  });
}

export async function getMyTeams(userId: string, userName: string) {
  return request<Team[]>("/teams/my", {
    headers: buildHeaders(userId, userName, "JUNIOR")
  });
}

export async function getTeams(search?: string) {
  const query = search ? `?search=${encodeURIComponent(search)}` : "";
  const page = await request<{ content: Team[] }>(`/teams${query}`);
  return page.content;
}

export async function getTeamById(teamId: string, role: UserRole, userId: string, userName: string) {
  return request<Team>(`/teams/${teamId}`, {
    headers: buildHeaders(userId, userName, role)
  });
}

export async function joinTeam(teamId: string, userId: string, userName: string) {
  return request<void>(`/teams/${teamId}/join`, {
    method: "POST",
    headers: buildHeaders(userId, userName, "JUNIOR")
  });
}

export async function leaveTeam(teamId: string, userId: string, userName: string) {
  return request<void>(`/teams/${teamId}/leave`, {
    method: "POST",
    headers: buildHeaders(userId, userName, "JUNIOR")
  });
}

export async function updateTeam(teamId: string, payload: UpdateTeamData, userId: string, userName: string) {
  return request<Team>(`/teams/${teamId}`, {
    method: "PUT",
    headers: buildHeaders(userId, userName, "JUNIOR"),
    body: JSON.stringify(payload)
  });
}

export async function deleteTeam(teamId: string, userId: string, userName: string) {
  return request<void>(`/teams/${teamId}`, {
    method: "DELETE",
    headers: buildHeaders(userId, userName, "JUNIOR")
  });
}

export async function removeMember(teamId: string, memberUserId: string, userId: string, userName: string) {
  return request<void>(`/teams/${teamId}/members/${memberUserId}`, {
    method: "DELETE",
    headers: buildHeaders(userId, userName, "JUNIOR")
  });
}

export async function getTeamActivity(userId: string, userName: string, role: UserRole, limit = 20) {
  return request<TeamActivity[]>(`/teams/activity?limit=${limit}`, {
    headers: buildHeaders(userId, userName, role)
  });
}
