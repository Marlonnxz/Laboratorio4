import React, { useState } from 'react';
import {
  Shield,
  User,
  GraduationCap,
  LogOut,
  Zap,
  Settings,
  Server,
  Layers,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';
import { getGatewayUrl, setGatewayUrl } from '../api/client';

export default function Navbar({ currentUser, onLoginClick, onLogout, onQuickLogin, onOpenBenchmark }) {
  const [showConfig, setShowConfig] = useState(false);
  const [gatewayInput, setGatewayInput] = useState(getGatewayUrl());
  const [savedSuccess, setSavedSuccess] = useState(false);

  const handleSaveGateway = (e) => {
    e.preventDefault();
    setGatewayUrl(gatewayInput);
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 2000);
  };

  return (
    <header className="sticky top-0 z-40 border-b border-slate-800 bg-slate-950/80 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Logo & Lab Info */}
          <div className="flex items-center space-x-3">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 flex items-center justify-center shadow-lg shadow-emerald-500/20 text-white font-bold text-lg">
              <Layers className="h-5 w-5" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
                  SGA Distribuido
                </span>
                <span className="text-[10px] font-semibold tracking-wider uppercase px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  Azure Cloud
                </span>
              </div>
              <p className="text-xs text-slate-400">UPTC • Lab 4: Microservicios, Caché Redis & Blob Storage</p>
            </div>
          </div>

          {/* Quick Actions & User Profile */}
          <div className="flex items-center space-x-3">
            
            {/* Benchmark button */}
            <button
              onClick={onOpenBenchmark}
              className="hidden sm:inline-flex items-center space-x-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg bg-indigo-500/10 text-indigo-300 border border-indigo-500/20 hover:bg-indigo-500/20 transition"
              title="Ejecutar 50 peticiones automáticas para medir Hit Ratio y latencias con Redis"
            >
              <Zap className="h-3.5 w-3.5 text-indigo-400" />
              <span>Benchmark Caché</span>
            </button>

            {/* Gateway Config Toggle */}
            <button
              onClick={() => setShowConfig(!showConfig)}
              className="p-2 text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 rounded-lg transition"
              title="Configurar URL del API Gateway"
            >
              <Settings className="h-4 w-4" />
            </button>

            {currentUser ? (
              <div className="flex items-center space-x-3 pl-2 border-l border-slate-800">
                <div className="text-right">
                  <div className="flex items-center justify-end space-x-1.5">
                    <span className="text-xs font-medium text-slate-200">{currentUser.fullName || currentUser.username}</span>
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${
                        currentUser.role === 'ADMIN'
                          ? 'bg-purple-500/10 text-purple-400 border-purple-500/20'
                          : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                      }`}
                    >
                      {currentUser.role}
                    </span>
                  </div>
                  <span className="text-[11px] text-slate-400 font-mono">{currentUser.username}</span>
                </div>

                <button
                  onClick={onLogout}
                  className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition"
                  title="Cerrar sesión"
                >
                  <LogOut className="h-4 w-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => onQuickLogin('admin@uptc.edu.co', 'Admin123*')}
                  className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-purple-500/10 text-purple-300 border border-purple-500/20 hover:bg-purple-500/20 transition"
                  title="Iniciar sesión rápida como ADMIN"
                >
                  ⚡ Admin
                </button>
                <button
                  onClick={() => onQuickLogin('estudiante1@uptc.edu.co', 'Student123*')}
                  className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-emerald-500/10 text-emerald-300 border border-emerald-500/20 hover:bg-emerald-500/20 transition"
                  title="Iniciar sesión rápida como ESTUDIANTE"
                >
                  ⚡ Estudiante
                </button>
                <button
                  onClick={onLoginClick}
                  className="px-3.5 py-1.5 text-xs font-semibold rounded-lg bg-emerald-600 text-white hover:bg-emerald-500 shadow-md shadow-emerald-600/20 transition"
                >
                  Iniciar Sesión
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Expandable Gateway URL config bar */}
        {showConfig && (
          <div className="py-3 px-4 mb-2 bg-slate-900/90 rounded-xl border border-slate-800 text-xs flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center space-x-2">
              <Server className="h-4 w-4 text-emerald-400" />
              <span className="font-semibold text-slate-300">URL del API Gateway:</span>
            </div>
            <form onSubmit={handleSaveGateway} className="flex items-center space-x-2 flex-1 max-w-md">
              <input
                type="text"
                value={gatewayInput}
                onChange={(e) => setGatewayInput(e.target.value)}
                placeholder="https://api-gateway.nice-...azurecontainerapps.io"
                className="flex-1 bg-slate-950 border border-slate-700 rounded-md px-2.5 py-1 text-slate-200 font-mono text-xs focus:outline-none focus:border-emerald-500"
              />
              <button
                type="submit"
                className="px-3 py-1 bg-emerald-600 hover:bg-emerald-500 text-white font-medium rounded-md text-xs transition"
              >
                Guardar
              </button>
            </form>
            {savedSuccess && (
              <span className="text-emerald-400 flex items-center space-x-1">
                <CheckCircle2 className="h-3.5 w-3.5" />
                <span>Actualizado</span>
              </span>
            )}
          </div>
        )}
      </div>
    </header>
  );
}
