import { api, ApiError, query } from "../services/api";
afterEach(() => vi.unstubAllGlobals());
describe("cliente HTTP", () => {
  it("propaga erros por campo do Back-end", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            detail: "Dados inválidos",
            errors: { name: "Obrigatório" },
          }),
          { status: 400 },
        ),
      ),
    );
    try {
      await api("/subjects");
      throw new Error("deveria falhar");
    } catch (e) {
      expect(e).toBeInstanceOf(ApiError);
      expect((e as ApiError).fields.name).toBe("Obrigatório");
    }
  });
  it("explica indisponibilidade da conexão", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("network")));
    await expect(api("/tasks")).rejects.toThrow("Não foi possível conectar");
  });
  it("trata exclusão sem corpo", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response(null, { status: 204 })),
    );
    expect(await api("/tasks/1", { method: "DELETE" })).toBeUndefined();
  });
  it("codifica apenas filtros preenchidos", () =>
    expect(query({ status: "PENDING", subjectId: "" })).toBe(
      "?status=PENDING",
    ));
});
