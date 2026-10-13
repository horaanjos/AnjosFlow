# Etapa 2 — CRUD de disciplinas

Módulo `com.anjosflow.subject`, sem Front-end, autenticação ou outros recursos.

## Contrato

| Método | Endpoint | Sucesso |
|---|---|---|
| POST | `/api/subjects` | 201, DTO criado e header Location |
| GET | `/api/subjects` | 200, array ordenado por name e id |
| GET | `/api/subjects/{id}` | 200, DTO da disciplina |
| PUT | `/api/subjects/{id}` | 200, DTO atualizado |
| DELETE | `/api/subjects/{id}` | 204, sem corpo |

Busca, atualização e exclusão de ID inexistente retornam 404. Dados inválidos,
JSON malformado e ID não numérico retornam 400. Falhas inesperadas retornam
500 sanitizado. A listagem vazia retorna `[]`. Não há paginação nem unicidade
por nome; disciplinas com nomes iguais são desempatadas pelo ID.

### Criação

```json
{
  "name": "  Engenharia de Software  ",
  "description": "Estudos de arquitetura e requisitos",
  "color": "#527C68"
}
```

Resposta ilustrativa, HTTP 201, Location `/api/subjects/1`:

```json
{
  "id": 1,
  "name": "Engenharia de Software",
  "description": "Estudos de arquitetura e requisitos",
  "color": "#527C68",
  "createdAt": "2026-10-13T21:00:00Z",
  "updatedAt": "2026-10-13T21:00:00Z"
}
```

`name` é obrigatório e deve ter de 2 a 100 caracteres após `strip()`, que remove
espaços nas extremidades. Os espaços internos são preservados. `description`
é opcional, com até 500 caracteres. `color` aceita `#RRGGBB`, com letras maiúsculas
ou minúsculas; omitido ou null usa `#A3B18A`. Cor vazia não é válida.
IDs e timestamps são gerados pelo servidor e não fazem parte do DTO de entrada.

### Atualização completa (PUT)

```json
{
  "name": "Arquitetura de Software",
  "description": "Conteúdo atualizado",
  "color": "#416653"
}
```

PUT substitui os campos editáveis. Omitir `description` remove a descrição;
omitir `color` restaura o padrão. `id` e `createdAt` são preservados. `updatedAt`
é atualizado quando há mudança efetiva, por callback JPA antes da resposta.
Timestamps usam Instant/UTC, precisão de microssegundos compatível com PostgreSQL,
e colunas TIMESTAMPTZ. Reenviar os mesmos valores não força alteração de auditoria.

### Erros

Exemplo ilustrativo de validação, HTTP 400, JSON Problem Detail:

```json
{
  "type": "about:blank",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Verifique os campos informados.",
  "errors": {
    "name": "O nome da disciplina é obrigatório.",
    "color": "A cor deve estar no formato #RRGGBB."
  }
}
```

Uma mensagem por campo é retornada. ID inexistente retorna `status: 404`,
`title: "Disciplina não encontrada"` e `detail: "Disciplina não encontrada."`.
Valores rejeitados, SQL e stack traces não são incluídos nas respostas.

## Arquivos e arquitetura

- `SubjectController`: recebe HTTP, valida DTOs e define status/Location.
- `SubjectService`: controla transações, busca registros, ordena e executa CRUD.
- `SubjectRepository`: persistência JPA de Subject com chave Long.
- `Subject`: entidade, identidade automática e callbacks de auditoria.
- `SubjectRequest`: campos editáveis, normalização e Bean Validation.
- `SubjectResponse`: DTO imutável; entidades nunca são retornadas na API.
- `SubjectNotFoundException`: sinaliza ausência do registro.
- `GlobalExceptionHandler`: reaproveitado e ampliado para 400 e 404.
- `V2__create_subjects.sql`: tabela, constraints e índice (name, id).

V1 não foi alterada. Hibernate permanece com ddl-auto=validate. Nenhuma nova
dependência foi adicionada. `.env`, Docker Compose, logo e configuração de
conexão permanecem preservados.

## Testes (PowerShell)

A partir de backend:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify -Pintegration
```

O segundo comando exige PostgreSQL iniciado e `.env` configurado. Usa porta
aleatória; não ocupa 8080, não encerra a API existente e não limpa o banco.
`SubjectIT` cria registros com prefixo único, guarda seus IDs e remove somente
esses registros ao terminar. Não utiliza deleteAll nem truncate.
Os testes da Etapa 1 permanecem ativos. Resultados em docs/validation.md.

## Testar manualmente com curl.exe no PowerShell

A API que estava aberta antes da implementação contém as classes antigas.
Após encerrá-la manualmente com Ctrl+C no terminal original, execute de backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Não execute uma segunda instância na porta 8080. Em outro terminal:

```powershell
$baseUrl = 'http://localhost:8080' # Ajuste se SERVER_PORT for diferente.
$createFile = Join-Path $env:TEMP ('anjosflow-create-' + [guid]::NewGuid() + '.json')
$updateFile = Join-Path $env:TEMP ('anjosflow-update-' + [guid]::NewGuid() + '.json')
$utf8 = [System.Text.UTF8Encoding]::new($false)

# Arquivo UTF-8 evita problemas de aspas JSON no Windows PowerShell.
[System.IO.File]::WriteAllText($createFile, '{"name":"Engenharia de Software","description":"Estudos de arquitetura","color":"#527C68"}', $utf8)

# Criar e guardar o ID retornado.
$created = curl.exe -sS --fail-with-body -X POST "$baseUrl/api/subjects" -H 'Content-Type: application/json' --data-binary "@$createFile" | ConvertFrom-Json
$created
$subjectId = $created.id

# Listar e consultar, exibindo status HTTP.
curl.exe -sS -i "$baseUrl/api/subjects"
curl.exe -sS -i "$baseUrl/api/subjects/$subjectId"

# Atualizar o registro recém-criado.
[System.IO.File]::WriteAllText($updateFile, '{"name":"Arquitetura de Software","description":"Conteúdo atualizado","color":"#416653"}', $utf8)
curl.exe -sS -i -X PUT "$baseUrl/api/subjects/$subjectId" -H 'Content-Type: application/json' --data-binary "@$updateFile"

# Excluir somente o registro criado neste exemplo (204), depois conferir 404.
curl.exe -sS -i -X DELETE "$baseUrl/api/subjects/$subjectId"
curl.exe -sS -i "$baseUrl/api/subjects/$subjectId"

# Conferir que a saúde da API permanece disponível.
curl.exe -sS -i "$baseUrl/api/health"
```

DELETE é irreversível: use apenas o ID retornado na criação deste exemplo.
Os arquivos temporários podem ser removidos após testar com
`Remove-Item -LiteralPath $createFile, $updateFile`.

## Exercício

Identifique onde @Valid rejeita uma cor inválida antes do Service. Depois envie
um nome com espaços nas extremidades e veja o valor persistido. Explique por
que PUT com cor omitida restaura o padrão, enquanto createdAt não muda.