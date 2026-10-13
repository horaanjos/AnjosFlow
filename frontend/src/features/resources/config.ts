import { monday, addDays, today } from "../../services/dates";
export interface Option {
  value: string;
  label: string;
}
export interface Field {
  key: string;
  label: string;
  type:
    | "text"
    | "textarea"
    | "number"
    | "date"
    | "datetime-local"
    | "select"
    | "color";
  required?: boolean;
  min?: number;
  max?: number;
  maxLength?: number;
  step?: string;
  options?: Option[];
  source?: "subjects" | "tasks";
  default?: string;
}
export interface ResourceConfig {
  path: string;
  title: string;
  singular: string;
  description: string;
  fields: Field[];
  columns: { key: string; label: string }[];
  editable?: boolean;
}
export const labels: Record<string, string> = {
  PENDING: "Pendente",
  IN_PROGRESS: "Em andamento",
  COMPLETED: "Concluída",
  LOW: "Baixa",
  MEDIUM: "Média",
  HIGH: "Alta",
  STUDY: "Estudo",
  EXAM: "Prova",
  PERSONAL: "Pessoal",
  UPCOMING: "Agendada",
  CANCELLED: "Cancelada",
  STUDY_MINUTES: "Minutos de estudo",
  COMPLETED_TASKS: "Tarefas concluídas",
};
const options = (...values: string[]) =>
  values.map((value) => ({ value, label: labels[value] }));
