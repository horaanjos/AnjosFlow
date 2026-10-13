package com.anjosflow.review;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface ReviewRepository
    extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {}
