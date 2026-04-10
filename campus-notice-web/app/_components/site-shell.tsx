import Image from "next/image";
import Link from "next/link";

export function SiteShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="site-root">
      <header className="site-header">
        <div className="site-header-inner">
          <Link href="/" className="brand-link">
            <Image
              src="/skhu-logo.svg"
              alt="성공회대학교 로고"
              className="brand-mark"
              width={40}
              height={40}
              priority
            />
            <span className="brand-title">
              SKHU <span>Board</span>
            </span>
          </Link>

          <nav className="site-nav">
            <Link href="/">홈</Link>
            <a href="https://skhu.ac.kr" target="_blank" rel="noreferrer">
              학교 홈페이지
            </a>
            <a href="https://lms.skhu.ac.kr" target="_blank" rel="noreferrer">
              LMS
            </a>
          </nav>
        </div>
      </header>

      {children}

      <footer className="site-footer">
        <div className="site-footer-inner">
          <div className="footer-logo">
            <Image
              src="/skhu-logo.svg"
              alt="성공회대학교 로고"
              className="brand-mark"
              width={36}
              height={36}
            />
            <span className="brand-title">
              SKHU <span>Board</span>
            </span>
          </div>

          <p className="footer-description">
            성공회대학교 학생들을 위한 스마트한 공지사항 요약 서비스입니다. 학교 공지와
            LMS 소식을 한곳에 모아 더 빠르게 읽을 수 있도록 구성했습니다.
          </p>

          <div className="footer-copyright">
            © 2026 SKHU Notice AI. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
}
