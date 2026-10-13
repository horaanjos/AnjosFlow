package com.anjosflow.goal;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface StudyGoalRepository
    extends JpaRepository<StudyGoal, Long>, JpaSpecificationExecutor<StudyGoal> {}
