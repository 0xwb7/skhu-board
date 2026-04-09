import Link from "next/link";
import { Notice, NoticeSource, getSourceLabel } from "@/lib/api";

type NoticeCardProps = {
  notice: Notice;
};

const shortSourceLabels: Record<NoticeSource, string> = {
  SCHOOL: "학교공지",
  LMS_ALL: "LMS 전체",
  LMS_IT: "IT융합",
  LMS_SOFTWARE: "소프트웨어",
};

export function NoticeCard({ notice }: NoticeCardProps) {
  return (
    <article className="notice-card">
      <div className="card-meta">
        <span className="badge badge-source">
          {shortSourceLabels[notice.source] ?? getSourceLabel(notice.source)}
        </span>
        <span className="badge badge-category">{notice.category}</span>
      </div>

      <Link href={`/notices/${notice.id}`} className="notice-link">
        <h3>{notice.title}</h3>
      </Link>

      <div className="summary-panel-card">
        <div className="summary-badge" aria-hidden="true">
          ✦
        </div>
        <p className="notice-summary">
          {notice.summary || "AI 요약을 생성 중입니다..."}
        </p>
      </div>

      <div className="card-footer">
        <div className="card-date">{notice.date}</div>
        <Link href={`/notices/${notice.id}`} className="card-action">
          자세히 보기 →
        </Link>
      </div>
    </article>
  );
}
