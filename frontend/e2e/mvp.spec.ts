import { test, expect } from "@playwright/test";
import { DateTime } from "luxon";
const apiUrl = process.env.E2E_API_URL || "http://localhost:8080";
const zone = "America/Sao_Paulo";
test("CRUD real dos módulos, calendário e indicadores", async ({
  page,
  request,
}) => {
  const created: string[] = [];
  const marker = "E2E-" + Date.now();
  async function submit(route: string, button: string) {
    const responsePromise = page.waitForResponse(
      (r) =>
        r.url().endsWith("/api/" + route) && r.request().method() === "POST",
    );
    await page.getByRole("button", { name: button, exact: true }).click();
    const response = await responsePromise;
    const body = await response.json();
    if (body.id) created.push("/api/" + route + "/" + body.id);
    expect(response.status()).toBe(201);
    await expect(page.getByRole("dialog")).not.toBeVisible();
    return body;
  }
  try {
    await page.goto("/subjects");
    await expect(
      page.getByRole("heading", { name: "Disciplinas", exact: true }),
    ).toBeVisible();
    await page
      .getByRole("button", { name: "Nova disciplina", exact: true })
      .first()
      .click();
    await page.getByLabel(/^Nome/).fill(marker);
    const subject = await submit("subjects", "Criar disciplina");
    await page.getByRole("link", { name: "Tarefas", exact: true }).click();
    await page
      .getByRole("button", { name: "Nova tarefa", exact: true })
      .first()
      .click();
    await page.getByLabel(/^Título/).fill(marker + " tarefa");
    await page.getByLabel(/^Disciplina/).selectOption(String(subject.id));
    await page.getByLabel("Tempo estimado (min)").fill("30");
    const task = await submit("tasks", "Criar tarefa");
    let row = page.getByRole("row").filter({ hasText: marker + " tarefa" });
    await row.getByRole("button", { name: "Concluir tarefa" }).click();
    await expect(row).toContainText("Concluída");
    await row.getByRole("button", { name: "Reabrir tarefa" }).click();
    await expect(row).toContainText("Pendente");
    await page.getByRole("link", { name: "Calendário", exact: true }).click();
    await expect(
      page.getByRole("button", { name: "Mês", exact: true }),
    ).toBeVisible();
    await page.getByRole("button", { name: "Semana", exact: true }).click();
    await page.getByRole("button", { name: "Dia", exact: true }).click();
    await page.getByRole("button", { name: "Mês", exact: true }).click();
    await page
      .getByRole("button", { name: "Novo evento", exact: true })
      .first()
      .click();
    await page.getByLabel(/^Título/).fill(marker + " evento");
    await page.getByLabel(/^Disciplina/).selectOption(String(subject.id));
    await submit("events", "Criar evento");
    await page
      .getByRole("link", { name: "Sessões de estudo", exact: true })
      .click();
    await page
      .getByRole("button", { name: "Nova sessão", exact: true })
      .first()
      .click();
    await page.getByLabel(/^Disciplina/).selectOption(String(subject.id));
    await page.getByLabel("Tarefa (opcional)").selectOption(String(task.id));
    const end = DateTime.now().setZone(zone).minus({ minutes: 1 });
    await page
      .getByLabel("Início (São Paulo)")
      .fill(end.minus({ minutes: 30 }).toFormat("yyyy-MM-dd'T'HH:mm"));
    await page
      .getByLabel("Fim (São Paulo)")
      .fill(end.toFormat("yyyy-MM-dd'T'HH:mm"));
    await submit("sessions", "Criar sessão");
    await expect(
      page.getByRole("row").filter({ hasText: marker }),
    ).toContainText("30 min");
    await page.getByRole("link", { name: "Provas", exact: true }).click();
    await page
      .getByRole("button", { name: "Nova prova", exact: true })
      .first()
      .click();
    await page.getByLabel(/^Título/).fill(marker + " prova");
    await page.getByLabel(/^Disciplina/).selectOption(String(subject.id));
    await page
      .getByLabel("Data e hora (São Paulo)")
      .fill(end.plus({ days: 2 }).toFormat("yyyy-MM-dd'T'HH:mm"));
    await submit("exams", "Criar prova");
    await page.getByRole("link", { name: "Metas", exact: true }).click();
    await page
      .getByRole("button", { name: "Nova meta", exact: true })
      .first()
      .click();
    await page.getByLabel("Objetivo (minutos ou tarefas)").fill("60");
    await page
      .getByLabel("Disciplina", { exact: true })
      .selectOption(String(subject.id));
    await submit("goals", "Criar meta");
    await expect(
      page.getByRole("row").filter({ hasText: marker }),
    ).toContainText("50%");
    await page.getByRole("link", { name: "Revisões", exact: true }).click();
    await page
      .getByRole("button", { name: "Nova revisão", exact: true })
      .first()
      .click();
    await page.getByLabel(/^Título/).fill(marker + " revisão");
    await page.getByLabel(/^Disciplina/).selectOption(String(subject.id));
    await submit("reviews", "Criar revisão");
    row = page.getByRole("row").filter({ hasText: marker + " revisão" });
    await row.getByRole("button", { name: "Registrar revisão" }).click();
    await expect(page.getByRole("status")).toContainText("Revisão registrada");
    await page.getByRole("link", { name: "Dashboard", exact: true }).click();
    await expect(
      page.getByRole("heading", { name: "Seu próximo passo começa aqui." }),
    ).toBeVisible();
    await expect(
      page.getByText(marker + " prova", { exact: true }),
    ).toBeVisible();
    await expect(page.getByText(marker, { exact: true })).toBeVisible();
    await page.getByRole("link", { name: "Planejamento", exact: true }).click();
    await page.getByRole("button", { name: "Gerar sugestão" }).click();
    await expect(
      page.getByRole("heading", { name: "Revise sua semana" }),
    ).toBeVisible();
    await page.getByRole("button", { name: "Descartar sugestão" }).click();
  } finally {
    for (const path of created.reverse()) {
      const response = await request.delete(apiUrl + path);
      expect(response.status()).toBe(204);
    }
  }
});
test("navegação responsiva, preferências e Pomodoro por tempo real", async ({
  page,
  request,
}) => {
  let sessionId: number | undefined;
  const response = await request.post(apiUrl + "/api/subjects", {
    data: { name: "E2E-timer-" + Date.now() },
  });
  expect(response.status()).toBe(201);
  const subject = await response.json();
  try {
    await page.goto("/settings");
    await page.getByLabel("Foco (min)").fill("1");
    await page.getByRole("button", { name: "Salvar preferências" }).click();
    await page.getByRole("link", { name: "Pomodoro", exact: true }).click();
    await page
      .getByLabel("Disciplina", { exact: true })
      .selectOption(String(subject.id));
    await page.clock.install({ time: new Date(Date.now() - 300000) });
    await page.getByRole("button", { name: "Iniciar", exact: true }).click();
    await page.clock.fastForward(30000);
    await page.getByRole("button", { name: "Pausar", exact: true }).click();
    await expect(page.getByRole("timer")).toHaveText("00:30");
    await page.clock.fastForward(60000);
    await expect(page.getByRole("timer")).toHaveText("00:30");
    await page.getByRole("button", { name: "Retomar", exact: true }).click();
    await page.clock.fastForward(31000);
    await expect(
      page.getByText("Bom trabalho. Mais um passo dado."),
    ).toBeVisible();
    await expect(
      page.getByRole("button", { name: "Registrar sessão de foco" }),
    ).toBeVisible();
    const saving = page.waitForResponse(
      (r) =>
        r.url().endsWith("/api/sessions") && r.request().method() === "POST",
    );
    await page
      .getByRole("button", { name: "Registrar sessão de foco" })
      .click();
    const saved = await saving;
    const savedBody = await saved.json();
    sessionId = savedBody.id;
    expect(saved.status()).toBe(201);
    await expect(
      page.getByText(
        "Sessão registrada. Seu tempo já conta nas metas e no dashboard.",
      ),
    ).toBeVisible();
    await page.reload();
    await expect(
      page.getByRole("button", { name: "Próximo ciclo" }),
    ).toBeVisible();
    await page.setViewportSize({ width: 390, height: 844 });
    await page.getByRole("button", { name: "Abrir menu" }).click();
    await page.getByRole("link", { name: "Dashboard", exact: true }).click();
    await expect(
      page.getByRole("heading", { name: "Seu próximo passo começa aqui." }),
    ).toBeVisible();
    expect(
      await page.evaluate(
        () => document.documentElement.scrollWidth <= window.innerWidth,
      ),
    ).toBe(true);
    await page.screenshot({
      path: "../docs/screenshots/dashboard-mobile.png",
      fullPage: true,
    });
  } finally {
    if (sessionId)
      expect(
        (await request.delete(apiUrl + "/api/sessions/" + sessionId)).status(),
      ).toBe(204);
    expect(
      (await request.delete(apiUrl + "/api/subjects/" + subject.id)).status(),
    ).toBe(204);
  }
});
test("captura do dashboard desktop com dados persistidos", async ({ page }) => {
  const errors: string[] = [];
  page.on("pageerror", (e) => errors.push(e.message));
  await page.setViewportSize({ width: 1440, height: 1050 });
  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "Seu próximo passo começa aqui." }),
  ).toBeVisible();
  await page.screenshot({
    path: "../docs/screenshots/dashboard-desktop.png",
    fullPage: true,
  });
  await page.setViewportSize({ width: 390, height: 844 });
  await page.screenshot({
    path: "../docs/screenshots/dashboard-mobile.png",
    fullPage: true,
  });
  expect(errors).toEqual([]);
});
