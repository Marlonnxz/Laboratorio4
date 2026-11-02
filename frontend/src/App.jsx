import React, { useState, useEffect } from 'react';
import LoginScreen from './components/LoginScreen';
import StudentPortal from './components/StudentPortal';
import AdminPortal from './components/AdminPortal';
import DiagnosticsBar from './components/DiagnosticsBar';
import { getStoredUser, clearAuth } from './api/client';

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    const user = getStoredUser();
    if (user) {
      setCurrentUser(user);
    }
  }, []);

  const handleLogout = () => {
    clearAuth();
    setCurrentUser(null);
  };

  // If user is not authenticated: Show ONLY the dedicated clean Login Screen
  if (!currentUser) {
    return <LoginScreen onLoginSuccess={(userData) => setCurrentUser(userData)} />;
  }

  // Once authenticated: Render role-tailored portal with real-time X-Cache telemetry bar
  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      
      {/* Real-time telemetry bar for the evaluator: shows X-Cache HIT/MISS and duration ms */}
      <DiagnosticsBar />

      {/* Role-based Portal Rendering */}
      {currentUser.role === 'ADMIN' ? (
        <AdminPortal currentUser={currentUser} onLogout={handleLogout} />
      ) : (
        <StudentPortal currentUser={currentUser} onLogout={handleLogout} />
      )}

    </div>
  );
}
