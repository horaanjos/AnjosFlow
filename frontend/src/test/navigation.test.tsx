import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import { App } from "../app/App";
import { AppProvider } from "../app/context";
const dashboard = {
  timeZone: "America/Sao_Paulo",
  today: "2026-10-13",
  weekStart: "2026-10-12",
  totalSubjects: 0,
  pendingTasks: 0,
  completedTasks: 0,
  overdueTasks: 0,
  minutesToday: 0,
  weekMinutes: 0,
  dailyStudy: [],
  distribution: [],
  upcomingExams: [],
  nextReviews: [],
  goals: [],
};
beforeEach(() => {
  localStorage.clear();
  vi.stubGlobal(
    "fetch",
    vi
      .fn()
      .mockImplementation((url: string) =>
        Promise.resolve(
          new Response(
            JSON.stringify(url.endsWith("/dashboard") ? dashboard : []),
            { status: 200 },
          ),
        ),
      ),
  );
});
afterEach(() => vi.unstubAllGlobals());
it("navega entre dashboard e disciplinas sem reload", async () => {
  const user = userEvent.setup();
  render(
    <MemoryRouter>
      <AppProvider>
        <App />
      </AppProvider>
    </MemoryRouter>,
  );
  expect(
    await screen.findByRole("heading", {
      name: "Seu próximo passo começa aqui.",
    }),
  ).toBeInTheDocument();
  await user.click(screen.getByRole("link", { name: "Disciplinas" }));
  expect(
    await screen.findByRole("heading", { name: "Disciplinas" }),
  ).toBeInTheDocument();
  expect(
    await screen.findByText("Tudo pronto para começar"),
  ).toBeInTheDocument();
});
it("preferências são salvas localmente", async () => {
  const user = userEvent.setup();
  render(
    <MemoryRouter initialEntries={["/settings"]}>
      <AppProvider>
        <App />
      </AppProvider>
    </MemoryRouter>,
  );
  await user.type(
    screen.getByLabelText("Como você quer ser chamado?"),
    "Carlos",
  );
  await user.click(screen.getByRole("button", { name: "Salvar preferências" }));
  expect(await screen.findByText("Preferências salvas.")).toBeInTheDocument();
  expect(localStorage.getItem("anjosflow-preferences")).toContain("Carlos");
});
