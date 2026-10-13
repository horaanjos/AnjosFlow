import { PORTFOLIO_MODE } from "../services/environment";
import { useState, lazy, Suspense } from "react";
import { NavLink, Routes, Route, Link, useLocation } from "react-router-dom";
import {
  LayoutDashboard,
  BookOpen,
  ListTodo,
  CalendarDays,
  Timer,
  Clock3,
  GraduationCap,
  Target,
  RotateCcw,
  CalendarRange,
  Settings,
  Menu,
  X,
  ChevronRight,
  ArrowUpRight,
} from "lucide-react";
import { DateTime } from "luxon";
import { useApp } from "./context";
const DashboardPage = lazy(() =>
  import("../pages/DashboardPage").then((m) => ({ default: m.DashboardPage })),
);
const CalendarPage = lazy(() =>
  import("../pages/CalendarPage").then((m) => ({ default: m.CalendarPage })),
);
import { PomodoroPage } from "../pages/PomodoroPage";
import { PlanningPage } from "../pages/PlanningPage";
import { SettingsPage } from "../pages/SettingsPage";
import { ResourcePage } from "../features/resources/ResourcePage";
import {
  subjectsConfig,
  tasksConfig,
  sessionsConfig,
  examsConfig,
  goalsConfig,
  reviewsConfig,
} from "../features/resources/config";
import type { Subject, Task, Session, Exam, Goal, Review } from "../types/api";
import { ZONE } from "../services/dates";
import { Empty, ErrorState, Loading } from "../components/ui";
const navigation = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard },
  { to: "/subjects", label: "Disciplinas", icon: BookOpen },
  { to: "/tasks", label: "Tarefas", icon: ListTodo },
  { to: "/calendar", label: "Calendário", icon: CalendarDays },
  { to: "/pomodoro", label: "Pomodoro", icon: Timer },
  { to: "/sessions", label: "Sessões de estudo", icon: Clock3 },
  { to: "/exams", label: "Provas", icon: GraduationCap },
  { to: "/goals", label: "Metas", icon: Target },
  { to: "/reviews", label: "Revisões", icon: RotateCcw },
  { to: "/planning", label: "Planejamento", icon: CalendarRange },
];
export function App() {
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();
  const { preferences, catalogError, refreshCatalog } = useApp();
  const title =
    navigation.find((n) => n.to === location.pathname)?.label ||
    "Configurações";
  return (
    <div className="app-shell">
      <a className="skip-link" href="#main">
        Ir para o conteúdo
      </a>
      {menuOpen && (
        <button
          className="sidebar-backdrop"
          aria-label="Fechar menu"
          onClick={() => setMenuOpen(false)}
        />
      )}
      <aside className={`sidebar ${menuOpen ? "open" : ""}`}>
        <Link className="brand" to="/" onClick={() => setMenuOpen(false)}>
          <img src="/anjosflow-logo.png" alt="Símbolo AnjosFlow" />
          <span>
            Anjos<span>Flow</span>
            <small>Organize. Aprenda. Evolua.</small>
          </span>
        </Link>
        <button
          className="mobile-close icon-button"
          aria-label="Fechar menu"
          onClick={() => setMenuOpen(false)}
        >
          <X size={20} />
        </button>
        <span className="nav-label">MEU ESPAÇO</span>
        <nav aria-label="Navegação principal">
          {navigation.map((n) => (
            <NavLink
              to={n.to}
              key={n.to}
              end={n.to === "/"}
              onClick={() => setMenuOpen(false)}
            >
              <n.icon size={19} strokeWidth={1.7} />
              {n.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="sidebar-note">
            <span className="round-icon">
              <BookOpen size={19} />
            </span>
            <strong>Um passo por dia.</strong>
            <p>
              Consistência transforma
              <br />
              intenção em progresso.
            </p>
            <Link to="/planning" onClick={() => setMenuOpen(false)}>
              Planejar minha semana
              <ArrowUpRight size={15} />
            </Link>
          </div>
          <NavLink
            className="settings-link"
            to="/settings"
            onClick={() => setMenuOpen(false)}
          >
            <Settings size={18} />
            Configurações
          </NavLink>
          <span className="local-label">
            <i />
            {PORTFOLIO_MODE ? "Versão de portfólio" : "Espaço pessoal · local"}
          </span>
        </div>
      </aside>
      <div className="workspace">
        <header className="topbar">
          <div className="breadcrumbs">
            <button
              className="icon-button menu-toggle"
              aria-label="Abrir menu"
              aria-expanded={menuOpen}
              onClick={() => setMenuOpen(!menuOpen)}
            >
              <Menu size={22} />
            </button>
            <span>Meu espaço</span>
            <ChevronRight size={13} />
            <strong>{title}</strong>
          </div>
          <div className="topbar-right">
            <span className="today-label">
              <CalendarDays size={16} />
              {DateTime.now()
                .setZone(ZONE)
                .setLocale("pt-BR")
                .toFormat("ccc, dd LLL")}
            </span>
            <Link
              className="avatar"
              to="/settings"
              aria-label="Preferências pessoais"
            >
              {preferences.displayName
                ? preferences.displayName.slice(0, 2).toUpperCase()
                : "AF"}
            </Link>
          </div>
        </header>
        <main id="main" tabIndex={-1}>
          {PORTFOLIO_MODE && (
            <div className="portfolio-banner" role="status">
              <strong>Apresentação de portfólio</strong>
              <span>
                API e banco não estão publicados. Cadastros e indicadores reais
                estão disponíveis na execução local.
              </span>
            </div>
          )}
          {catalogError && (
            <ErrorState message={catalogError} retry={refreshCatalog} />
          )}
          <Suspense fallback={<Loading />}>
            <Routes>
              <Route path="/" element={<DashboardPage />} />
              <Route
                path="/subjects"
                element={<ResourcePage<Subject> config={subjectsConfig} />}
              />
              <Route
                path="/tasks"
                element={<ResourcePage<Task> config={tasksConfig} />}
              />
              <Route path="/calendar" element={<CalendarPage />} />
              <Route path="/pomodoro" element={<PomodoroPage />} />
              <Route
                path="/sessions"
                element={<ResourcePage<Session> config={sessionsConfig} />}
              />
              <Route
                path="/exams"
                element={<ResourcePage<Exam> config={examsConfig} />}
              />
              <Route
                path="/goals"
                element={<ResourcePage<Goal> config={goalsConfig} />}
              />
              <Route
                path="/reviews"
                element={<ResourcePage<Review> config={reviewsConfig} />}
              />
              <Route path="/planning" element={<PlanningPage />} />
              <Route path="/settings" element={<SettingsPage />} />
              <Route
                path="*"
                element={
                  <Empty
                    title="Página não encontrada"
                    text="Este caminho não está disponível."
                    action={
                      <Link className="button" to="/">
                        Voltar ao início
                      </Link>
                    }
                  />
                }
              />
            </Routes>
          </Suspense>
          <footer className="footer">
            AnjosFlow<span>Organize seu tempo. Evolua todos os dias.</span>
          </footer>
        </main>
      </div>
    </div>
  );
}
