package com.anjosflow.review;

import com.anjosflow.common.error.ApiException;
import com.anjosflow.common.model.References;
import com.anjosflow.common.time.AppTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewService {
  private final ReviewRepository repository;
  private final References references;
  private final AppTime time;

  public ReviewService(ReviewRepository repository, References references, AppTime time) {
    this.repository = repository;
    this.references = references;
    this.time = time;
  }

  public static int intervalAfter(int count) {
    return switch (count) {
      case 0 -> 1;
      case 1 -> 3;
      case 2 -> 7;
      case 3 -> 14;
      default -> 30;
    };
  }

  @Transactional
  public ReviewResponse create(ReviewRequest r) {
    var e = new Review();
    apply(e, r);
    e.setReviewCount(0);
    e.setIntervalDays(1);
    e.setNextReviewAt(time.today().plusDays(1));
    return ReviewResponse.from(repository.saveAndFlush(e));
  }

  public List<ReviewResponse> list() {
    return repository.findAll(Sort.by("nextReviewAt", "id")).stream()
        .map(ReviewResponse::from)
        .toList();
  }

  public ReviewResponse find(Long id) {
    return ReviewResponse.from(entity(id));
  }

  @Transactional
  public ReviewResponse update(Long id, ReviewRequest r) {
    var e = entity(id);
    apply(e, r);
    return ReviewResponse.from(repository.saveAndFlush(e));
  }

  @Transactional
  public ReviewResponse complete(Long id) {
    var e = entity(id);
    // Cliques repetidos no mesmo dia não avançam novamente o intervalo.
    if (e.getLastReviewedAt() != null
        && e.getLastReviewedAt().atZone(AppTime.ZONE).toLocalDate().equals(time.today()))
      return ReviewResponse.from(e);
    e.setReviewCount(e.getReviewCount() + 1);
    e.setIntervalDays(intervalAfter(e.getReviewCount()));
    e.setLastReviewedAt(time.now());
    e.setNextReviewAt(time.today().plusDays(e.getIntervalDays()));
    return ReviewResponse.from(repository.saveAndFlush(e));
  }

  @Transactional
  public void delete(Long id) {
    repository.delete(entity(id));
  }

  private Review entity(Long id) {
    return repository.findById(id).orElseThrow(() -> ApiException.missing("Revisão"));
  }

  private void apply(Review e, ReviewRequest r) {
    references.subject(r.subjectId());
    e.setSubjectId(r.subjectId());
    e.setTitle(r.title());
    e.setNotes(r.notes());
  }
}
