import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ResourceForm } from "../features/resources/ResourceForm";
import { ResourcePage } from "../features/resources/ResourcePage";
import { subjectsConfig, tasksConfig } from "../features/resources/config";
import type { Subject } from "../types/api";
vi.mock("../app/context", () => ({
  useApp: () => ({
    subjects: [{ id: 1, name: "Java" }],
    tasks: [],
    refreshCatalog: vi.fn(),
  }),
}));
beforeEach(() =>
  vi.stubGlobal(
    "fetch",
    vi
      .fn()
      .mockResolvedValue(new Response(JSON.stringify([]), { status: 200 })),
  ),
);
afterEach(() => vi.unstubAllGlobals());
it("formulário salva uma disciplina pela API", async () => {
  const user = userEvent.setup();
  const saved = vi.fn();
  render(
    <ResourceForm
      config={subjectsConfig}
      onSaved={saved}
      onCancel={() => {}}
    />,
  );
  await user.type(screen.getByLabelText(/Nome/), "Java");
  await user.click(screen.getByRole("button", { name: "Criar disciplina" }));
  await waitFor(() => expect(saved).toHaveBeenCalled());
  const call = vi.mocked(fetch).mock.calls[0];
  expect(call[0]).toContain("/api/subjects");
  expect(call[1]?.method).toBe("POST");
  expect(JSON.parse(String(call[1]?.body))).toEqual({
    name: "Java",
    description: null,
    color: "#A3B18A",
  });
});
it("formulário mostra erro recebido por campo", async () => {
  vi.mocked(fetch).mockResolvedValue(
    new Response(
      JSON.stringify({
        detail: "Verifique os campos",
        errors: { name: "Nome inválido" },
      }),
      { status: 400 },
    ),
  );
  const user = userEvent.setup();
  render(
    <ResourceForm
      config={subjectsConfig}
      onSaved={() => {}}
      onCancel={() => {}}
    />,
  );
  await user.type(screen.getByLabelText(/Nome/), "Java");
  await user.click(screen.getByRole("button", { name: "Criar disciplina" }));
  expect(await screen.findByText("Nome inválido")).toBeInTheDocument();
  expect(screen.getByRole("alert")).toHaveTextContent("Verifique os campos");
});
it("campo obrigatório impede envio sem título", async () => {
  const user = userEvent.setup();
  render(
    <ResourceForm
      config={tasksConfig}
      onSaved={() => {}}
      onCancel={() => {}}
    />,
  );
  await user.click(screen.getByRole("button", { name: "Criar tarefa" }));
  expect(fetch).not.toHaveBeenCalled();
});
it("lista vazia orienta a primeira ação", async () => {
  render(<ResourcePage<Subject> config={subjectsConfig} />);
  expect(
    await screen.findByText("Tudo pronto para começar"),
  ).toBeInTheDocument();
  expect(
    screen.getAllByRole("button", { name: "Nova disciplina" }),
  ).toHaveLength(2);
});
