import { DateTime } from "luxon";
export const ZONE = "America/Sao_Paulo";
export function today() {
  return DateTime.now().setZone(ZONE).toISODate()!;
}
export function dateLabel(value: string, withTime = false) {
  const date = DateTime.fromISO(value, { zone: ZONE }).setZone(ZONE);
  return date.isValid
    ? date
        .setLocale("pt-BR")
        .toFormat(withTime ? "dd LLL · HH:mm" : "dd LLL yyyy")
    : "—";
}
export function toInputDateTime(value: string) {
  return DateTime.fromISO(value).setZone(ZONE).toFormat("yyyy-MM-dd'T'HH:mm");
}
export function toIso(value: string) {
  const date = DateTime.fromISO(value, { zone: ZONE });
  if (!date.isValid) throw new Error("Informe uma data e hora válidas.");
  return date.toUTC().toISO()!;
}
export function monday(value = today()) {
  return DateTime.fromISO(value, { zone: ZONE }).startOf("week").toISODate()!;
}
export function addDays(value: string, days: number) {
  return DateTime.fromISO(value, { zone: ZONE }).plus({ days }).toISODate()!;
}
export function minutesLabel(minutes: number) {
  return minutes < 60
    ? `${minutes} min`
    : `${Math.floor(minutes / 60)}h${minutes % 60 ? ` ${minutes % 60}min` : ""}`;
}
export function contrastText(hex: string) {
  const channels = hex
    .replace("#", "")
    .match(/.{2}/g)
    ?.map((v) => parseInt(v, 16) / 255) || [0, 0, 0];
  const linear = channels.map((v) =>
    v <= 0.04045 ? v / 12.92 : ((v + 0.055) / 1.055) ** 2.4,
  );
  const luminance =
    linear[0] * 0.2126 + linear[1] * 0.7152 + linear[2] * 0.0722;
  return luminance > 0.179 ? "#000000" : "#FFFFFF";
}