const title: Field = {
  key: "title",
  label: "Título",
  type: "text",
  required: true,
  min: 2,
  maxLength: 150,
};
const description: Field = {
  key: "description",
  label: "Descrição",
  type: "textarea",
  maxLength: 1000,
};
const subject: Field = {
  key: "subjectId",
  label: "Disciplina",
  type: "select",
  source: "subjects",
};
const subjectRequired: Field = { ...subject, required: true };
const columns = [
  { key: "title", label: "Título" },
  { key: "subjectId", label: "Disciplina" },
];
export const subjectsConfig: ResourceConfig = {
  path: "subjects",
  title: "Disciplinas",
  singular: "disciplina",
  description: "Cada matéria tem seu lugar. Organize a base dos seus estudos.",
  fields: [
    {
      key: "name",
      label: "Nome",
      type: "text",
      required: true,
      min: 2,
      maxLength: 100,
    },
    { ...description, maxLength: 500 },
    {
      key: "color",
      label: "Cor da disciplina",
      type: "color",
      default: "#A3B18A",
    },
  ],
  columns: [
    { key: "name", label: "Disciplina" },
    { key: "description", label: "Descrição" },
    { key: "color", label: "Cor" },
  ],
};
export const tasksConfig: ResourceConfig = {
  path: "tasks",
  title: "Tarefas",
  singular: "tarefa",
  description: "Transforme seus objetivos em próximos passos claros.",
  fields: [
    title,
    description,
    subjectRequired,
    {
      key: "status",
      label: "Status",
      type: "select",
      required: true,
      default: "PENDING",
      options: options("PENDING", "IN_PROGRESS", "COMPLETED"),
    },
    {
      key: "priority",
      label: "Prioridade",
      type: "select",
      required: true,
      default: "MEDIUM",
      options: options("LOW", "MEDIUM", "HIGH"),
    },
    { key: "dueDate", label: "Prazo", type: "date" },
    {
      key: "estimatedMinutes",
      label: "Tempo estimado (min)",
      type: "number",
      min: 1,
      max: 10080,
    },
  ],
  columns: [
    ...columns,
    { key: "priority", label: "Prioridade" },
    { key: "dueDate", label: "Prazo" },
    { key: "status", label: "Status" },
  ],
};
export const eventsConfig: ResourceConfig = {
  path: "events",
  title: "Eventos",
  singular: "evento",
  description: "Reserve tempo para o que importa.",
  fields: [
    title,
    description,
    subject,
    {
      key: "startAt",
      label: "Início (São Paulo)",
      type: "datetime-local",
      required: true,
      default: today() + "T09:00",
    },
    {
      key: "endAt",
      label: "Fim (São Paulo)",
      type: "datetime-local",
      required: true,
      default: today() + "T10:00",
    },
    {
      key: "type",
      label: "Tipo",
      type: "select",
      required: true,
      default: "STUDY",
      options: options("STUDY", "EXAM", "PERSONAL"),
    },
  ],
  columns: [
    ...columns,
    { key: "startAt", label: "Início" },
    { key: "endAt", label: "Fim" },
    { key: "type", label: "Tipo" },
  ],
};
export const sessionsConfig: ResourceConfig = {
  path: "sessions",
  title: "Sessões de estudo",
  singular: "sessão",
  description: "Seu esforço merece ser registrado. Acompanhe cada sessão.",
  editable: false,
  fields: [
    subjectRequired,
    {
      key: "taskId",
      label: "Tarefa (opcional)",
      type: "select",
      source: "tasks",
    },
    {
      key: "startedAt",
      label: "Início (São Paulo)",
      type: "datetime-local",
      required: true,
    },
    {
      key: "endedAt",
      label: "Fim (São Paulo)",
      type: "datetime-local",
      required: true,
    },
    { key: "notes", label: "Anotações", type: "textarea", maxLength: 1000 },
  ],
  columns: [
    { key: "subjectId", label: "Disciplina" },
    { key: "endedAt", label: "Concluída em" },
    { key: "durationMinutes", label: "Tempo estudado" },
    { key: "notes", label: "Anotações" },
  ],
};
export const examsConfig: ResourceConfig = {
  path: "exams",
  title: "Provas e avaliações",
  singular: "prova",
  description:
    "Prepare-se com antecedência. Suas avaliações também aparecem no calendário.",
  fields: [
    title,
    description,
    subject,
    {
      key: "scheduledAt",
      label: "Data e hora (São Paulo)",
      type: "datetime-local",
      required: true,
    },
    { key: "location", label: "Local", type: "text", maxLength: 200 },
    {
      key: "status",
      label: "Status",
      type: "select",
      required: true,
      default: "UPCOMING",
      options: options("UPCOMING", "COMPLETED", "CANCELLED"),
    },
    {
      key: "score",
      label: "Nota",
      type: "number",
      min: 0,
      max: 999999,
      step: "0.01",
    },
    {
      key: "maxScore",
      label: "Pontuação máxima",
      type: "number",
      min: 0.01,
      max: 999999,
      step: "0.01",
    },
  ],
  columns: [
    ...columns,
    { key: "scheduledAt", label: "Quando" },
    { key: "status", label: "Status" },
    { key: "score", label: "Nota" },
  ],
};
export const goalsConfig: ResourceConfig = {
  path: "goals",
  title: "Metas de estudo",
  singular: "meta",
  description:
    "Pequenos avanços, medidos de verdade. O progresso vem dos seus registros.",
  fields: [
    {
      key: "type",
      label: "Tipo de meta",
      type: "select",
      required: true,
      default: "STUDY_MINUTES",
      options: options("STUDY_MINUTES", "COMPLETED_TASKS"),
    },
    {
      key: "targetValue",
      label: "Objetivo (minutos ou tarefas)",
      type: "number",
      required: true,
      min: 1,
      max: 1000000,
    },
    subject,
    {
      key: "periodStart",
      label: "Início da semana",
      type: "date",
      required: true,
      default: monday(),
    },
    {
      key: "periodEnd",
      label: "Fim da semana (7 dias)",
      type: "date",
      required: true,
      default: addDays(monday(), 6),
    },
  ],
  columns: [
    { key: "type", label: "Meta" },
    { key: "subjectId", label: "Disciplina" },
    { key: "periodStart", label: "Semana" },
    { key: "targetValue", label: "Objetivo" },
    { key: "progress", label: "Progresso" },
  ],
};
export const reviewsConfig: ResourceConfig = {
  path: "reviews",
  title: "Revisões espaçadas",
  singular: "revisão",
  description:
    "Volte ao conteúdo na hora certa. Sua próxima revisão é calculada automaticamente.",
  fields: [
    title,
    subjectRequired,
    { key: "notes", label: "Anotações", type: "textarea", maxLength: 1000 },
  ],
  columns: [
    ...columns,
    { key: "nextReviewAt", label: "Próxima revisão" },
    { key: "reviewCount", label: "Revisões feitas" },
    { key: "intervalDays", label: "Intervalo (dias)" },
  ],
};
