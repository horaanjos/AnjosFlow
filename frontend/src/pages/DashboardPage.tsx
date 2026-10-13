import { PORTFOLIO_MODE } from "../services/environment";
import { PortfolioDashboard } from "./PortfolioDashboard";
import {
  BookOpen,
  CheckCircle2,
  Clock3,
  Flame,
  ArrowUpRight,
  CalendarDays,
  Timer,
  Sun,
} from "lucide-react";
import { Link } from "react-router-dom";
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from "recharts";
import { useApi } from "../hooks/useApi";
import { useApp } from "../app/context";
import { dateLabel, minutesLabel } from "../services/dates";
import {
  PageTitle,
  Loading,
  ErrorState,
  Empty,
  SectionLink,
  Badge,
} from "../components/ui";
import type { Dashboard } from "../types/api";
export function DashboardPage() {
  const result = useApi<Dashboard>("/dashboard");
  const { preferences } = useApp();
  if (PORTFOLIO_MODE) return <PortfolioDashboard />;
  if (result.loading) return <Loading />;
  if (result.error)
    return <ErrorState message={result.error} retry={result.reload} />;
  const data = result.data!;
  return (
    <>
      <PageTitle
        eyebrow="UM DIA DE CADA VEZ"
        title={
          preferences.displayName
            ? `Seu próximo passo, ${preferences.displayName}.`
            : "Seu próximo passo começa aqui."
        }
        description="Um espaço para organizar a mente, cuidar do tempo e seguir em frente."
        action={
          <Link className="button secondary" to="/calendar">
            <CalendarDays size={17} />
            Ver meu calendário
          </Link>
        }
      />
      <section className="focus-banner">
        <div>
          <span className="banner-tag">
            <Sun size={16} />
            CONSTÂNCIA VALE MAIS QUE PRESSA
          </span>
          <h2>
            Reserve um momento
            <br />
            para aprender algo novo.
          </h2>
          <p>
            Escolha uma disciplina, respire e comece.
            <br />
            Cada sessão conta para o seu progresso.
          </p>
          <Link className="button light" to="/pomodoro">
            <Timer size={17} />
            Começar uma sessão
            <ArrowUpRight size={16} />
          </Link>
        </div>
        <div className="banner-orbit" aria-hidden="true">
          <div className="orbit-inner">
            <BookOpen size={48} strokeWidth={1} />
            <span>
              Seu tempo.
              <br />
              Seu ritmo.
            </span>
          </div>
        </div>
      </section>
      <div className="stats-grid">
        {[
          {
            label: "Disciplinas",
            value: data.totalSubjects,
            icon: BookOpen,
            detail: "Sua base de conhecimento",
            to: "/subjects",
          },
          {
            label: "Tarefas pendentes",
            value: data.pendingTasks,
            icon: CheckCircle2,
            detail: `${data.overdueTasks} com prazo vencido`,
            to: "/tasks",
          },
          {
            label: "Estudo de hoje",
            value: minutesLabel(data.minutesToday),
            icon: Clock3,
            detail: "Tempo de foco registrado",
            to: "/sessions",
          },
          {
            label: "Nesta semana",
            value: minutesLabel(data.weekMinutes),
            icon: Flame,
            detail: `Desde ${dateLabel(data.weekStart)}`,
            to: "/sessions",
          },
        ].map((stat) => (
          <Link to={stat.to} className="card stat-card" key={stat.label}>
            <div className="stat-top">
              <span>{stat.label}</span>
              <stat.icon size={19} />
            </div>
            <strong>{stat.value}</strong>
            <small>{stat.detail}</small>
          </Link>
        ))}
      </div>
      <div className="dashboard-grid">
        <section className="card">
          <div className="section-heading">
            <div>
              <h2>Seu ritmo de estudo</h2>
              <p>Minutos registrados nos últimos 7 dias</p>
            </div>
            <Badge>Últimos 7 dias</Badge>
          </div>
          {data.dailyStudy.some((d) => d.minutes > 0) ? (
            <div className="chart">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={data.dailyStudy}>
                  <CartesianGrid
                    strokeDasharray="3 3"
                    vertical={false}
                    stroke="#E8EDE7"
                  />
                  <XAxis
                    dataKey="day"
                    tickFormatter={(d) => dateLabel(d).slice(0, 6)}
                    axisLine={false}
                    tickLine={false}
                    fontSize={12}
                  />
                  <YAxis
                    axisLine={false}
                    tickLine={false}
                    width={35}
                    fontSize={12}
                  />
                  <Tooltip
                    formatter={(v) => [`${v} min`, "Estudo"]}
                    labelFormatter={(v) => dateLabel(String(v))}
                  />
                  <Area
                    type="monotone"
                    dataKey="minutes"
                    stroke="#527C68"
                    fill="#E4EBE0"
                    strokeWidth={2}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <Empty
              title="Seu progresso começa na primeira sessão"
              text="Registre um estudo ou conclua um Pomodoro para ver sua evolução."
              action={
                <SectionLink to="/sessions">Registrar estudo</SectionLink>
              }
            />
          )}
        </section>
        <section className="card">
          <div className="section-heading">
            <div>
              <h2>Metas da semana</h2>
              <p>Progresso calculado a partir dos seus registros</p>
            </div>
            <SectionLink to="/goals">Ver todas</SectionLink>
          </div>
          {data.goals.filter(
            (p) =>
              p.goal.periodStart <= data.today &&
              p.goal.periodEnd >= data.today,
          ).length ? (
            data.goals
              .filter(
                (p) =>
                  p.goal.periodStart <= data.today &&
                  p.goal.periodEnd >= data.today,
              )
              .map((p) => (
                <div className="goal-row" key={p.goal.id}>
                  <div>
                    <strong>
                      {p.goal.type === "STUDY_MINUTES"
                        ? "Tempo de estudo"
                        : "Tarefas concluídas"}
                    </strong>
                    <span>{p.percentage}%</span>
                  </div>
                  <progress max={100} value={p.percentage} />
                  <small>
                    {p.currentValue} de {p.goal.targetValue}{" "}
                    {p.goal.type === "STUDY_MINUTES" ? "minutos" : "tarefas"}
                  </small>
                </div>
              ))
          ) : (
            <Empty
              title="Dê uma direção à sua semana"
              text="Uma meta simples ajuda a manter a constância."
              action={<SectionLink to="/goals">Criar uma meta</SectionLink>}
            />
          )}
        </section>
        <section className="card">
          <div className="section-heading">
            <div>
              <h2>Próximas avaliações</h2>
              <p>Prepare-se com tranquilidade</p>
            </div>
            <SectionLink to="/exams">Ver todas</SectionLink>
          </div>
          {data.upcomingExams.length ? (
            <div className="agenda-list">
              {data.upcomingExams.map((e) => (
                <Link className="agenda-row" key={e.id} to="/exams">
                  <div className="date-tile">
                    {dateLabel(e.scheduledAt).slice(0, 6)}
                  </div>
                  <div>
                    <strong>{e.title}</strong>
                    <p>
                      {dateLabel(e.scheduledAt, true)}
                      {e.location ? ` · ${e.location}` : ""}
                    </p>
                  </div>
                  <ArrowUpRight size={16} />
                </Link>
              ))}
            </div>
          ) : (
            <Empty
              title="Nenhuma prova por enquanto"
              text="Cadastre uma avaliação para organizar sua preparação."
            />
          )}
        </section>
        <section className="card">
          <div className="section-heading">
            <div>
              <h2>Hora de revisar</h2>
              <p>Reforce o que você já aprendeu</p>
            </div>
            <SectionLink to="/reviews">Ver todas</SectionLink>
          </div>
          {data.nextReviews.length ? (
            <div className="agenda-list">
              {data.nextReviews.map((r) => (
                <Link className="agenda-row" key={r.id} to="/reviews">
                  <div className="round-icon">
                    <BookOpen size={18} />
                  </div>
                  <div>
                    <strong>{r.title}</strong>
                    <p className={r.nextReviewAt < data.today ? "overdue" : ""}>
                      {r.nextReviewAt < data.today ? "Revisão atrasada · " : ""}
                      {dateLabel(r.nextReviewAt)}
                    </p>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <Empty
              title="Mantenha o aprendizado vivo"
              text="Crie revisões para voltar aos conteúdos no momento certo."
            />
          )}
        </section>
      </div>
      <section className="card distribution">
        <div className="section-heading">
          <div>
            <h2>Tempo por disciplina</h2>
            <p>Distribuição de todo o tempo registrado</p>
          </div>
          <Badge>{data.completedTasks} tarefas concluídas</Badge>
        </div>
        {data.distribution.length ? (
          <div className="distribution-grid">
            {data.distribution.map((s) => (
              <div key={s.subjectId}>
                <span>
                  <i style={{ background: s.color }} />
                  {s.name}
                </span>
                <strong>{minutesLabel(s.minutes)}</strong>
                <progress
                  max={Math.max(1, ...data.distribution.map((s) => s.minutes))}
                  value={s.minutes}
                />
              </div>
            ))}
          </div>
        ) : (
          <p className="muted">
            Quando você registrar uma sessão, sua distribuição aparecerá aqui.
          </p>
        )}
      </section>
    </>
  );
}
