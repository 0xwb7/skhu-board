"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import {
  fetchNoticeDetail,
  getSourceLabel,
  isDirectLinkSource,
  NoticeDetail,
} from "@/lib/api";

export function NoticeDetailClient({ id }: { id: string }) {
  const [notice, setNotice] = useState<NoticeDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      try {
        setLoading(true);
        setError("");
        const data = await fetchNoticeDetail(id);

        if (!cancelled) {
          setNotice(data);
        }
      } catch {
        if (!cancelled) {
          setError("공지 상세를 불러오지 못했습니다.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    load();

    return () => {
      cancelled = true;
    };
  }, [id]);

  if (loading) {
    return (
      <main className="detail-shell">
        <div className="notice-skeleton-grid">
          <div className="notice-skeleton" />
          <div className="notice-skeleton" />
        </div>
      </main>
    );
  }

  if (error || !notice) {
    return (
      <main className="detail-shell">
        <div className="error-panel">{error || "공지 상세를 찾을 수 없습니다."}</div>
        <Link href="/" className="detail-back-link">
          ← 목록으로 돌아가기
        </Link>
      </main>
    );
  }

  const canOpenOriginalDirectly = isDirectLinkSource(notice.source);

  return (
    <main className="detail-shell">
      <Link href="/" className="detail-back-link">
        ← 목록으로 돌아가기
      </Link>

      <header className="detail-top">
        <div className="detail-meta-badges">
          <span className="badge badge-source">{getSourceLabel(notice.source)}</span>
          <span className="badge badge-category">{notice.category}</span>
        </div>
        <h1>{notice.title}</h1>
        <div className="detail-date">{notice.date}</div>
      </header>

      <div className="detail-section-stack">
        <section className="detail-highlight-card">
          <div className="detail-section-head">
            <span aria-hidden="true">✦</span>
            <h2>AI 3줄 요약</h2>
          </div>
          <p>{notice.summary}</p>
        </section>

        <section className="detail-card">
          <div className="detail-section-head detail-section-head-brand">
            <span aria-hidden="true">☰</span>
            <h2>핵심 정리</h2>
          </div>
          <p>{notice.organizedContent}</p>
        </section>

        <section className="detail-card detail-card-muted">
          <div className="detail-section-head detail-section-head-muted">
            <span aria-hidden="true">▤</span>
            <h2>원문 내용</h2>
          </div>
          <p className="detail-content">{notice.content}</p>
        </section>

        {canOpenOriginalDirectly && notice.url ? (
          <div className="detail-link-row">
            <a
              href={notice.url}
              target="_blank"
              rel="noreferrer"
              className="detail-primary-link"
            >
              원문 페이지에서 확인하기 ↗
            </a>
          </div>
        ) : (
          <div className="detail-warning">
            이 공지는 LMS 내부 공지라서 직접 링크로는 열리지 않을 수 있습니다. LMS에
            로그인한 뒤 해당 어울림 강의실 공지사항에서 확인해주세요.
          </div>
        )}
      </div>
    </main>
  );
}
