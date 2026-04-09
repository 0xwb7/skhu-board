const defaultApiBaseUrl = "http://localhost:8080";

export const API_BASE_URL = (process.env.NEXT_PUBLIC_API_BASE_URL || defaultApiBaseUrl).replace(/\/$/, "");

export const INTERESTS_KEY = "selected_categories";

export const SOURCE_LABELS = {
  SCHOOL: "학교공지",
  LMS_ALL: "LMS 전체공지",
  LMS_IT: "IT융합자율학부",
  LMS_SOFTWARE: "소프트웨어공학전공",
} as const;

export const SOURCE_TABS: { key: "ALL" | NoticeSource; label: string }[] = [
  { key: "ALL", label: "전체" },
  { key: "SCHOOL", label: SOURCE_LABELS.SCHOOL },
  { key: "LMS_ALL", label: SOURCE_LABELS.LMS_ALL },
  { key: "LMS_IT", label: SOURCE_LABELS.LMS_IT },
  { key: "LMS_SOFTWARE", label: SOURCE_LABELS.LMS_SOFTWARE },
];

export const CATEGORY_OPTIONS = [
  "장학",
  "모집",
  "대회",
  "인턴십",
  "특강",
  "행사",
  "학사",
  "수업",
  "취업",
  "비교과",
  "기타",
] as const;

export type NoticeSource =
  | "SCHOOL"
  | "LMS_ALL"
  | "LMS_IT"
  | "LMS_SOFTWARE";

export type Notice = {
  id: string;
  title: string;
  summary: string;
  category: string;
  date: string;
  url?: string;
  source: NoticeSource;
};

export type NoticeDetail = {
  id: string;
  title: string;
  summary: string;
  organizedContent: string;
  aiGenerated: boolean;
  category: string;
  date: string;
  content: string;
  url: string;
  source: NoticeSource;
};

export function getSourceLabel(source: NoticeSource) {
  return SOURCE_LABELS[source] ?? source;
}

export function isDirectLinkSource(source: NoticeSource) {
  return source === "SCHOOL" || source === "LMS_ALL";
}

export function normalizeCategory(category: string) {
  switch (category) {
    case "경진대회":
      return "대회";
    case "설명회":
      return "행사";
    case "교환학생":
    case "봉사":
      return "비교과";
    case "인공지능전공":
    case "IT융합자율학부":
    case "소프트웨어공학전공":
      return "기타";
    default:
      return category || "기타";
  }
}

export async function fetchNotices(): Promise<Notice[]> {
  const response = await fetch(`${API_BASE_URL}/api/notices`, {
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error("공지 목록 조회 실패");
  }

  const data: unknown = await response.json();

  if (!Array.isArray(data)) {
    throw new Error("응답 형식이 올바르지 않습니다.");
  }

  return data.filter(isNotice).map((notice) => ({
    ...notice,
    category: normalizeCategory(notice.category),
  }));
}

export async function fetchNoticeDetail(id: string): Promise<NoticeDetail> {
  const response = await fetch(`${API_BASE_URL}/api/notices/${id}`, {
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error("공지 상세 조회 실패");
  }

  const data: unknown = await response.json();

  if (!isNoticeDetailPayload(data)) {
    throw new Error("상세 응답 형식이 올바르지 않습니다.");
  }

  return {
    id: data.id,
    title: data.title,
    summary: data.summary,
    organizedContent: data.organizedContent,
    aiGenerated: typeof data.aiGenerated === "boolean" ? data.aiGenerated : false,
    category: normalizeCategory(data.category),
    date: data.date,
    content: data.content,
    url: data.url,
    source: data.source,
  };
}

function isNotice(item: unknown): item is Notice {
  if (typeof item !== "object" || item === null) {
    return false;
  }

  const notice = item as Record<string, unknown>;

  return (
    typeof notice.id === "string" &&
    typeof notice.title === "string" &&
    typeof notice.summary === "string" &&
    typeof notice.category === "string" &&
    typeof notice.date === "string" &&
    typeof notice.source === "string"
  );
}

type NoticeDetailPayload = Omit<NoticeDetail, "aiGenerated"> & {
  aiGenerated?: boolean;
};

function isNoticeDetailPayload(item: unknown): item is NoticeDetailPayload {
  if (typeof item !== "object" || item === null) {
    return false;
  }

  const detail = item as Record<string, unknown>;

  return (
    typeof detail.id === "string" &&
    typeof detail.title === "string" &&
    typeof detail.summary === "string" &&
    typeof detail.organizedContent === "string" &&
    typeof detail.category === "string" &&
    typeof detail.date === "string" &&
    typeof detail.content === "string" &&
    typeof detail.url === "string" &&
    typeof detail.source === "string"
  );
}
