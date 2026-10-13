import { PORTFOLIO_MODE } from "../../services/environment";
import { useState, type FormEvent } from "react";
import { useApp } from "../../app/context";
import { ApiError, send } from "../../services/api";
import { toInputDateTime, toIso, addDays } from "../../services/dates";
import { ErrorState } from "../../components/ui";
import type { Entity } from "../../types/api";
import type { ResourceConfig } from "./config";
export function ResourceForm<T extends Entity>({
  config,
  row,
  onSaved,
  onCancel,
  initial = {},
}: {
  config: ResourceConfig;
  row?: T;
  onSaved: () => void;
  onCancel: () => void;
  initial?: Record<string, string>;
}) {
  const { subjects, tasks } = useApp();
  const [values, setValues] = useState<Record<string, string>>(() =>
    Object.fromEntries(
      config.fields.map((f) => {
        const raw = row
          ? (row as unknown as Record<string, unknown>)[f.key]
          : undefined;
        return [
          f.key,
          raw != null
            ? f.type === "datetime-local"
              ? toInputDateTime(String(raw))
              : String(raw)
            : (initial[f.key] ?? f.default ?? ""),
        ];
      }),
    ),
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [sessionKey] = useState(() => crypto.randomUUID());
  async function submit(event: FormEvent) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    setFieldErrors({});
    try {
      const payload: Record<string, unknown> = {};
      for (const field of config.fields) {
        const value = values[field.key];
        payload[field.key] = !value
          ? null
          : field.type === "number" || field.source
            ? Number(value)
            : field.type === "datetime-local"
              ? toIso(value)
              : value;
      }
      if (config.path === "sessions") payload.clientKey = sessionKey;
      await send(
        `/${config.path}${row ? `/${row.id}` : ""}`,
        row ? "PUT" : "POST",
        payload,
      );
      onSaved();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Não foi possível salvar.");
      if (e instanceof ApiError) setFieldErrors(e.fields);
    } finally {
      setBusy(false);
    }
  }
  function change(key: string, value: string) {
    setValues((v) => ({
      ...v,
      [key]: value,
      ...(key === "subjectId" ? { taskId: "" } : {}),
      ...(key === "periodStart" && value
        ? { periodEnd: addDays(value, 6) }
        : {}),
    }));
  }
  return (
    <form onSubmit={submit} className="resource-form">
      {error && <ErrorState message={error} />}
      <div className="form-grid">
        {config.fields.map((field) => {
          const id = "field-" + field.key;
          const common = {
            id,
            name: field.key,
            required: field.required,
            value: values[field.key],
            disabled: busy,
            "aria-invalid": !!fieldErrors[field.key],
            "aria-describedby": fieldErrors[field.key]
              ? id + "-error"
              : undefined,
            onChange: (
              e: React.ChangeEvent<
                HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
              >,
            ) => change(field.key, e.target.value),
          };
          const choices =
            field.source === "subjects"
              ? subjects.map((s) => ({ value: String(s.id), label: s.name }))
              : field.source === "tasks"
                ? tasks
                    .filter((t) => String(t.subjectId) === values.subjectId)
                    .map((t) => ({ value: String(t.id), label: t.title }))
                : field.options || [];
          return (
            <div
              className={`field ${field.type === "textarea" ? "wide" : ""}`}
              key={field.key}
            >
              <label htmlFor={id}>
                {field.label}
                {field.required && <span className="required"> *</span>}
              </label>
              {field.type === "textarea" ? (
                <textarea {...common} rows={3} maxLength={field.maxLength} />
              ) : field.type === "select" ? (
                <select {...common}>
                  <option value="">
                    {field.required ? "Selecione…" : "Sem associação"}
                  </option>
                  {choices.map((o) => (
                    <option value={o.value} key={o.value}>
                      {o.label}
                    </option>
                  ))}
                </select>
              ) : (
                <input
                  {...common}
                  type={field.type}
                  min={field.type === "number" ? field.min : undefined}
                  minLength={field.type === "text" ? field.min : undefined}
                  max={field.max}
                  maxLength={field.maxLength}
                  step={field.step}
                />
              )}
              {fieldErrors[field.key] && (
                <small className="field-error" id={id + "-error"}>
                  {fieldErrors[field.key]}
                </small>
              )}
            </div>
          );
        })}
      </div>
      {config.fields.some((f) => f.source === "subjects" && f.required) &&
        subjects.length === 0 && (
          <p className="muted">Cadastre uma disciplina antes de continuar.</p>
        )}
      <div className="form-actions">
        <button
          className="button secondary"
          type="button"
          onClick={onCancel}
          disabled={busy}
        >
          Cancelar
        </button>
        <button className="button" disabled={busy || PORTFOLIO_MODE}>
          {busy
            ? "Salvando…"
            : row
              ? "Salvar alterações"
              : "Criar " + config.singular}
        </button>
      </div>
    </form>
  );
}
