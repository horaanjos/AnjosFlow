# MVP — relatório final

## Concluído

Base preservada: Java 21, Boot 3.5.16, Wrapper, saúde, disciplinas, PostgreSQL,
Flyway e testes anteriores. Implementados tarefas, calendário, sessões, Pomodoro,
provas, metas, revisões, dashboard agregado e planejamento determinístico.
Front-end completo com 11 páginas/rotas, API real, formulários, confirmação de
exclusão, preferências locais, estados de erro/vazio/loading e responsividade.

Migrations novas: V3–V10. V1/V2 e a logo não foram alteradas. .env e Compose
permaneceram intactos. Disciplina cadastrada pelo usuário foi preservada.

## Verificação

80 testes unitários Back-end + 13 integração; 22 Front-end + 3 Edge: **118 aprovados**.
TypeScript e build de produção aprovados. JAR iniciou, conectou ao PostgreSQL e
respondeu health UP/UP. Detalhes e problemas resolvidos em validation.md.
Servidores temporários de QA 8081/5174 encerrados; não houve commits, push,
publicação ou interrupção de processos do usuário.

## Executar

Raiz: docker compose up -d --wait.
Backend: .\mvnw.cmd spring-boot:run.
Frontend: npm.cmd ci; npm.cmd run dev.
Navegador: http://localhost:5173 (API http://localhost:8080).
Se uma instância antiga ocupar a porta, reinicie-a manualmente no seu terminal.

Testes: backend/.\mvnw.cmd test e verify -Pintegration; frontend/npm.cmd test,
run typecheck, run build e run test:e2e (último exige API/Vite/Edge).

## Pendências fora do MVP local

Autenticação/autorização antes de exposição pública, contas múltiplas, implantação,
paginação, exportação/backup, histórico por ocorrência de revisão e preferências
sincronizadas. Não há funcionalidades autorizadas deste MVP aguardando implementação.
Limites deliberados: provas com 1h na agenda, sessões imutáveis, metas de dois tipos,
revisões simples e planner baseado em tarefas. Regras documentadas em business-rules.md
 e planner.md. Não há cronômetro do sistema com navegador fechado.
## Publicação de portfólio — 13/10/2026

Código completo enviado a https://github.com/horaanjos/AnjosFlow (público, branch master).
Front-end publicado em https://anjosflow-horaanjos.netlify.app. Back-end e PostgreSQL
não foram hospedados. Não foi implementado login de aplicação nesta etapa.

26 testes do Front-end aprovados (22 existentes + 4 do modo de portfólio).
TypeScript e build em VITE_PORTFOLIO_MODE=true aprovados. Verificação do site real
no Edge passou: HTTP 200, rotas SPA diretas, formulário sem gravação, Pomodoro
iniciando/pausando, planejamento sem API desabilitado, mobile sem overflow,
zero chamadas à API/localhost e zero erros JavaScript.

A Netlify inicialmente exigia login do visitante (401). A exigência foi removida
somente neste projeto e a verificação pública passou. .env e credenciais de GitHub
 e Netlify permaneceram fora do Git. A publicação atual é manual via CLI;
commits futuros não disparam deploy automaticamente até conectar o repositório
pela integração Git da Netlify. netlify.toml já contém os parâmetros para isso.