# Planejador determinístico

## Entradas persistidas

Disponibilidade recorrente: dia ISO 1=segunda até 7=domingo, início/fim no mesmo dia,
fuso America/Sao_Paulo. Até 28 janelas; janelas do mesmo dia não podem se sobrepor.
O PUT substitui a configuração somente quando o usuário salva na interface.
Tarefas pendentes/em andamento, disciplina, prioridade, prazo, estimativa e provas
próximas são lidos do banco. A semana escolhida deve começar em uma segunda-feira,
da semana atual ou futura.

## Algoritmo

1. Materializa as janelas para as datas da semana em São Paulo e converte para UTC.
2. Exclui horários já passados na semana atual.
3. Subtrai eventos que intersectam cada janela e provas UPCOMING (bloqueio de 1h).
4. Ordena tarefas por menor prazo entre entrega e próxima prova da disciplina;
   sem prazo vem depois. Empates: prioridade HIGH/MEDIUM/LOW, depois ID crescente.
5. Usa estimatedMinutes, ou sessionMinutes se a tarefa não possui estimativa.
6. Desconta minutos dos blocos dessa tarefa já aceitos na mesma semana.
7. Distribui os minutos em intervalos livres, com limite sessionMinutes por bloco,
   reduzindo o último bloco conforme espaço/restante. Respeita breakMinutes entre
   blocos, mesmo quando há um pequeno compromisso entre dois intervalos livres.
8. Retorna blocos e minutos restantes de cada tarefa que não coube.

Não garante concluir um trabalho antes de seu prazo: prioriza esse prazo e informa
falta de espaço. Não inventa atividades para disciplinas sem tarefas. Resultado
reproduzível para os mesmos registros, parâmetros e referência de tempo.

## Sugestão e aceite

Preview não grava eventos. O resultado recebe fingerprint SHA256 do conteúdo.
Aceite recebe semana/durações, fingerprint e clientKey UUID. O servidor recalcula
a sugestão; se o conteúdo mudou, retorna 409 para nova revisão pelo usuário.
Um lock transacional serializa aceites. Locks nas tabelas de eventos, provas,
tarefas e disponibilidade protegem a checagem e inserção contra alterações
concorrentes durante o aceite. A transação salva o lote e seus eventos STUDY.
Nenhum evento existente é sobrescrito. Replay de clientKey retorna os mesmos
blocos, sem duplicação. A mesma chave com outro fingerprint retorna 409.

Descartar significa apenas remover a sugestão da interface. Uma sugestão aceita
pode ser ajustada/excluída pelo CRUD normal de eventos. Mudar a disciplina de um
bloco desvincula sua tarefa original; o lote de aceite permanece como metadado.

## Testes

PlannerEngineTest cobre precedência, determinismo, conflitos, intervalos, ausência
de disponibilidade, evento cobrindo janela e minutos não alocados. PlanningIT
valida preview sem escrita, aceite persistido, replay, mudanças entre preview e
aceite e janelas inválidas. Toda a transação de PlanningIT é revertida: dados e
configuração do usuário não são alterados ao final do teste.