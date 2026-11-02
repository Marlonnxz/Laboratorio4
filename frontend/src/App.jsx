import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import DiagnosticsBar from './components/DiagnosticsBar';
import LoginModal from './components/LoginModal';
import StudentsView from './components/StudentsView';
import SubjectsView from './components/SubjectsView';
import CacheBenchmarkModal from './components/CacheBenchmarkModal';
import { getStoredUser, clearAuth, apiRequest, storeAuth, getGatewayUrl } from './api/client';
import {
  Users,
  BookOpen,
  Cloud,
  CheckCircle2,
  Database,
  HardDrive,
  Cpu,
  ShieldCheck,
  Zap,
  ExternalLink
} from 'lucide-react';

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [showBenchmarkModal, setShowBenchmarkModal] = useState(false);
  const [activeTab, setActiveTab] = useState('students'); // 'students' | 'subjects' | 'architecture'
  const [healthStatus, setHealthStatus] = useState(null);

  useEffect(() => {
    const user = getStoredUser();
    if (user) {
      setCurrentUser(user);
    }
    checkGatewayHealth();
  }, []);

  const checkGatewayHealth = async () => {
    try {
      const res = await apiRequest('/actuator/health');
      setHealthStatus(res.data?.status || 'UP');
    } catch {
      setHealthStatus('OFFLINE');
    }
  };

  const handleLogout = () => {
    clearAuth();
    setCurrentUser(null);
  };

  const handleQuickLogin = async (username, password) => {
    try {
      const res = await apiRequest('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });
      const data = res.data;
      storeAuth(data.token, {
        username: data.username,
        fullName: data.fullName,
        role: data.role,
        studentId: data.studentId,
      });
      setCurrentUser(data);
    } catch (err) {
      alert(`Error en inicio rápido: ${err.message}`);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      
      {/* Top Navigation */}
      <Navbar
        currentUser={currentUser}
        onLoginClick={() => setShowLoginModal(true)}
        onLogout={handleLogout}
        onQuickLogin={handleQuickLogin}
        onOpenBenchmark={() => setShowBenchmarkModal(true)}
      />

      {/* Real-time Diagnostics Bar (X-Cache, Status, Latency) */}
      <DiagnosticsBar />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
        
        {/* Navigation Tabs */}
        <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800 pb-3">
          <div className="flex space-x-1 sm:space-x-2 bg-slate-900/80 p-1 rounded-xl border border-slate-800">
            <button
              onClick={() => setActiveTab('students')}
              className={`flex items-center space-x-2 px-3.5 py-2 rounded-lg text-xs font-semibold transition ${
                activeTab === 'students'
                  ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-600/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <Users className="h-4 w-4" />
              <span>Estudiantes & Fotos</span>
            </button>

            <button
              onClick={() => setActiveTab('subjects')}
              className={`flex items-center space-x-2 px-3.5 py-2 rounded-lg text-xs font-semibold transition ${
                activeTab === 'subjects'
                  ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <BookOpen className="h-4 w-4" />
              <span>Materias & Redis Caché</span>
            </button>

            <button
              onClick={() => setActiveTab('architecture')}
              className={`flex items-center space-x-2 px-3.5 py-2 rounded-lg text-xs font-semibold transition ${
                activeTab === 'architecture'
                  ? 'bg-teal-600 text-white shadow-lg shadow-teal-600/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <Cloud className="h-4 w-4" />
              <span>Topología Nube (Azure)</span>
            </button>
          </div>

          {/* Quick status pill */}
          <div className="flex items-center space-x-2 text-xs text-slate-400">
            <span>Gateway Actuator:</span>
            <span
              className={`px-2 py-0.5 rounded-full font-mono font-bold text-[10px] border ${
                healthStatus === 'UP'
                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                  : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
              }`}
            >
              ● {healthStatus || 'CHECKING...'}
            </span>
          </div>
        </div>

        {/* Tab Views */}
        {activeTab === 'students' && (
          <StudentsView currentUser={currentUser} />
        )}

        {activeTab === 'subjects' && (
          <SubjectsView currentUser={currentUser} />
        )}

        {activeTab === 'architecture' && (
          <div className="space-y-6">
            <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
              <h3 className="text-lg font-bold text-white flex items-center space-x-2">
                <Cloud className="h-5 w-5 text-teal-400" />
                <span>Despliegue Distribuido en Microsoft Azure (Laboratorio 4)</span>
              </h3>
              <p className="text-xs text-slate-400 leading-relaxed max-w-3xl">
                El sistema se ejecuta en un ambiente unificado en <strong>Azure Container Apps (ACA)</strong> dentro de una red virtual privada. Solo el <strong>API Gateway</strong> y el <strong>Frontend</strong> disponen de ingreso público con terminación HTTPS gestionada por Azure. Los cuatro microservicios de backend son completamente privados e inaccesibles desde internet.
              </p>

              {/* Service Cards Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 pt-3">
                
                <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-100 flex items-center space-x-1.5">
                      <Cpu className="h-4 w-4 text-emerald-400" />
                      <span>API Gateway</span>
                    </span>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                      PÚBLICO (HTTPS)
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-400">
                    Spring Cloud Gateway (WebFlux reactivo). Valida JWT, orquesta /detail, administra caché Redis y enruta peticiones.
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-100 flex items-center space-x-1.5">
                      <ShieldCheck className="h-4 w-4 text-purple-400" />
                      <span>auth-service</span>
                    </span>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                      PRIVADO (INTERNAL)
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-400">
                    Microservicio independiente. Base <code>auth_db</code>, hash BCrypt, firma JWT con roles (ADMIN / ESTUDIANTE).
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-100 flex items-center space-x-1.5">
                      <HardDrive className="h-4 w-4 text-teal-400" />
                      <span>Azure Blob Storage</span>
                    </span>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                      BUCKET PRIVADO
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-400">
                    Almacenamiento de fotos (JPG/PNG $\le$ 2MB) y PDFs temáticos ($\le$ 10MB). Acceso seguro mediante URLs firmadas (SAS Tokens).
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-100 flex items-center space-x-1.5">
                      <Zap className="h-4 w-4 text-indigo-400" />
                      <span>Azure Cache for Redis</span>
                    </span>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                      PRIVADO (SOLO GATEWAY)
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-400">
                    Redis administrado (Basic C0). Patrón Cache-Aside sobre catálogo de materias, cabecera X-Cache y degradación elegante ante caídas.
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-100 flex items-center space-x-1.5">
                      <Database className="h-4 w-4 text-blue-400" />
                      <span>PostgreSQL Flexible Server</span>
                    </span>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                      4 BASES INDEPENDIENTES
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-400">
                    Instancia administrada B1ms con respaldos activos. Aloja <code>students_db</code>, <code>subjects_db</code>, <code>enrollments_db</code> y <code>auth_db</code>.
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-100 flex items-center space-x-1.5">
                      <Users className="h-4 w-4 text-amber-400" />
                      <span>Módulos de Dominio</span>
                    </span>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                      PRIVADOS (INTERNAL)
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-400">
                    <code>students-service</code>, <code>subjects-service</code> y <code>enrollments-service</code> con ciclo de vida e imágenes independientes.
                  </p>
                </div>

              </div>
            </div>
          </div>
        )}

      </main>

      {/* Login Modal */}
      <LoginModal
        isOpen={showLoginModal}
        onClose={() => setShowLoginModal(false)}
        onLoginSuccess={(userData) => setCurrentUser(userData)}
      />

      {/* Cache Benchmark Modal */}
      <CacheBenchmarkModal
        isOpen={showBenchmarkModal}
        onClose={() => setShowBenchmarkModal(false)}
      />

    </div>
  );
}
