import { NoticeDetailView } from "@/app/_components/notice-detail-view";

type Props = {
  params: Promise<{ id: string }>;
};

export default async function NoticeDetailPage({ params }: Props) {
  const { id } = await params;
  return <NoticeDetailView id={id} />;
}
