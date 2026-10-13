import {
  createTimer,
  startTimer,
  pauseTimer,
  tickTimer,
  remaining,
  restoreTimer,
  timerLabel,
} from "../features/pomodoro/timer";
describe("Pomodoro com relógio real", () => {
  it("usa o prazo absoluto mesmo se callbacks forem atrasados", () => {
    const initial = { ...createTimer("focus", 25), subjectId: 1 };
    const running = startTimer(initial, 100000);
    expect(remaining(running, 700000)).toBe(900000);
    const finished = tickTimer(running, 2000000, "key");
    expect(finished.status).toBe("finished");
    expect(finished.pending?.endedAt).toBe(new Date(1600000).toISOString());
    expect(finished.cycles).toBe(1);
    expect(tickTimer(finished, 3000000, "other")).toEqual(finished);
  });
  it("exclui pausas do tempo de foco", () => {
    let state = startTimer(
      { ...createTimer("focus", 1), subjectId: 1 },
      100000,
    );
    state = pauseTimer(state, 130000);
    expect(state.remainingMs).toBe(30000);
    state = startTimer(state, 300000);
    const end = tickTimer(state, 330000, "id");
    expect(
      Date.parse(end.pending!.endedAt) - Date.parse(end.pending!.startedAt),
    ).toBe(60000);
  });
  it("não registra intervalos de descanso", () => {
    const state = tickTimer(
      startTimer(createTimer("short", 1), 100000),
      160000,
      "id",
    );
    expect(state.pending).toBeNull();
    expect(state.cycles).toBe(0);
  });
  it("restaura execução após atualização de página", () => {
    const state = startTimer(
      { ...createTimer("focus", 25), subjectId: 2 },
      1000,
    );
    const restored = restoreTimer(
      JSON.stringify(state),
      createTimer("focus", 25),
    );
    expect(remaining(restored, 61000)).toBe(1440000);
  });
  it("recupera armazenamento corrompido", () => {
    const initial = createTimer("focus", 25);
    expect(restoreTimer("{invalid", initial)).toEqual(initial);
    expect(restoreTimer("{}", initial)).toEqual(initial);
  });
  it("formata os segundos sem valores negativos", () => {
    expect(timerLabel(59999)).toBe("01:00");
    expect(timerLabel(0)).toBe("00:00");
  });
  it("mantém a chave do ciclo ao restaurar o mesmo cronômetro em outra aba", () => {
    const state = startTimer(
      { ...createTimer("focus", 1), subjectId: 1 },
      100000,
    );
    const restored = restoreTimer(
      JSON.stringify(state),
      createTimer("focus", 1),
    );
    expect(tickTimer(state, 160000).pending?.clientKey).toBe(
      tickTimer(restored, 160000).pending?.clientKey,
    );
  });
});
