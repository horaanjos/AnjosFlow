# Regras do MVP

## Horários e períodos

Horários são Instant/UTC em Java, TIMESTAMPTZ em PostgreSQL e ISO 8601 na API.
Formulários e calendário usam America/Sao_Paulo, independentemente do fuso do
navegador. Datas de prazo e revisão são LocalDate/DATE, sem conversão para UTC.
Auditoria é gerenciada por callbacks JPA, com precisão de microssegundos.

Hoje: [00:00 do dia em São Paulo, 00:00 do dia seguinte). Semana: segunda-feira
00:00 até a próxima segunda-feira 00:00 (fim exclusivo). Últimos 7 dias: hoje e
os seis dias anteriores. Tempo estudado é atribuído à data de endedAt; sessões
que atravessam meia-noite não são divididas. A duração é truncada para minutos
inteiros, entre 1 e 10080. Sessões futuras são rejeitadas, com tolerância de 5 segundos.

## Relacionamentos e exclusões

Referências de disciplina devem existir. Sessão exige disciplina; tarefa opcional
precisa pertencer à mesma disciplina. FK composta impede mudar a disciplina de
uma tarefa que já possui sessões relacionadas. Exclusões têm RESTRICT, sem cascatas.
Para apagar uma disciplina, remova explicitamente registros relacionados; conflitos
retornam 409. A interface pede confirmação e não remove vínculos automaticamente.

## Tarefas, provas e calendário

Tarefa exige título, disciplina, status e prioridade. Estimativa é opcional e
positiva quando informada. Ao concluir, completedAt é preenchido uma única vez;
ao reabrir, é limpo. Filtros combinam condições com AND.

Provas têm status UPCOMING, COMPLETED ou CANCELLED. Nota é não negativa, máximo
positivo, e score<=maxScore. Calendário projeta cada prova não cancelada a partir
do registro original, com ID exam-N e duração visual de 1 hora. Editar a prova
atualiza sua representação sem sincronização duplicada de tabela.
Eventos manuais podem se sobrepor; o planejador respeita eventos e provas próximas.

## Sessões e Pomodoro

Sessões são registros concluídos e imutáveis. É possível excluir e registrar outro
para corrigir um erro. Chave clientKey UUID é única. O servidor serializa requisições
concorrentes da mesma chave com advisory lock transacional. Replay idêntico retorna
o mesmo registro; a mesma chave com outros dados retorna 409.

Pomodoro: foco 25 min, descanso curto 5 min e longo 15 min, configuráveis. Uma pausa
longa é sugerida a cada 4 focos concluídos. Deadline absoluto (Date.now) determina o
tempo restante; intervalos só atualizam a tela. Pausas congelam o tempo restante,
retomada calcula novo deadline. Estado e chave do ciclo ficam em localStorage.
Voltar à página ou recarregar recupera o ciclo, inclusive um foco concluído pendente.

O usuário decide registrar o foco concluído. Descansos nunca viram sessões. O
registro utiliza a mesma clientKey do ciclo em toda tentativa. No caso de pausas,
a janela registrada termina no deadline de conclusão e começa duração de foco
minutos antes: representa tempo efetivo de foco, sem incluir o tempo pausado.
O MVP não guarda todos os segmentos intermediários de pausa. Não garante alertas
com o navegador fechado e não substitui um processo em segundo plano do sistema.

## Metas e dashboard

Metas cobrem exatamente 7 datas inclusivas; targetValue positivo. O backend calcula
STUDY_MINUTES pela soma de duração das sessões concluídas no período, e
COMPLETED_TASKS pelas tarefas ainda concluídas com completedAt no período.
Reabrir uma tarefa remove sua contribuição. SubjectId opcional restringe a meta.
Percentual é currentValue/targetValue, truncado e limitado a 100%; valor real não
é truncado ao objetivo. Progresso não é campo editável do cliente.

Dashboard usa agregações no PostgreSQL: total de disciplinas, tarefas, atrasos,
minutos no dia/semana, série diária e distribuição por disciplina. Não transfere
o histórico completo ao Front-end para contar estatísticas. Uma consulta agregada
calcula valores de todas as metas. Listas de provas/revisões no resumo são limitadas
a 5 itens. Históricos sem registros produzem zeros/estados vazios reais.

## Revisões

Uma nova revisão tem contador 0 e próxima data amanhã (intervalo 1).
Cada conclusão aumenta o contador e escolhe intervalo 3, 7, 14 ou 30 dias.
Após alcançar 30 dias, mantém intervalos de 30 dias. A próxima data é calculada
na data efetiva da revisão em São Paulo, permitindo recuperar revisões atrasadas.
Cliques repetidos no mesmo dia não avançam novamente o contador. A tela oferece
filtros para hoje/atrasadas e registros já revisados. Mantém lastReviewedAt e
reviewCount, sem histórico separado de cada ocorrência nesta versão.