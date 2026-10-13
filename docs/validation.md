# Validação da fundação

## Resultado atual — 13/10/2026

- Apache Maven Wrapper 3.3.4 oficial, distribuição only-script.
- Maven 3.9.11 executado com JDK Temurin 21.0.12.1.
- Arquivo ZIP Maven conferido pelo SHA512 publicado no Maven Central.
- distributionSha256Sum configurado para verificar downloads do Wrapper.
- PostgreSQL do Compose: running/healthy.
- `.\mvnw.cmd -B test`, executado de backend: BUILD SUCCESS; 7 testes, sem falhas.
- `.\mvnw.cmd -B -ntp verify -Pintegration`, executado de backend: BUILD SUCCESS;
  7 testes unitários e 1 teste de integração, sem falhas ou testes ignorados.
- Spring carregou POSTGRES_HOST, POSTGRES_PORT e POSTGRES_DB do `.env` da raiz
  via `optional:file:../.env[.properties]`. Credenciais não foram impressas.
- Flyway aplicou V1 no PostgreSQL; o teste confirmou os metadados e histórico.
- A API iniciou em porta aleatória e GET /api/health respondeu HTTP 200,
  com status UP e database UP. O servidor do teste encerrou ao finalizar.
- O JAR executável foi gerado em backend/target pelo comando verify.

A configuração de aplicação e todo o código Java foram preservados.
Há avisos do Mockito sobre carregamento dinâmico de agente em versões futuras
 do Java; não impediram os testes no Java 21.

## Histórico

Na criação inicial, Java, Maven e Docker não estavam disponíveis. Essa limitação
foi resolvida após instalação das ferramentas e configuração do Wrapper.
A versão Maven 3.9.16 consultada inicialmente não estava disponível no repositório;
foi utilizada a distribuição estável 3.9.11, baixada e executada com sucesso.

## Wrapper

Arquivos: backend/mvnw, backend/mvnw.cmd,
backend/.mvn/wrapper/maven-wrapper.properties.
Não é necessário maven-wrapper.jar nem MavenWrapperDownloader.java no modo
only-script. Os scripts baixam Maven no cache do usuário em `.m2/wrapper/dists`.
A primeira execução exige internet. Não houve commits, push ou avanço de etapa.

## Referências oficiais

- https://maven.apache.org/tools/wrapper/maven-wrapper-plugin/usage.html
- https://maven.apache.org/tools/mavenwrapper.html
- https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/
## Etapa 2 — CRUD de disciplinas — 13/10/2026

Executados a partir de backend (flags -B e -ntp apenas reduzem saída interativa):

- `.\mvnw.cmd test -B -ntp`: BUILD SUCCESS, 39 testes unitários, zero falhas,
  erros ou testes ignorados.
- `.\mvnw.cmd verify -Pintegration -B -ntp`: BUILD SUCCESS, mesmos 39 testes
  unitários e 4 testes de integração, zero falhas, erros ou testes ignorados.

Distribuição: HealthControllerTest 4, HealthServiceTest 3, SubjectControllerTest 24,
SubjectServiceTest 8, FoundationIT 1, SubjectIT 3. Todos os testes da Etapa 1
continuam passando. V2 foi aplicada ao PostgreSQL real com sucesso; Hibernate
validou o schema. SubjectIT validou CRUD HTTP completo, ordenação por nome/ID,
cor padrão, auditoria, 400 e 404; os registros criados foram removidos pelo teste.

A porta 8080 estava ocupada por uma instância existente. Ela não foi interrompida;
o servidor de integração iniciou em porta aleatória (56998 nesta execução) e
encerrou normalmente ao terminar. Não houve conflitos de porta nem de banco.
A API já aberta precisa ser reiniciada manualmente para carregar o novo módulo.

Verificação SHA256 confirmou preservação de .env, compose.yaml, imagem da logo,
V1, application.properties e pom.xml. Não houve alteração de credenciais,
dependências ou configuração do Docker. Avisos do Mockito sobre agente dinâmico
continuam presentes, sem falhar os testes. O log ERROR dos testes de saúde é uma
exceção simulada para verificar HTTP 500, não uma falha da execução.

Relatórios XML: backend/target/surefire-reports e backend/target/failsafe-reports.