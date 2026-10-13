import { PORTFOLIO_MODE } from "../../services/environment";
import { useState, type ReactNode } from "react";
import { Plus, Search, Pencil, Trash2, Check, RotateCcw } from "lucide-react";
import { useApi } from "../../hooks/useApi";
import { useApp } from "../../app/context";
import { send, query } from "../../services/api";
import { today, dateLabel, minutesLabel, toIso } from "../../services/dates";
import {
  PageTitle,
  Loading,
  ErrorState,
  Empty,
  Modal,
  Badge,
} from "../../components/ui";
import { ResourceForm } from "./ResourceForm";
import { labels, type ResourceConfig } from "./config";
import type { Entity, GoalProgress } from "../../types/api";
export function ResourcePage<T extends Entity>({
  config,
  embedded = false,
  initial = {},
  externalEditId,
  onEdited,
}: {
  config: ResourceConfig;
  embedded?: boolean;
  initial?: Record<string, string>;
  externalEditId?: number;
  onEdited?: () => void;
}) {
  const { subjects, refreshCatalog } = useApp();
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [search, setSearch] = useState("");
  const resource = useApi<T[]>(`/${config.path}${query(filters)}`);
  const progress = useApi<GoalProgress[]>(
    config.path === "goals" ? "/goals/progress" : null,
  );
  const [editing, setEditing] = useState<T | true | null>(null);
  const [deleting, setDeleting] = useState<T | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [reviewTab, setReviewTab] = useState("all");
  const externalRow = externalEditId
    ? resource.data?.find((r) => r.id === externalEditId)
    : undefined;
  const selected = editing === true ? undefined : editing || externalRow;
  const open = editing !== null || !!externalRow;
  const value = (row: T, key: string) =>
    (row as unknown as Record<string, unknown>)[key];
  function changed(message: string) {
    resource.reload();
    progress.reload();
    refreshCatalog();
    setNotice(message);
    setError("");
  }
  async function remove() {
    if (!deleting || busy) return;
    setBusy(true);
    try {
      await send(`/${config.path}/${deleting.id}`, "DELETE");
      setDeleting(null);
      changed("Registro excluído.");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao excluir.");
    } finally {
      setBusy(false);
    }
  }
  async function action(row: T) {
    if (busy) return;
    setBusy(true);
    try {
      if (config.path === "tasks")
        await send(`/tasks/${row.id}/status`, "PATCH", {
          status:
            value(row, "status") === "COMPLETED" ? "PENDING" : "COMPLETED",
        });
      else await send(`/reviews/${row.id}/complete`, "POST", {});
      changed(
        config.path === "tasks"
          ? "Status atualizado."
          : "Revisão registrada. Próxima data atualizada.",
      );
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao atualizar.");
    } finally {
      setBusy(false);
    }
  }
  function cell(row: T, key: string): ReactNode {
    const raw = value(row, key);
    if (key === "progress") {
      const p = progress.data?.find((p) => p.goal.id === row.id);
      return p ? (
        <div className="progress-cell">
          <span>
            {p.currentValue} / {p.goal.targetValue} · {p.percentage}%
          </span>
          <progress max={100} value={p.percentage} />
        </div>
      ) : (
        "—"
      );
    }
    if (raw == null || raw === "") return <span className="muted">—</span>;
    if (key === "subjectId")
      return (
        subjects.find((s) => s.id === Number(raw))?.name || "Disciplina #" + raw
      );
    if (key === "color")
      return (
        <span className="color-chip">
          <i style={{ backgroundColor: String(raw) }} />
          {String(raw)}
        </span>
      );
    if (["status", "priority", "type"].includes(key))
      return (
        <Badge
          tone={
            raw === "COMPLETED"
              ? "green"
              : raw === "HIGH"
                ? "orange"
                : "neutral"
          }
        >
          {labels[String(raw)] || String(raw)}
        </Badge>
      );
    if (key === "durationMinutes") return minutesLabel(Number(raw));
    if (["dueDate", "periodStart", "nextReviewAt"].includes(key))
      return (
        <span
          className={
            String(raw) < today() &&
            (key === "nextReviewAt" ||
              (key === "dueDate" && value(row, "status") !== "COMPLETED"))
              ? "overdue"
              : ""
          }
        >
          {dateLabel(String(raw))}
        </span>
      );
    if (["startAt", "endAt", "endedAt", "scheduledAt"].includes(key))
      return dateLabel(String(raw), true);
    return String(raw);
  }
  const visible = (resource.data || [])
    .filter((row) =>
      String(
        value(row, "name") ??
          value(row, "title") ??
          labels[String(value(row, "type"))] ??
          "",
      )
        .toLocaleLowerCase("pt-BR")
        .includes(search.toLocaleLowerCase("pt-BR")),
    )
    .filter(
      (row) =>
        config.path !== "reviews" ||
        reviewTab === "all" ||
        (reviewTab === "due" &&
          String(value(row, "nextReviewAt")) <= today()) ||
        (reviewTab === "reviewed" && Number(value(row, "reviewCount")) > 0),
    );
  const newLabel =
    (config.singular === "evento" ? "Novo " : "Nova ") + config.singular;
  const add = (
    <button
      className="button"
      onClick={() => {
        setEditing(true);
        setError("");
      }}
    >
      <Plus size={18} />
      {newLabel}
    </button>
  );
  return (
    <>
      {!embedded && (
        <PageTitle
          title={config.title}
          description={config.description}
          action={add}
        />
      )}
      <section className="card resource-card">
        {embedded && (
          <div className="section-heading">
            <h2>Seus eventos</h2>
            {add}
          </div>
        )}
        <div className="table-tools">
          <label className="search">
            <Search size={17} />
            <input
              aria-label={"Buscar " + config.title.toLowerCase()}
              placeholder="Buscar…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </label>
          {["tasks", "sessions", "events"].includes(config.path) && (
            <select
              aria-label="Filtrar por disciplina"
              value={filters.subjectId || ""}
              onChange={(e) =>
                setFilters((f) => ({ ...f, subjectId: e.target.value }))
              }
            >
              <option value="">Todas as disciplinas</option>
              {subjects.map((s) => (
                <option value={s.id} key={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          )}
          {config.path === "tasks" && (
            <>
              <select
                aria-label="Filtrar por status"
                value={filters.status || ""}
                onChange={(e) =>
                  setFilters((f) => ({ ...f, status: e.target.value }))
                }
              >
                <option value="">Todos os status</option>
                {["PENDING", "IN_PROGRESS", "COMPLETED"].map((v) => (
                  <option key={v} value={v}>
                    {labels[v]}
                  </option>
                ))}
              </select>
              <select
                aria-label="Filtrar por prioridade"
                value={filters.priority || ""}
                onChange={(e) =>
                  setFilters((f) => ({ ...f, priority: e.target.value }))
                }
              >
                <option value="">Todas as prioridades</option>
                {["HIGH", "MEDIUM", "LOW"].map((v) => (
                  <option key={v} value={v}>
                    {labels[v]}
                  </option>
                ))}
              </select>
              <label className="compact-field">
                Prazo até
                <input
                  type="date"
                  value={filters.dueBefore || ""}
                  onChange={(e) =>
                    setFilters((f) => ({ ...f, dueBefore: e.target.value }))
                  }
                />
              </label>
            </>
          )}
          {config.path === "sessions" && (
            <>
              <label className="compact-field">
                De
                <input
                  type="date"
                  onChange={(e) =>
                    setFilters((f) => ({
                      ...f,
                      from: e.target.value
                        ? toIso(e.target.value + "T00:00")
                        : "",
                    }))
                  }
                />
              </label>
              <label className="compact-field">
                Até (exclusivo)
                <input
                  type="date"
                  onChange={(e) =>
                    setFilters((f) => ({
                      ...f,
                      to: e.target.value
                        ? toIso(e.target.value + "T00:00")
                        : "",
                    }))
                  }
                />
              </label>
            </>
          )}
          {config.path === "reviews" && (
            <select
              aria-label="Filtrar revisões"
              value={reviewTab}
              onChange={(e) => setReviewTab(e.target.value)}
            >
              <option value="all">Todas as revisões</option>
              <option value="due">Hoje e atrasadas</option>
              <option value="reviewed">Já revisadas</option>
            </select>
          )}
        </div>
        {notice && (
          <div className="success" role="status">
            {notice}
          </div>
        )}
        {error && <ErrorState message={error} />}{" "}
        {resource.error ? (
          <ErrorState message={resource.error} retry={resource.reload} />
        ) : resource.loading ? (
          <Loading />
        ) : visible.length === 0 ? (
          <Empty
            title={
              PORTFOLIO_MODE
                ? "Explore os formulários do projeto"
                : "Tudo pronto para começar"
            }
            text={
              search || Object.values(filters).some(Boolean)
                ? "Nenhum registro corresponde aos filtros."
                : PORTFOLIO_MODE
                  ? "Nesta publicação, cadastros não são salvos. A persistência está disponível ao executar a API local."
                  : "Adicione seu primeiro registro e dê o próximo passo."
            }
            action={add}
          />
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  {config.columns.map((c) => (
                    <th key={c.key}>{c.label}</th>
                  ))}
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((row) => (
                  <tr key={row.id}>
                    {config.columns.map((c) => (
                      <td key={c.key}>{cell(row, c.key)}</td>
                    ))}
                    <td>
                      <div className="row-actions">
                        {["tasks", "reviews"].includes(config.path) && (
                          <button
                            className="icon-button"
                            disabled={busy}
                            onClick={() => action(row)}
                            aria-label={
                              config.path === "reviews"
                                ? "Registrar revisão"
                                : value(row, "status") === "COMPLETED"
                                  ? "Reabrir tarefa"
                                  : "Concluir tarefa"
                            }
                            title={
                              config.path === "reviews"
                                ? "Registrar revisão"
                                : value(row, "status") === "COMPLETED"
                                  ? "Reabrir"
                                  : "Concluir"
                            }
                          >
                            {value(row, "status") === "COMPLETED" ? (
                              <RotateCcw size={16} />
                            ) : (
                              <Check size={16} />
                            )}
                          </button>
                        )}
                        {config.editable !== false && (
                          <button
                            className="icon-button"
                            onClick={() => setEditing(row)}
                            aria-label={"Editar " + config.singular}
                          >
                            <Pencil size={16} />
                          </button>
                        )}
                        <button
                          className="icon-button danger"
                          onClick={() => {
                            setDeleting(row);
                            setError("");
                          }}
                          aria-label={"Excluir " + config.singular}
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
      {open && (
        <Modal
          title={selected ? "Editar " + config.singular : newLabel}
          onClose={() => {
            setEditing(null);
            onEdited?.();
          }}
        >
          <ResourceForm
            config={config}
            row={selected}
            initial={initial}
            onCancel={() => {
              setEditing(null);
              onEdited?.();
            }}
            onSaved={() => {
              setEditing(null);
              onEdited?.();
              changed("Registro salvo com sucesso.");
            }}
          />
        </Modal>
      )}
      {deleting && (
        <Modal
          title={"Excluir " + config.singular + "?"}
          onClose={() => {
            if (!busy) setDeleting(null);
          }}
        >
          <p className="modal-copy">
            Esta ação remove o registro. Registros vinculados podem impedir a
            exclusão.
          </p>
          {error && <ErrorState message={error} />}
          <div className="form-actions">
            <button
              className="button secondary"
              disabled={busy}
              onClick={() => setDeleting(null)}
            >
              Cancelar
            </button>
            <button
              className="button destructive"
              disabled={busy}
              onClick={remove}
            >
              {busy ? "Excluindo…" : "Confirmar exclusão"}
            </button>
          </div>
        </Modal>
      )}
    </>
  );
}
