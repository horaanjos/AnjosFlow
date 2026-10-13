import { PORTFOLIO_MODE } from "./environment";
export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public fields: Record<string, string> = {},
  ) {
    super(message);
  }
}
export const API_URL = (
  import.meta.env.VITE_API_URL || "http://localhost:8080"
).replace(/\/$/, "");
export async function api<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  if (PORTFOLIO_MODE)
    throw new ApiError(
      "Esta versão de portfólio não possui API publicada. Execute o projeto localmente para salvar dados.",
      0,
    );
  let response: Response;
  try {
    response = await fetch(`${API_URL}/api${path}`, {
      ...options,
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers,
      },
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError")
      throw error;
    throw new ApiError(
      "Não foi possível conectar à API. Confira se o Back-end está iniciado.",
      0,
    );
  }
  if (!response.ok) {
    const problem = (await response.json().catch(() => ({}))) as {
      detail?: string;
      errors?: Record<string, string>;
    };
    throw new ApiError(
      problem.detail || `A solicitação falhou (${response.status}).`,
      response.status,
      problem.errors,
    );
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}
export function send<T>(path: string, method: string, data?: unknown) {
  return api<T>(path, {
    method,
    ...(data === undefined ? {} : { body: JSON.stringify(data) }),
  });
}
export function query(params: Record<string, string>) {
  const filtered = Object.entries(params).filter(([, v]) => v !== "");
  return filtered.length ? "?" + new URLSearchParams(filtered).toString() : "";
}
