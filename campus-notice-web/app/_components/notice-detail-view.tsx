import { NoticeDetailClient } from "@/app/_components/notice-detail-client";

type NoticeDetailViewProps = {
  id: string;
};

export async function NoticeDetailView({ id }: NoticeDetailViewProps) {
  return <NoticeDetailClient id={id} />;
}
