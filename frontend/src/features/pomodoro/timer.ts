export type Mode = "focus" | "short" | "long";
export interface PendingSession {
  clientKey: string;
  subjectId: number;
  taskId: number | null;
  startedAt: string;
  endedAt: string;
}
export interface TimerState {
  cycleKey: string;
  mode: Mode;
  status: "idle" | "running" | "paused" | "finished";
  durationMs: number;
  remainingMs: number;
  deadline: number | null;
  cycles: number;
  subjectId: number | null;
  taskId: number | null;
  pending: PendingSession | null;
}
export function createTimer(
  mode: Mode,
  minutes: number,
  cycles = 0,
): TimerState {
  return {
    cycleKey: crypto.randomUUID(),
    mode,
    status: "idle",
    durationMs: minutes * 60000,
    remainingMs: minutes * 60000,
    deadline: null,
    cycles,
    subjectId: null,
    taskId: null,
    pending: null,
  };
}
export function remaining(state: TimerState, now: number) {
  return state.status === "running" && state.deadline !== null
    ? Math.max(0, state.deadline - now)
    : state.remainingMs;
}
export function startTimer(state: TimerState, now: number): TimerState {
  return state.status === "idle" || state.status === "paused"
    ? { ...state, status: "running", deadline: now + state.remainingMs }
    : state;
}
export function pauseTimer(state: TimerState, now: number): TimerState {
  return state.status === "running"
    ? {
        ...state,
        status: "paused",
        remainingMs: remaining(state, now),
        deadline: null,
      }
    : state;
}
export function tickTimer(
  state: TimerState,
  now: number,
  key: string = state.cycleKey,
): TimerState {
  if (
    state.status !== "running" ||
    state.deadline === null ||
    state.deadline > now
  )
    return state;
  const focus = state.mode === "focus";
  return {
    ...state,
    status: "finished",
    remainingMs: 0,
    deadline: null,
    cycles: state.cycles + (focus ? 1 : 0),
    pending:
      focus && state.subjectId
        ? {
            clientKey: key,
            subjectId: state.subjectId,
            taskId: state.taskId,
            startedAt: new Date(
              state.deadline - state.durationMs,
            ).toISOString(),
            endedAt: new Date(state.deadline).toISOString(),
          }
        : null,
  };
}
export function restoreTimer(
  raw: string | null,
  fallback: TimerState,
): TimerState {
  try {
    const value = JSON.parse(raw || "null") as TimerState | null;
    if (
      !value ||
      !["focus", "short", "long"].includes(value.mode) ||
      !["idle", "running", "paused", "finished"].includes(value.status) ||
      !Number.isFinite(value.durationMs) ||
      value.durationMs < 60000 ||
      value.durationMs > 10800000 ||
      !Number.isFinite(value.remainingMs) ||
      value.remainingMs < 0 ||
      value.remainingMs > value.durationMs ||
      !Number.isInteger(value.cycles) ||
      value.cycles < 0 ||
      (value.status === "running" && !Number.isFinite(value.deadline))
    )
      return fallback;
    if (
      value.pending &&
      (!value.pending.clientKey ||
        !Number.isInteger(value.pending.subjectId) ||
        !Number.isFinite(Date.parse(value.pending.endedAt)))
    )
      return fallback;
    return {
      ...value,
      cycleKey:
        typeof value.cycleKey === "string"
          ? value.cycleKey
          : crypto.randomUUID(),
    };
  } catch {
    return fallback;
  }
}
export function timerLabel(ms: number) {
  const seconds = Math.ceil(ms / 1000);
  return `${Math.floor(seconds / 60)
    .toString()
    .padStart(2, "0")}:${(seconds % 60).toString().padStart(2, "0")}`;
}
