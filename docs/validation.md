# Validação final do MVP — 13/10/2026

## Resultado

| Verificação | Resultado |
|---|---|
| Back-end unitário/Controller | 80 testes, zero falhas/erros/ignorados |
| Back-end integração PostgreSQL | 13 testes, zero falhas/erros/ignorados |
| Front-end Vitest/Testing Library | 22 testes, todos aprovados |
| Navegador Microsoft Edge/Playwright | 3 testes, todos aprovados |
| TypeScript | npm run typecheck aprovado |
| Build Front-end | npm run build aprovado |
| JAR Spring Boot | Gerado e iniciado; health UP/UP |
| Migrations | V1–V10 validadas/aplicadas no PostgreSQL |
| PostgreSQL Compose | running / healthy |
| Preservação | .env, Compose, logo, V1/V2 e disciplina existente preservados |

Total da suíte final: **118 testes**, sem duplicar a contagem de reexecuções.
Comandos Maven test e verify -Pintegration foram executados. O verify final também
executou os 80 testes unitários antes dos 13 de integração. Relatórios em
backend/target/surefire-reports e backend/target/failsafe-reports.

## Distribuição do Back-end

HealthControllerTest 4, HealthServiceTest 3, SubjectControllerTest 24,
SubjectServiceTest 8, MvpControllerTest 18, MvpRulesTest 18, PlannerEngineTest 5.
Integração: FoundationIT 1, SubjectIT 3, MvpIT 6 e PlanningIT 3.

Cobertura relevante: validação/404 de todos os novos CRUDs; referências e bloqueio
de exclusão; status/completedAt; duração e compatibilidade de sessão; reenvios
simultâneos; notas; metas reais incluindo reabertura; série diária e fuso; revisões;
algoritmo, conflitos, intervalos, preview, aceite, replay e revisão de dados alterados.
Nenhum teste preexistente foi removido ou desativado.

## Front-end e navegador

Vitest: cronômetro absoluto, pausas, retomada, chave persistida do ciclo,
recuperação após reload, dados locais inválidos, conversões São Paulo/UTC,
cliente HTTP, erros por campo, formulários, listas vazias, navegação e preferências.

Playwright usou Edge instalado em modo headless, sem baixar navegador. Verificou:
criação real de disciplinas, tarefas, eventos, sessões, provas, metas e revisões;
conclusão/reabertura; calendário mensal/semanal/diário; indicadores com registros
reais; preview do planejamento; Pomodoro com pausa, retomada, foco salvo e reload;
menu mobile e ausência de rolagem horizontal a 390px. Capturas em docs/screenshots.

A verificação usou API temporária 8081 e Vite 5174, porque havia instâncias anteriores
em 8080/5173. Essas instâncias existentes não foram encerradas. Os servidores
iniciados pelo agente para QA foram encerrados ao terminar a verificação.
Todos os registros E2E foram removidos pelos IDs criados. PlanningIT usa rollback.
A disciplina existente ficou idêntica ao snapshot após o QA. Não há registros
residuais dos testes nos novos módulos.

## Build e versões verificadas

Node 24.21.0, React 19.3.0, Vite 7.3.7, TypeScript 5.9.3, Tailwind 4.3.3,
FullCalendar 6.1.21, Recharts 3.10.1, Vitest 3.2.7 e Playwright 1.55.1.
Java Temurin 21.0.12.1, Spring Boot 3.5.16, Maven 3.9.11 e PostgreSQL 17.11.
Versões instaladas fixadas em package-lock.json/Maven parent.

Build final: entrada JS ~380 kB, calendário ~264 kB e dashboard/gráficos ~369 kB
antes de gzip. Carregamento por rota eliminou o aviso de chunk inicial acima de
500 kB. Inter é servida localmente; nenhuma fonte externa é necessária.

## Problemas encontrados e resolvidos

- Restrição de leitura do sandbox impediu esbuild inicialmente. Verificações foram
  executadas com acesso adequado, sem alteração do código para contornar o erro.
- Dois mocks de testes do Front-end foram corrigidos; a suíte final usa fetch
  simulado e os componentes reais para validar sucesso e erro.
- Empacotar enquanto a API temporária usava o mesmo JAR causou bloqueio no Windows.
  Somente a API temporária foi encerrada; o JAR foi reconstruído e a integração e
  navegador passaram novamente. O README explica esse cuidado ao usar java -jar.
- CSS de botões sobrepunha display:none do menu desktop. Corrigido e capturas
  atualizadas. Cores do calendário agora escolhem texto preto/branco por luminância.

Avisos não impeditivos: Mockito informa carregamento dinâmico de agente no Java;
npm avisou sobre dependência transitiva whatwg-encoding; Playwright mostrou aviso
de variáveis de cor do terminal. Nenhuma dessas mensagens falhou os testes finais.

## Limites da verificação

Não foi realizada publicação ou auditoria formal de acessibilidade. Layout foi
verificado em Edge desktop/mobile, não em todos os navegadores/dispositivos.
Não há testes de carga nem garantia de alarmes com o navegador fechado. O MVP
permanece local e sem autenticação. Limitações funcionais estão no README.
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