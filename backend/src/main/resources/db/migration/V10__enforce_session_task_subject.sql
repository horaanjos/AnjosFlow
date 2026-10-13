-- Impede que uma mudança posterior de disciplina da tarefa invalide seu histórico.
ALTER TABLE tasks ADD CONSTRAINT tasks_id_subject_unique UNIQUE(id,subject_id);
ALTER TABLE study_sessions ADD CONSTRAINT sessions_task_subject_fk
 FOREIGN KEY(task_id,subject_id) REFERENCES tasks(id,subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT;