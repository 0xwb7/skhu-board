"use client";

import { useEffect, useMemo, useState } from "react";
import { NoticeCard } from "@/app/_components/notice-card";
import {
  CATEGORY_OPTIONS,
  fetchNotices,
  INTERESTS_KEY,
  Notice,
  NoticeSource,
  SOURCE_TABS,
} from "@/lib/api";

type SourceFilter = "ALL" | NoticeSource;

export function HomePageClient() {
  const [notices, setNotices] = useState<Notice[]>([]);
  const [selectedTab, setSelectedTab] = useState<SourceFilter>("ALL");
  const [selectedCategories, setSelectedCategories] = useState<string[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const saved = localStorage.getItem(INTERESTS_KEY);
    if (!saved) {
      return;
    }

    try {
      const parsed: unknown = JSON.parse(saved);
      if (Array.isArray(parsed) && parsed.every((item) => typeof item === "string")) {
        setSelectedCategories(parsed);
      }
    } catch {
      localStorage.removeItem(INTERESTS_KEY);
    }
  }, []);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError("");
        const data = await fetchNotices();
        setNotices(data);
      } catch {
        setError("공지사항을 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);

  const toggleCategory = (category: string) => {
    setSelectedCategories((previous) => {
      const next = previous.includes(category)
        ? previous.filter((item) => item !== category)
        : [...previous, category];

      localStorage.setItem(INTERESTS_KEY, JSON.stringify(next));
      return next;
    });
  };

  const clearCategories = () => {
    setSelectedCategories([]);
    localStorage.setItem(INTERESTS_KEY, JSON.stringify([]));
  };

  const filteredNotices = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();

    return notices.filter((notice) => {
      const matchesSource = selectedTab === "ALL" || notice.source === selectedTab;
      const matchesCategory =
        selectedCategories.length === 0 || selectedCategories.includes(notice.category);
      const matchesQuery =
        query.length === 0 ||
        notice.title.toLowerCase().includes(query) ||
        notice.summary.toLowerCase().includes(query);

      return matchesSource && matchesCategory && matchesQuery;
    });
  }, [notices, searchQuery, selectedCategories, selectedTab]);

  return (
    <main className="home-shell">
      <section className="hero-block">
        <div className="hero-badge">AI가 요약해주는 스마트한 공지사항</div>
        <h1 className="hero-title">
          성공회대 공지 <span>모아보기</span>
        </h1>
        <p className="hero-description">
          학교 공지와 LMS 소식을 한눈에 확인하고, AI 요약으로 핵심만 빠르게 파악하세요.
        </p>
      </section>

      <section className="toolbar-stack">
        <div className="search-tabs-panel glass-card">
          <div className="search-box">
            <span className="search-icon" aria-hidden="true">
              ⌕
            </span>
            <input
              type="text"
              placeholder="공지사항 제목이나 내용 검색..."
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
            />
          </div>

          <div className="tab-strip">
            {SOURCE_TABS.map((tab) => (
              <button
                key={tab.key}
                type="button"
                onClick={() => setSelectedTab(tab.key)}
                className={`tab-button ${selectedTab === tab.key ? "is-active" : ""}`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>
      </section>

      <div className="content-grid">
        <aside className="filter-sidebar">
          <section className="filter-card">
            <div className="filter-header">
              <div className="filter-title">카테고리 필터</div>
              <button
                type="button"
                onClick={clearCategories}
                disabled={selectedCategories.length === 0}
                className="reset-button"
                title="필터 초기화"
              >
                ↻
              </button>
            </div>

            <div className="filter-list">
              {CATEGORY_OPTIONS.map((category) => {
                const selected = selectedCategories.includes(category);
                return (
                  <button
                    key={category}
                    type="button"
                    className={`category-button ${selected ? "is-selected" : ""}`}
                    onClick={() => toggleCategory(category)}
                  >
                    <span>{category}</span>
                    {selected ? <span className="category-dot" aria-hidden="true" /> : null}
                  </button>
                );
              })}
            </div>
          </section>
        </aside>

        <section className="notice-panel">
          {loading ? (
            <div className="notice-skeleton-grid">
              {[1, 2, 3, 4].map((item) => (
                <div key={item} className="notice-skeleton" />
              ))}
            </div>
          ) : null}

          {!loading && error ? <div className="error-panel">{error}</div> : null}

          {!loading && !error && filteredNotices.length === 0 ? (
            <div className="empty-panel">
              <p>
                {notices.length === 0
                  ? "아직 등록된 공지사항이 없습니다."
                  : "조건에 맞는 공지사항이 없습니다."}
              </p>
            </div>
          ) : null}

          {!loading && !error && filteredNotices.length > 0 ? (
            <div className="notice-grid">
              {filteredNotices.map((notice) => (
                <NoticeCard key={notice.id} notice={notice} />
              ))}
            </div>
          ) : null}
        </section>
      </div>
    </main>
  );
}
