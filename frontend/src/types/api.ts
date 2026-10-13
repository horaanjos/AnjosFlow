export interface Entity {
  id: number;
  createdAt: string;
  updatedAt: string;
}
export interface Subject extends Entity {
  name: string;
  description: string | null;
  color: string;
}
export type TaskStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";
export type Priority = "LOW" | "MEDIUM" | "HIGH";
export interface Task extends Entity {
  title: string;
  description: string | null;
  subjectId: number;
  status: TaskStatus;
  priority: Priority;
  dueDate: string | null;
  estimatedMinutes: number | null;
  completedAt: string | null;
}
export interface CalendarEvent extends Entity {
  title: string;
  description: string | null;
  subjectId: number | null;
  startAt: string;
  endAt: string;
  type: "STUDY" | "EXAM" | "PERSONAL";
}
export interface CalendarItem {
  id: string;
  title: string;
  description: string | null;
  subjectId: number | null;
  startAt: string;
  endAt: string;
  type: string;
  source: "EVENT" | "EXAM";
  sourceId: number;
}
export interface Session extends Entity {
  subjectId: number;
  taskId: number | null;
  startedAt: string;
  endedAt: string;
  durationMinutes: number;
  notes: string | null;
  clientKey: string;
}
export interface Exam extends Entity {
  title: string;
  description: string | null;
  subjectId: number | null;
  scheduledAt: string;
  location: string | null;
  status: "UPCOMING" | "COMPLETED" | "CANCELLED";
  score: number | null;
  maxScore: number | null;
}
export interface Goal extends Entity {
  type: "STUDY_MINUTES" | "COMPLETED_TASKS";
  targetValue: number;
  periodStart: string;
  periodEnd: string;
  subjectId: number | null;
}
export interface GoalProgress {
  goal: Goal;
  currentValue: number;
  percentage: number;
}
export interface Review extends Entity {
  subjectId: number;
  title: string;
  notes: string | null;
  lastReviewedAt: string | null;
  nextReviewAt: string;
  intervalDays: number;
  reviewCount: number;
}
export interface Dashboard {
  timeZone: string;
  today: string;
  weekStart: string;
  totalSubjects: number;
  pendingTasks: number;
  completedTasks: number;
  overdueTasks: number;
  minutesToday: number;
  weekMinutes: number;
  dailyStudy: { day: string; minutes: number }[];
  distribution: {
    subjectId: number;
    name: string;
    color: string;
    minutes: number;
  }[];
  upcomingExams: Exam[];
  nextReviews: Review[];
  goals: GoalProgress[];
}
export interface Availability {
  dayOfWeek: number;
  startTime: string;
  endTime: string;
}
export interface PlanBlock {
  taskId: number;
  subjectId: number;
  title: string;
  startAt: string;
  endAt: string;
}
export interface Proposal {
  fingerprint: string;
  blocks: PlanBlock[];
  unscheduled: { taskId: number; title: string; remainingMinutes: number }[];
}
