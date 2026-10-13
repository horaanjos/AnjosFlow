package com.anjosflow.task;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {}
