import type { Metadata } from "next";
import { SiteShell } from "@/app/_components/site-shell";
import "./globals.css";

export const metadata: Metadata = {
  title: "Campus Notice Radar",
  description: "성공회대 학교 공지와 LMS 공지를 한 번에 모아보는 서비스",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" className="h-full antialiased">
      <body className="min-h-full">
        <SiteShell>{children}</SiteShell>
      </body>
    </html>
  );
}
