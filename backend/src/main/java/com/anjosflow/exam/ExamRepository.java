package com.anjosflow.exam;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface ExamRepository extends JpaRepository<Exam, Long>, JpaSpecificationExecutor<Exam> {}
