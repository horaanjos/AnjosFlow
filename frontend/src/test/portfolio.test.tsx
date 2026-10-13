import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import { PortfolioDashboard } from "../pages/PortfolioDashboard";
import { PomodoroPage } from "../pages/PomodoroPage";
import { ResourceForm } from "../features/resources/ResourceForm";
import { subjectsConfig } from "../features/resources/config";
import { api } from "../services/api";
vi.mock("../services/environment", () => ({
  PORTFOLIO_MODE: true,
  REPOSITORY_URL: "https://github.com/horaanjos/AnjosFlow",
}));
vi.mock("../app/context", () => ({
  useApp: () => ({
    preferences: {
      displayName: "",
      focusMinutes: 25,
      shortMinutes: 5,
      longMinutes: 15,
    },
    subjects: [],
    tasks: [],
  }),
}));
beforeEach(() => {
  localStorage.clear();
  vi.stubGlobal("fetch", vi.fn());
});
afterEach(() => vi.unstubAllGlobals());
it("mostra captura identificada e link para o código", () => {
  render(
    <MemoryRouter>
      <PortfolioDashboard />
    </MemoryRouter>,
  );
  expect(
    screen.getByText(
      "Captura real da execução local; estes indicadores não são dados ao vivo.",
    ),
  ).toBeInTheDocument();
  expect(
    screen.getByRole("link", { name: "Ver código no GitHub" }),
  ).toHaveAttribute("href", "https://github.com/horaanjos/AnjosFlow");
  expect(fetch).not.toHaveBeenCalled();
});
it("não envia chamadas para a API do visitante", async () => {
  await expect(api("/tasks")).rejects.toThrow("não possui API publicada");
  expect(fetch).not.toHaveBeenCalled();
});
it("desabilita gravação de cadastros na apresentação", () => {
  render(
    <ResourceForm
      config={subjectsConfig}
      onSaved={() => {}}
      onCancel={() => {}}
    />,
  );
  expect(
    screen.getByRole("button", { name: "Criar disciplina" }),
  ).toBeDisabled();
});
it("permite iniciar e pausar foco sem disciplina na prévia", async () => {
  const user = userEvent.setup();
  render(
    <MemoryRouter>
      <PomodoroPage />
    </MemoryRouter>,
  );
  await user.click(
    screen.getByRole("button", { name: "Iniciar" }),
  );
  expect(
    screen.getByRole("button", { name: "Pausar" }),
  ).toBeEnabled();
  await user.click(screen.getByRole("button", { name: "Pausar" }));
  expect(
    screen.getByRole("button", { name: "Retomar" }),
  ).toBeEnabled();
  expect(fetch).not.toHaveBeenCalled();
});
