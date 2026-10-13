import { PORTFOLIO_MODE } from "../services/environment";
import { useEffect, useState, type FormEvent } from "react";
import { Plus, Trash2, Sparkles, Check, ArrowRight } from "lucide-react";
import { PageTitle, Loading, ErrorState, Empty, Badge } from "../components/ui";
import { useApi } from "../hooks/useApi";
import { send } from "../services/api";
import {
  monday,
  dateLabel,
  minutesLabel,
  today,
  addDays,
} from "../services/dates";
import type { Availability, Proposal } from "../types/api";
const days = [
  "Segunda-feira",
  "Terça-feira",
  "Quarta-feira",
  "Quinta-feira",
  "Sexta-feira",
  "Sábado",
  "Domingo",
];
export function PlanningPage() {
  const availability = useApi<Availability[]>("/planning/availability");
  const [slots, setSlots] = useState<Availability[]>([]);
  const [week, setWeek] = useState(addDays(monday(), 7));
  const [sessionMinutes, setSessionMinutes] = useState(25);
  const [breakMinutes, setBreakMinutes] = useState(5);
  const [proposal, setProposal] = useState<Proposal>();
  const [clientKey, setClientKey] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  useEffect(() => {
    if (availability.data) setSlots(availability.data);
  }, [availability.data]);
  async function save(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      await send("/planning/availability", "PUT", { slots });
      availability.reload();
      setProposal(undefined);
      setNotice("Disponibilidade salva.");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao salvar.");
    } finally {
      setBusy(false);
    }
  }
  async function preview() {
    setBusy(true);
    setError("");
    setNotice("");
    try {
      const result = await send<Proposal>("/planning/preview", "POST", {
        weekStart: week,
        sessionMinutes,
        breakMinutes,
      });
      setProposal(result);
      setClientKey(crypto.randomUUID());
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao planejar.");
    } finally {
      setBusy(false);
    }
  }
  async function accept() {
    if (!proposal) return;
    setBusy(true);
    setError("");
    try {
      await send("/planning/accept", "POST", {
        weekStart: week,
        sessionMinutes,
        breakMinutes,
        fingerprint: proposal.fingerprint,
        clientKey,
      });
      setNotice("Planejamento aceito. Os blocos estão no calendário.");
      setProposal(undefined);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao aceitar.");
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageTitle
        title="Uma semana com direção"
        description="Você escolhe o tempo disponível. O AnjosFlow organiza uma sugestão possível."
      />
      <div className="planning-layout">
        <section className="card">
          <div className="section-heading">
            <div>
              <h2>Quando você pode estudar?</h2>
              <p>Disponibilidade semanal · São Paulo</p>
            </div>
            <Badge>1. Disponibilidade</Badge>
          </div>
          {availability.loading ? (
            <Loading />
          ) : availability.error ? (
            <ErrorState
              message={availability.error}
              retry={availability.reload}
            />
          ) : (
            <form onSubmit={save}>
              <div className="availability-list">
                {slots.map((slot, i) => (
                  <div className="availability-row" key={i}>
                    <select
                      aria-label={"Dia da janela " + (i + 1)}
                      value={slot.dayOfWeek}
                      disabled={busy}
                      onChange={(e) =>
                        setSlots((s) =>
                          s.map((v, j) =>
                            j === i
                              ? { ...v, dayOfWeek: Number(e.target.value) }
                              : v,
                          ),
                        )
                      }
                    >
                      {days.map((day, n) => (
                        <option value={n + 1} key={day}>
                          {day}
                        </option>
                      ))}
                    </select>
                    <input
                      type="time"
                      required
                      aria-label={"Início da janela " + (i + 1)}
                      value={slot.startTime}
                      disabled={busy}
                      onChange={(e) =>
                        setSlots((s) =>
                          s.map((v, j) =>
                            j === i ? { ...v, startTime: e.target.value } : v,
                          ),
                        )
                      }
                    />
                    <span>até</span>
                    <input
                      type="time"
                      required
                      aria-label={"Fim da janela " + (i + 1)}
                      value={slot.endTime}
                      disabled={busy}
                      onChange={(e) =>
                        setSlots((s) =>
                          s.map((v, j) =>
                            j === i ? { ...v, endTime: e.target.value } : v,
                          ),
                        )
                      }
                    />
                    <button
                      type="button"
                      className="icon-button danger"
                      aria-label="Remover janela"
                      disabled={busy}
                      onClick={() =>
                        setSlots((s) => s.filter((_, j) => j !== i))
                      }
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                ))}
              </div>
              {!slots.length && (
                <p className="muted">
                  Adicione os horários que você quer reservar para estudo.
                </p>
              )}
              <div className="form-actions">
                <button
                  type="button"
                  className="button secondary"
                  disabled={busy || slots.length >= 28}
                  onClick={() =>
                    setSlots((s) => [
                      ...s,
                      { dayOfWeek: 1, startTime: "18:00", endTime: "20:00" },
                    ])
                  }
                >
                  <Plus size={17} />
                  Adicionar horário
                </button>
                <button className="button" disabled={busy || PORTFOLIO_MODE}>
                  Salvar disponibilidade
                </button>
              </div>
            </form>
          )}
        </section>
        <section className="card">
          <div className="section-heading">
            <div>
              <h2>Monte sua sugestão</h2>
              <p>Prazos próximos, prioridades e provas são considerados.</p>
            </div>
            <Badge>2. Sugestão</Badge>
          </div>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="plan-week">Segunda-feira da semana</label>
              <input
                id="plan-week"
                type="date"
                min={monday(today())}
                value={week}
                onChange={(e) => {
                  setWeek(e.target.value);
                  setProposal(undefined);
                }}
              />
            </div>
            <div className="field">
              <label htmlFor="plan-duration">Sessão (minutos)</label>
              <input
                id="plan-duration"
                type="number"
                min={1}
                max={180}
                value={sessionMinutes}
                onChange={(e) => {
                  setSessionMinutes(Number(e.target.value));
                  setProposal(undefined);
                }}
              />
            </div>
            <div className="field">
              <label htmlFor="plan-break">Intervalo (minutos)</label>
              <input
                id="plan-break"
                type="number"
                min={0}
                max={60}
                value={breakMinutes}
                onChange={(e) => {
                  setBreakMinutes(Number(e.target.value));
                  setProposal(undefined);
                }}
              />
            </div>
          </div>
          <button
            className="button"
            disabled={busy || !week || PORTFOLIO_MODE}
            onClick={preview}
          >
            <Sparkles size={17} />
            {busy ? "Processando…" : "Gerar sugestão"}
          </button>
          <p className="muted helper">
            A sugestão usa a disponibilidade salva. Nada é gravado até você
            aceitar.
          </p>
        </section>
      </div>
      {error && <ErrorState message={error} />}{" "}
      {notice && (
        <div className="success" role="status">
          {notice}
        </div>
      )}
      {proposal && (
        <section className="card proposal">
          <div className="section-heading">
            <div>
              <h2>Revise sua semana</h2>
              <p>Os compromissos existentes foram respeitados.</p>
            </div>
            <Badge>{proposal.blocks.length} blocos sugeridos</Badge>
          </div>
          {proposal.blocks.length ? (
            <div className="agenda-list">
              {proposal.blocks.map((block, i) => (
                <div className="agenda-row" key={i}>
                  <div className="date-tile">
                    {dateLabel(block.startAt).slice(0, 6)}
                  </div>
                  <div>
                    <strong>{block.title}</strong>
                    <p>
                      {dateLabel(block.startAt, true)} →{" "}
                      {dateLabel(block.endAt, true)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <Empty
              title="Nenhum bloco disponível"
              text="Confira seus horários e cadastre tarefas pendentes com tempo estimado."
            />
          )}
          {proposal.unscheduled.length > 0 && (
            <div className="unscheduled">
              <h3>O que ainda precisa de espaço</h3>
              {proposal.unscheduled.map((t) => (
                <p key={t.taskId}>
                  {t.title}
                  <span>{minutesLabel(t.remainingMinutes)} restantes</span>
                </p>
              ))}
            </div>
          )}
          <div className="form-actions">
            <button
              className="button secondary"
              disabled={busy}
              onClick={() => setProposal(undefined)}
            >
              Descartar sugestão
            </button>
            <button
              className="button"
              disabled={busy || !proposal.blocks.length}
              onClick={accept}
            >
              <Check size={17} />
              Aceitar e salvar no calendário
              <ArrowRight size={16} />
            </button>
          </div>
        </section>
      )}
    </>
  );
}
