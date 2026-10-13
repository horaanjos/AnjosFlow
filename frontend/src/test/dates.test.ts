import {
  toIso,
  toInputDateTime,
  monday,
  addDays,
  minutesLabel,
  dateLabel,
} from "../services/dates";
describe("datas de São Paulo", () => {
  it("converte entrada local para UTC", () =>
    expect(toIso("2026-10-13T09:00")).toBe("2026-10-13T12:00:00.000Z"));
  it("faz o caminho inverso sem depender do fuso do navegador", () =>
    expect(toInputDateTime("2026-10-13T12:00:00Z")).toBe("2026-10-13T09:00"));
  it("define semana segunda a domingo", () => {
    expect(monday("2026-10-13")).toBe("2026-10-12");
    expect(addDays("2026-10-12", 6)).toBe("2026-10-18");
  });
  it("rejeita data inválida", () => expect(() => toIso("invalid")).toThrow());
  it("formata duração e datas brasileiras", () => {
    expect(minutesLabel(90)).toBe("1h 30min");
    expect(dateLabel("2026-10-13")).toContain("13");
  });
});
