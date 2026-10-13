import { PORTFOLIO_MODE } from "../services/environment";
import { useEffect, useState } from "react";
import {
  Play,
  Pause,
  RotateCcw,
  Check,
  ArrowRight,
  Coffee,
  BookOpen,
} from "lucide-react";
import { Link } from "react-router-dom";
import { useApp } from "../app/context";
import { PageTitle, ErrorState, Modal, Badge } from "../components/ui";
import {
  createTimer,
  restoreTimer,
  startTimer,
  pauseTimer,
  tickTimer,
  remaining,
  timerLabel,
  type Mode,
  type TimerState,
} from "../features/pomodoro/timer";
import { send } from "../services/api";
export function PomodoroPage() {
  const { preferences, subjects, tasks } = useApp();
  const minutes = (mode: Mode) =>
    mode === "focus"
      ? preferences.focusMinutes
      : mode === "short"
        ? preferences.shortMinutes
        : preferences.longMinutes;
  const [timer, setTimer] = useState<TimerState>(() =>
    restoreTimer(
      localStorage.getItem("anjosflow-timer"),
      createTimer("focus", preferences.focusMinutes),
    ),
  );
  const [now, setNow] = useState(Date.now());
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [discard, setDiscard] = useState<Mode | "reset" | null>(null);
  useEffect(() => {
    localStorage.setItem("anjosflow-timer", JSON.stringify(timer));
  }, [timer]);
  useEffect(() => {
    function update() {
      const current = Date.now();
      setNow(current);
      setTimer((value) => tickTimer(value, current));
    }
    update();
    const id = setInterval(update, 250);
    document.addEventListener("visibilitychange", update);
    return () => {
      clearInterval(id);
      document.removeEventListener("visibilitychange", update);
    };
  }, []);
  const left = remaining(timer, now);
  const progress = Math.min(
    100,
    Math.max(0, 100 - (left / timer.durationMs) * 100),
  );
  function switchMode(mode: Mode) {
    if (
      timer.status === "running" ||
      timer.status === "paused" ||
      timer.pending
    ) {
      setDiscard(mode);
      return;
    }
    setTimer((t) => ({
      ...createTimer(mode, minutes(mode), t.cycles),
      subjectId: t.subjectId,
      taskId: t.taskId,
    }));
    setNotice("");
  }
  function reset() {
    if (timer.status !== "idle" || timer.pending) {
      setDiscard("reset");
      return;
    }
    setTimer((t) => ({
      ...createTimer(t.mode, minutes(t.mode), t.cycles),
      subjectId: t.subjectId,
      taskId: t.taskId,
    }));
  }
  async function save() {
    if (!timer.pending || busy) return;
    setBusy(true);
    setError("");
    try {
      await send("/sessions", "POST", timer.pending);
      setTimer((t) => ({ ...t, pending: null }));
      setNotice(
        "Sessão registrada. Seu tempo já conta nas metas e no dashboard.",
      );
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao registrar sessão.");
    } finally {
      setBusy(false);
    }
  }
  const locked =
    timer.status === "running" || timer.status === "paused" || !!timer.pending;
  return (
    <>
      <PageTitle
        title="Um momento de foco"
        description="Escolha o que estudar. O resto pode esperar."
        action={
          <Link className="button secondary" to="/settings">
            Ajustar durações
          </Link>
        }
      />
      <div className="pomodoro-layout">
        <section className="card timer-card">
          <div className="timer-tabs" aria-label="Modo do cronômetro">
            {(
              [
                { mode: "focus", title: "Foco" },
                { mode: "short", title: "Pausa curta" },
                { mode: "long", title: "Pausa longa" },
              ] as const
            ).map((m) => (
              <button
                key={m.mode}
                className={timer.mode === m.mode ? "selected" : ""}
                aria-pressed={timer.mode === m.mode}
                disabled={busy}
                onClick={() => switchMode(m.mode)}
              >
                {m.title}
              </button>
            ))}
          </div>
          <div
            className="timer-ring"
            style={{
              background: `conic-gradient(#527C68 ${progress}%, #E8EDE7 0)`,
            }}
          >
            <div className="timer-face">
              {timer.mode === "focus" ? (
                <BookOpen size={22} />
              ) : (
                <Coffee size={22} />
              )}
              <span
                className="timer-digits"
                role="timer"
                aria-label="Tempo restante"
              >
                {timerLabel(left)}
              </span>
              <p>
                {timer.status === "finished"
                  ? "Ciclo concluído"
                  : timer.status === "paused"
                    ? "No seu tempo"
                    : timer.mode === "focus"
                      ? "Uma coisa de cada vez"
                      : "Respire. Você merece uma pausa."}
              </p>
            </div>
          </div>
          <div className="timer-controls">
            {timer.status === "running" ? (
              <button
                className="button"
                onClick={() => setTimer((t) => pauseTimer(t, Date.now()))}
              >
                <Pause size={18} />
                Pausar
              </button>
            ) : (
              <button
                className="button"
                disabled={
                  timer.status === "finished" ||
                  (timer.mode === "focus" &&
                    !timer.subjectId &&
                    !PORTFOLIO_MODE) ||
                  busy
                }
                onClick={() => {
                  setNotice("");
                  setTimer((t) => startTimer(t, Date.now()));
                }}
              >
                <Play size={18} />
                {timer.status === "paused" ? "Retomar" : "Iniciar"}
              </button>
            )}
            <button
              className="button secondary"
              onClick={reset}
              disabled={busy}
            >
              <RotateCcw size={17} />
              Reiniciar
            </button>
          </div>
          <p className="muted timer-cycles">
            {timer.cycles}{" "}
            {timer.cycles === 1
              ? "ciclo de foco concluído"
              : "ciclos de foco concluídos"}{" "}
            · Pausa longa a cada 4 ciclos
          </p>
          {timer.status === "finished" && (
            <div className="completion" role="status">
              <strong>
                {timer.mode === "focus"
                  ? "Bom trabalho. Mais um passo dado."
                  : "Pausa concluída. Pronto para continuar?"}
              </strong>
              {timer.pending ? (
                <button className="button" onClick={save} disabled={busy}>
                  <Check size={17} />
                  {busy ? "Registrando…" : "Registrar sessão de foco"}
                </button>
              ) : (
                <button
                  className="button"
                  onClick={() =>
                    switchMode(
                      timer.mode === "focus"
                        ? timer.cycles % 4 === 0
                          ? "long"
                          : "short"
                        : "focus",
                    )
                  }
                >
                  Próximo ciclo
                  <ArrowRight size={17} />
                </button>
              )}
              {timer.pending && (
                <button
                  className="text-link"
                  disabled={busy}
                  onClick={() =>
                    setDiscard(timer.cycles % 4 === 0 ? "long" : "short")
                  }
                >
                  Continuar sem registrar
                </button>
              )}
            </div>
          )}
          {error && <ErrorState message={error} />}{" "}
          {notice && (
            <div className="success" role="status">
              {notice}
            </div>
          )}
        </section>
        <aside>
          <section className="card">
            <span className="eyebrow">INTENÇÃO DA SESSÃO</span>
            <h2>O que você vai estudar?</h2>
            <div className="field">
              <label htmlFor="timer-subject">Disciplina</label>
              <select
                id="timer-subject"
                value={timer.subjectId || ""}
                disabled={locked || PORTFOLIO_MODE}
                onChange={(e) =>
                  setTimer((t) => ({
                    ...t,
                    subjectId: e.target.value ? Number(e.target.value) : null,
                    taskId: null,
                  }))
                }
              >
                <option value="">Selecione uma disciplina</option>
                {subjects.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="timer-task">Tarefa (opcional)</label>
              <select
                id="timer-task"
                value={timer.taskId || ""}
                disabled={locked || PORTFOLIO_MODE}
                onChange={(e) =>
                  setTimer((t) => ({
                    ...t,
                    taskId: e.target.value ? Number(e.target.value) : null,
                  }))
                }
              >
                <option value="">Foco livre</option>
                {tasks
                  .filter(
                    (t) =>
                      t.subjectId === timer.subjectId &&
                      t.status !== "COMPLETED",
                  )
                  .map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.title}
                    </option>
                  ))}
              </select>
            </div>
            {!subjects.length && !PORTFOLIO_MODE && (
              <Link className="text-link" to="/subjects">
                Cadastrar minha primeira disciplina
              </Link>
            )}
          </section>
          <section className="card focus-tips">
            <Badge tone="green">Seu ritmo importa</Badge>
            <h3>Crie espaço para se concentrar.</h3>
            <p>
              Afaste distrações, tenha água por perto e escolha uma tarefa
              possível.
            </p>
            <p>
              O cronômetro acompanha o tempo real mesmo ao alternar de aba.
              Descansos não contam como estudo.
            </p>
            <Link className="text-link" to="/sessions">
              Ver meu histórico
              <ArrowRight size={15} />
            </Link>
          </section>
        </aside>
      </div>
      {discard && (
        <Modal
          title="Descartar o ciclo atual?"
          onClose={() => setDiscard(null)}
        >
          <p className="modal-copy">
            O tempo deste ciclo não será registrado. Sessões já salvas continuam
            no histórico.
          </p>
          <div className="form-actions">
            <button
              className="button secondary"
              onClick={() => setDiscard(null)}
            >
              Voltar
            </button>
            <button
              className="button"
              onClick={() => {
                const mode = discard === "reset" ? timer.mode : discard;
                setTimer((t) => ({
                  ...createTimer(mode, minutes(mode), t.cycles),
                  subjectId: t.subjectId,
                  taskId: t.taskId,
                }));
                setDiscard(null);
                setNotice("");
              }}
            >
              Descartar e continuar
            </button>
          </div>
        </Modal>
      )}
    </>
  );
}
