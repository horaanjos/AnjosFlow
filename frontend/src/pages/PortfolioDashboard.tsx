import { Link } from "react-router-dom";
import {
  Github,
  Timer,
  ArrowUpRight,
  BookOpen,
  Database,
  Code2,
} from "lucide-react";
import { PageTitle, Badge } from "../components/ui";
import { REPOSITORY_URL } from "../services/environment";
export function PortfolioDashboard() {
  return (
    <>
      <PageTitle
        eyebrow="PROJETO FULL STACK · PORTFÓLIO"
        title="Organize seu tempo. Evolua todos os dias."
        description="AnjosFlow: organização acadêmica e produtividade com Java, Spring Boot, PostgreSQL e React."
        action={
          <a
            className="button secondary"
            href={REPOSITORY_URL}
            target="_blank"
            rel="noreferrer"
          >
            <Github size={17} />
            Ver código no GitHub
          </a>
        }
      />
      <section className="focus-banner">
        <div>
          <span className="banner-tag">DO PLANEJAMENTO AO PROGRESSO</span>
          <h2>
            Um espaço para aprender
            <br />
            no seu próprio ritmo.
          </h2>
          <p>
            Explore a interface e experimente o cronômetro.
            <br />O projeto completo, com persistência real, roda localmente.
          </p>
          <Link className="button light" to="/pomodoro">
            <Timer size={17} />
            Experimentar Pomodoro
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
      <div className="portfolio-features">
        {[
          {
            title: "Back-end Java",
            description:
              "API REST modular, validações, auditoria e regras de negócio testadas.",
            icon: Code2,
          },
          {
            title: "PostgreSQL + Flyway",
            description:
              "Dados reais, integridade referencial e dez migrações versionadas.",
            icon: Database,
          },
          {
            title: "Interface React",
            description:
              "Calendário, tarefas, metas e revisões, com navegação responsiva.",
            icon: BookOpen,
          },
        ].map((item) => (
          <section className="card" key={item.title}>
            <span className="round-icon">
              <item.icon size={21} />
            </span>
            <h2>{item.title}</h2>
            <p className="muted">{item.description}</p>
          </section>
        ))}
      </div>
      <section className="card portfolio-preview">
        <div className="section-heading">
          <div>
            <h2>A aplicação completa em funcionamento</h2>
            <p>
              Captura real da execução local; estes indicadores não são dados ao
              vivo.
            </p>
          </div>
          <Badge>Prévia do projeto</Badge>
        </div>
        <a
          href="/portfolio/dashboard-desktop.png"
          target="_blank"
          rel="noreferrer"
        >
          <img
            src="/portfolio/dashboard-desktop.png"
            alt="Captura real do dashboard AnjosFlow conectado ao PostgreSQL local"
            loading="lazy"
          />
        </a>
      </section>
      <section className="card portfolio-about">
        <h2>Construído para aprender. Preparado para evoluir.</h2>
        <p>
          O MVP inclui disciplinas, tarefas, eventos, sessões, provas, metas,
          revisões e planejamento semanal determinístico. O dashboard agrega
          dados no PostgreSQL e o Pomodoro acompanha o tempo real.
        </p>
        <p>
          Nesta publicação, o Back-end e o banco permanecem locais. Cadastros e
          indicadores ao vivo ficam disponíveis ao executar o repositório
          completo. Nenhuma informação de estudo é inventada nesta apresentação.
        </p>
        <a
          className="text-link"
          href={REPOSITORY_URL + "/blob/master/README.md"}
          target="_blank"
          rel="noreferrer"
        >
          Como executar o projeto completo
          <ArrowUpRight size={15} />
        </a>
      </section>
    </>
  );
}
