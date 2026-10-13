package com.anjosflow.study;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface StudySessionRepository
    extends JpaRepository<StudySession, Long>, JpaSpecificationExecutor<StudySession> {
  Optional<StudySession> findByClientKey(UUID clientKey);
}
