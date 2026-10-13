package com.anjosflow.dashboard;

import com.anjosflow.exam.ExamResponse;
import com.anjosflow.goal.GoalProgress;
import com.anjosflow.review.ReviewResponse;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
    String timeZone,
    LocalDate today,
    LocalDate weekStart,
    long totalSubjects,
    long pendingTasks,
    long completedTasks,
    long overdueTasks,
    long minutesToday,
    long weekMinutes,
    List<DailyStudy> dailyStudy,
    List<SubjectStudy> distribution,
    List<ExamResponse> upcomingExams,
    List<ReviewResponse> nextReviews,
    List<GoalProgress> goals) {
  public record DailyStudy(LocalDate day, long minutes) {}

  public record SubjectStudy(Long subjectId, String name, String color, long minutes) {}
}
