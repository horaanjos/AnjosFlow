# AnjosFlow

![Logo oficial](anjosflow-logo.png.png)

**Organize seu tempo. Evolua todos os dias.**

Plataforma pessoal de organização acadêmica, estudos para concursos e produtividade.
Projeto de portfólio desenvolvido de forma incremental, com foco em Back-end Java.
A primeira versão funciona localmente com um único perfil; não possui autenticação.

## Estado atual

Fundação e CRUD de disciplinas implementados e validados: 39 testes unitários e 4 de integração passaram.
Maven Wrapper configurado com Maven 3.9.11 e Apache Wrapper 3.3.4.
Consulte docs/validation.md para os resultados mais recentes da validação.

Disponível: configuração Spring Boot, PostgreSQL via Compose, Flyway, endpoint de
saúde com consulta real ao banco, CORS local, tratamento de erros e testes iniciais.
CRUD de disciplinas implementado: veja [contrato e exemplos PowerShell](docs/subjects.md).
Tarefas, calendário, sessões de estudo, interface React, Swagger e autenticação ainda não foram implementados.

## Tecnologias e arquitetura

Java 21, Spring Boot 3.5.16, Maven, Spring Web, Data JPA, Validation,
PostgreSQL 17, Flyway, JUnit 5 e Mockito. Spring Boot gerencia as versões das
bibliotecas pelo parent Maven. Arquitetura: monorepo com monólito modular por
funcionalidade; Controllers recebem HTTP, Services executam regras e DTOs
representam respostas. Consulte [decisões arquiteturais](docs/architecture.md).

```text
backend/   API Java e testes
frontend/  reservado para a Etapa 3
assets/    reservado para arquivos de marca
docs/      decisões e validação
compose.yaml
.env.example
anjosflow-logo.png.png
```

## Pré-requisitos

Instale JDK 21 e Docker Desktop com Docker Compose v2. Maven é baixado pelo Wrapper.
Verifique em um novo terminal: `java -version`, `.ackendmvnw.cmd -v`, `docker version`.
Docker Desktop deve estar iniciado. Git é necessário quando você decidir versionar.
Nenhum commit ou push foi realizado nesta entrega.

## Configuração e execução (PowerShell)

Na raiz do projeto:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
notepad .env
```

Defina `POSTGRES_PASSWORD` com uma senha local. Use valores simples sem aspas;
este arquivo também é lido pelo Spring como propriedades. Não adicione o `.env`
ao Git. O Compose exige uma senha não vazia. As demais opções estão no exemplo.
O Spring lê `.env` tanto ao executar da raiz quanto de `backend/`; variáveis
exportadas no processo têm precedência. `JAVA_HOME` deve apontar para o JDK 21.

```powershell
docker compose config --quiet
docker compose up -d --wait
.ackendmvnw.cmd -f backend/pom.xml spring-boot:run
```

Em outro terminal na raiz:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Se alterar `SERVER_PORT`, ajuste também a URL. PostgreSQL e API estão restritos
a loopback. CORS permite somente `http://localhost:5173` por padrão.

```json
{"status":"UP","database":"UP","timestamp":"2026-10-13T20:00:00Z"}
```

O JSON acima ilustra o contrato, não uma resposta obtida nesta entrega.
`GET /api/health` retorna **200** com banco acessível ou **503** com `DOWN` se
uma consulta falhar após a inicialização. Se o banco estiver indisponível durante
a inicialização, Flyway impede a API de iniciar. Timestamp usa UTC ISO 8601.
Falhas inesperadas retornam **500** no formato Problem Detail, sem detalhes internos.

Flyway executa `V1__create_application_metadata.sql` automaticamente ao iniciar.
V1 cria metadados técnicos e V2 cria a tabela de disciplinas, com constraints e índice.
Hibernate usa `ddl-auto=validate`, sem criar ou atualizar tabelas automaticamente.

## Testes

Sem banco ou Docker (exige JDK; o Wrapper baixa Maven):

```powershell
.ackendmvnw.cmd -f backend/pom.xml test
```

Com PostgreSQL iniciado e `.env` configurado:

```powershell
.ackendmvnw.cmd -f backend/pom.xml verify -Pintegration
```

Os testes unitários cobrem banco saudável/indisponível, resultado inesperado,
HTTP 200/503/500 e método não permitido. `FoundationIT` inicializa a aplicação
em porta aleatória, consulta HTTP real e verifica a migração no PostgreSQL.
O teste de integração usa o banco configurado; prefira um banco local dedicado
a testes. Não limpa o schema. SubjectIT remove apenas os registros que ele próprio criou.
Relatórios ficam em `backend/target/surefire-reports` e `failsafe-reports`.
Consulte o [registro de validação](docs/validation.md).

Para gerar o JAR: `.ackendmvnw.cmd -f backend/pom.xml package`.
Para executá-lo da raiz: `java -jar backend/target/anjosflow-backend-0.1.0-SNAPSHOT.jar`.
Para parar os serviços preservando o volume: `docker compose stop`.
Alterar a senha no `.env` não altera a senha de um banco já inicializado.

## Front-end e identidade visual

Não há Front-end executável nesta etapa. React/TypeScript/Vite/Tailwind e a
organização dos arquivos de marca serão implementados na Etapa 3.
A imagem original está preservada como `anjosflow-logo.png.png`; nenhum símbolo
foi redesenhado. Não há screenshots da aplicação disponíveis.

## Roadmap

1. Fundação — validada com 7 testes unitários e 1 teste de integração no PostgreSQL.
2. Disciplinas — CRUD implementado. Tarefas, eventos e sessões aguardam autorização.
3. Identidade visual e Front-end integrado.
4. Calendário e faculdade.
5. Concursos.
6. Pomodoro, metas e revisões.
7. Planejamento de estudos.
8. Estatísticas e refinamento.
9. Revisão final para portfólio.

Cada etapa depende de autorização. Nesta Etapa 2 foi implementado exclusivamente o módulo de disciplinas.
## Maven Wrapper no PowerShell

O Back-end inclui `mvnw`, `mvnw.cmd` e `.mvn/wrapper/maven-wrapper.properties`.
São scripts oficiais Apache Wrapper 3.3.4, distribuição `only-script`, sem JAR
ou downloader Java. Maven 3.9.11 é fixado e tem checksum SHA256 configurado.
O primeiro uso exige internet; não é necessário instalar Maven globalmente.

Execute da pasta `backend` para usar o `.env` da raiz automaticamente:

```powershell
cd backend
.\mvnw.cmd -v
.\mvnw.cmd test
.\mvnw.cmd verify -Pintegration
.\mvnw.cmd spring-boot:run
```

Os comandos são independentes: `spring-boot:run` permanece no terminal até Ctrl+C.
O perfil `integration` exige o PostgreSQL disponível e aplica migrações pendentes.
A configuração `optional:file:../.env[.properties]` importa o `.env` da raiz
quando o diretório de execução é `backend`; `optional:file:.env[.properties]`
atende execução a partir da raiz. Não é preciso copiar `.env` para `backend`.