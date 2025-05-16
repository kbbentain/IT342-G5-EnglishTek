interface DashboardLayoutProps {
  children: React.ReactNode;
}

import Sidebar from './Sidebar';

const DashboardLayout = ({ children }: DashboardLayoutProps) => {
  return (
    <div className="flex h-screen bg-base-200">
      <Sidebar />
      <main className="flex-1 overflow-auto p-8">
        {children}
      </main>
    </div>
  );
};

export default DashboardLayout;
