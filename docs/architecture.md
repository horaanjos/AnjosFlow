# Decisões arquiteturais — Etapa 1

- Monorepo simples: um Back-end, um Front-end futuro e documentação compartilhada.
- Java 21 e Spring Boot 3.5.16: base compatível com JUnit 5 e bibliotecas gerenciadas
  pelo parent Maven. Não há versões avulsas de dependências sem necessidade.
- Monólito modular no pacote `com.anjosflow`. Os módulos de negócio só serão criados
  quando implementados. Não há microsserviços nem abstrações antecipadas.
- `AnjosFlowApplication.main` inicia o contexto Spring e o servidor embutido.
- `HealthController.health` converte o estado da saúde em HTTP 200 ou 503.
- `HealthService.check` consulta `SELECT 1` via JdbcTemplate. Uma resposta estática
  não comprovaria acesso ao banco. A consulta JDBC é suficiente para essa operação;
  entidades de negócio usarão JPA nas próximas etapas.
- `HealthResponse` é um record imutável: contrato da API sem entidades JPA expostas.
- `GlobalExceptionHandler` reutiliza o tratamento Spring de erros HTTP e sanitiza
  exceções inesperadas. Não retorna SQL, senha ou stack trace ao cliente.
- `WebConfig` limita CORS à origem configurada do Front-end local.
- PostgreSQL em volume persistente, porta vinculada a 127.0.0.1. A API também
  escuta apenas em loopback. CORS não substitui autenticação.
- Flyway é responsável pelo schema; Hibernate apenas valida. A primeira migração
  cria metadados técnicos para comprovar a execução do fluxo de migração.
- Configuração sem senha padrão. `.env` é ignorado pelo Git e importado pelo Spring.
- Testes unitários independem de infraestrutura; integração é opt-in pelo perfil
  Maven `integration` e usa PostgreSQL real, sem substituir o banco por H2.
- Swagger/OpenAPI, perfil local e entidades de negócio ficam para as etapas seguintes.

## Exercício

Leia `HealthService.check` e os seus testes. Explique por que uma falha de conexão
resulta em 503, enquanto uma exceção inesperada resulta em 500. Depois de instalar
as ferramentas e iniciar a API, pare o PostgreSQL com `docker compose stop postgres`,
consulte `/api/health` e reinicie com `docker compose start postgres`.
## Etapa 2 — disciplinas

O módulo subject possui Controller, Service transacional, Repository JPA,
Entity e DTOs separados. A entidade não sai pela API. Bean Validation é aplicada
no Controller após normalização do nome no construtor do DTO. O Service define
a ordenação por name/id e lança uma exceção específica para ausência de registro.
PUT substitui os campos editáveis e aplica o padrão de cor quando omitida.

Callbacks JPA gerenciam auditoria, sem setters públicos para IDs/timestamps.
Instant é truncado em microssegundos para manter respostas e armazenamento
PostgreSQL consistentes. saveAndFlush executa auditoria antes de gerar o DTO.
V2 cria tabela e constraints; V1 é preservada. Não há owner/user nesta etapa,
conforme escopo autorizado; isso deverá evoluir com autenticação futuramente.

GlobalExceptionHandler adiciona Problem Detail para validação, JSON malformado
 e disciplina não encontrada. Não expõe valores rejeitados ou detalhes de SQL.
SubjectIT usa HTTP real em porta aleatória e remove somente seus próprios IDs.