# API REST do MVP

Base local: `http://localhost:8080/api`. Datas com horário são ISO 8601 com offset
ou Z (UTC). Datas civis usam yyyy-MM-dd. Content-Type de entrada: application/json.
DTOs nunca expõem entidades JPA. Campos de ID e auditoria não são aceitos como
campos editáveis. Não há autenticação nesta versão local.

## Endpoints

| Recurso | Rotas |
|---|---|
| Saúde | GET /health |
| Disciplinas | POST/GET /subjects; GET/PUT/DELETE /subjects/{id} |
| Tarefas | POST/GET /tasks; GET/PUT/DELETE /tasks/{id}; PATCH /tasks/{id}/status |
| Eventos | POST/GET /events; GET/PUT/DELETE /events/{id} |
| Calendário agregado | GET /calendar?from=...&to=... |
| Sessões | POST/GET /sessions; GET/DELETE /sessions/{id} |
| Provas | POST/GET /exams; GET/PUT/DELETE /exams/{id} |
| Metas | POST/GET /goals; GET/PUT/DELETE /goals/{id}; GET /goals/progress |
| Revisões | POST/GET /reviews; GET/PUT/DELETE /reviews/{id}; POST /reviews/{id}/complete |
| Dashboard | GET /dashboard |
| Disponibilidade | GET/PUT /planning/availability |
| Sugestões | POST /planning/preview; POST /planning/accept |

POST de CRUD retorna 201 e Location. GET/PUT/PATCH retornam 200. DELETE retorna
204 sem corpo. Status: 400 entrada inválida, 404 referência/registro ausente,
409 vínculo ou conflito de reenvio/planejamento, 500 falha inesperada sanitizada.
GET /health usa 503 quando não consegue consultar o banco após a inicialização.
Reenvios idênticos de uma sessão retornam 201 com o mesmo ID, sem nova inserção.

Filtros de tarefas: subjectId, status, priority e dueBefore (inclusive).
Eventos: from/to (intervalo de sobreposição, limites exclusivos) e subjectId.
Sessões: from/to filtram endedAt, início inclusivo/fim exclusivo; subjectId.
Calendário exige from e to e projeta provas não canceladas, sem copiar registros.
Listagens são arrays; uma lista sem registros retorna [].

## Exemplos JSON

IDs abaixo são ilustrativos; substitua pelos IDs reais criados no seu banco.
PUT usa os mesmos campos de POST, com substituição dos campos editáveis.

Tarefa:

```json
{"title":"Estudar coleções Java","description":"List e Map","subjectId":1,"status":"PENDING","priority":"HIGH","dueDate":"2026-10-20","estimatedMinutes":50}
```

Alteração de status: `PATCH /tasks/1/status`, corpo `{"status":"COMPLETED"}`.
Reabrir: `{"status":"PENDING"}` ou `{"status":"IN_PROGRESS"}`.

Evento, incluindo vários dias se necessário:

```json
{"title":"Estudo de Java","description":null,"subjectId":1,"startAt":"2026-10-20T12:00:00Z","endAt":"2026-10-20T13:00:00Z","type":"STUDY"}
```

Sessão concluída (use horários passados no momento do envio):

```json
{"subjectId":1,"taskId":null,"startedAt":"2026-10-13T12:00:00Z","endedAt":"2026-10-13T12:25:00Z","notes":"Revisão de coleções","clientKey":"1be59d48-fb41-4d2f-9c10-57c821305fab"}
```

A duração é calculada no servidor; o cliente não envia durationMinutes.
Reenvie a mesma chave e os mesmos dados para tentar novamente sem duplicar.

Prova:

```json
{"title":"Avaliação de Java","subjectId":1,"description":null,"scheduledAt":"2026-10-25T12:00:00Z","location":"Sala 10","status":"UPCOMING","score":null,"maxScore":10}
```

Concluir/cancelar: PUT com status COMPLETED/CANCELLED e demais campos do registro.
Nota exige maxScore e não pode ultrapassá-lo.

Meta:

```json
{"type":"STUDY_MINUTES","targetValue":300,"periodStart":"2026-10-12","periodEnd":"2026-10-18","subjectId":1}
```

Outro tipo: COMPLETED_TASKS, targetValue conta tarefas. SubjectId pode ser null
para uma meta global. /goals/progress responde [{goal:{...},currentValue:...,percentage:...}].

Revisão:

```json
{"title":"Revisar coleções","subjectId":1,"notes":"Focar em mapas"}
```

POST /reviews/1/complete recebe corpo vazio/{}; datas e contador são recalculados.

Disponibilidade: PUT /planning/availability:

```json
{"slots":[{"dayOfWeek":1,"startTime":"18:00","endTime":"20:00"},{"dayOfWeek":3,"startTime":"18:00","endTime":"20:00"}]}
```

Sugestão: POST /planning/preview:

```json
{"weekStart":"2026-10-19","sessionMinutes":25,"breakMinutes":5}
```

Aceite: POST /planning/accept usa os mesmos parâmetros e acrescenta clientKey
(UUID novo para esse aceite) e fingerprint retornado pela sugestão. Não aceita
blocos arbitrários do cliente; recalcula os dados no servidor. Retorna os eventos
criados. Um replay do mesmo aceite retorna os eventos existentes.

## PowerShell / curl.exe

Exemplo de tarefa, depois de criar uma disciplina real:

```powershell
$subjectId=1 # Substitua pelo ID real.
$file=Join-Path $env:TEMP ('anjosflow-task-'+[guid]::NewGuid()+'.json')
$body=@{title='Estudar Java';subjectId=$subjectId;status='PENDING';priority='HIGH';estimatedMinutes=25}|ConvertTo-Json
[System.IO.File]::WriteAllText($file,$body,[System.Text.UTF8Encoding]::new($false))
$created=curl.exe -sS --fail-with-body -X POST http://localhost:8080/api/tasks -H 'Content-Type: application/json' --data-binary "@$file"|ConvertFrom-Json
curl.exe -i "http://localhost:8080/api/tasks?subjectId=$subjectId&status=PENDING"
curl.exe -i "http://localhost:8080/api/tasks/$($created.id)"
```

Os arquivos evitam problemas de aspas do Windows PowerShell com JSON. Para outros
recursos, substitua rota e corpo pelos exemplos acima. Exemplos completos de CRUD
de disciplinas estão em subjects.md. Não use IDs de registros que deseja preservar
em operações DELETE.

## Formato de erro

```json
{"type":"about:blank","title":"Dados inválidos","status":400,"detail":"Verifique os campos informados.","errors":{"title":"O título é obrigatório."}}
```

Conflitos e ausências usam Problem Detail com detail compreensível, sem stack trace,
SQL, valores rejeitados ou credenciais. Valores de enum inválidos retornam 400.