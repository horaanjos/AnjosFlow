import { useState, type FormEvent } from "react";
import { Settings, Timer, Check } from "lucide-react";
import { useApp } from "../app/context";
import { PageTitle, Badge } from "../components/ui";
export function SettingsPage() {
  const { preferences, setPreferences } = useApp();
  const [values, setValues] = useState(preferences);
  const [saved, setSaved] = useState(false);
  function save(e: FormEvent) {
    e.preventDefault();
    setPreferences(values);
    setSaved(true);
  }
  return (
    <>
      <PageTitle
        title="Do seu jeito"
        description="Ajuste seu espaço e encontre um ritmo que funciona para você."
      />
      <form className="settings-grid" onSubmit={save}>
        <section className="card">
          <span className="round-icon">
            <Settings size={22} />
          </span>
          <h2>Seu espaço pessoal</h2>
          <p className="muted">Preferências salvas apenas neste navegador.</p>
          <div className="field">
            <label htmlFor="display-name">Como você quer ser chamado?</label>
            <input
              id="display-name"
              maxLength={60}
              value={values.displayName}
              placeholder="Seu nome"
              onChange={(e) => {
                setValues((v) => ({ ...v, displayName: e.target.value }));
                setSaved(false);
              }}
            />
          </div>
          <Badge>Fuso dos estudos: America/Sao_Paulo</Badge>
        </section>
        <section className="card">
          <span className="round-icon">
            <Timer size={22} />
          </span>
          <h2>Seu ritmo de foco</h2>
          <p className="muted">
            As novas durações valem ao iniciar ou reiniciar um ciclo.
          </p>
          {(
            [
              { key: "focusMinutes", label: "Foco (min)", max: 180 },
              { key: "shortMinutes", label: "Pausa curta (min)", max: 60 },
              { key: "longMinutes", label: "Pausa longa (min)", max: 60 },
            ] as const
          ).map((f) => (
            <div className="field" key={f.key}>
              <label htmlFor={f.key}>{f.label}</label>
              <input
                id={f.key}
                type="number"
                required
                min={1}
                max={f.max}
                value={values[f.key]}
                onChange={(e) => {
                  setValues((v) => ({ ...v, [f.key]: Number(e.target.value) }));
                  setSaved(false);
                }}
              />
            </div>
          ))}
          <p className="muted">
            Uma pausa longa é sugerida após quatro ciclos de foco.
          </p>
        </section>
        <div className="settings-actions">
          <button className="button">
            <Check size={17} />
            Salvar preferências
          </button>
          {saved && (
            <span className="success" role="status">
              Preferências salvas.
            </span>
          )}
        </div>
      </form>
    </>
  );
}
