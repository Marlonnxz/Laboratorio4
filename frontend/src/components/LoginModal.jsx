import React, { useState } from 'react';
import { Shield, Lock, Mail, X, ArrowRight, UserCheck, AlertCircle } from 'lucide-react';
import { apiRequest, storeAuth } from '../api/client';

export default function LoginModal({ isOpen, onClose, onLoginSuccess }) {
  const [username, setUsername] = useState('admin@uptc.edu.co');
  const [password, setPassword] = useState('Admin123*');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

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

      onLoginSuccess(data);
      onClose();
    } catch (err) {
      setError(err.message || 'Error al autenticar credenciales');
    } finally {
      setLoading(false);
    }
  };

  const fillCredentials = (user, pass) => {
    setUsername(user);
    setPassword(pass);
    setError(null);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fadeIn">
      <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden">
        
        {/* Header */}
        <div className="px-6 py-5 border-b border-slate-800 flex items-center justify-between bg-slate-850/50">
          <div className="flex items-center space-x-2.5">
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <Shield className="h-5 w-5" />
            </div>
            <div>
              <h3 className="font-bold text-slate-100 text-base">Iniciar Sesión</h3>
              <p className="text-xs text-slate-400">Autenticación basada en JWT & Roles</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-200 hover:bg-slate-800 rounded-lg transition"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Content */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          
          {error && (
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs flex items-start space-x-2">
              <AlertCircle className="h-4 w-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">Usuario o Correo</label>
            <div className="relative">
              <Mail className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="ej: admin@uptc.edu.co"
                className="w-full pl-9 pr-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 text-sm focus:outline-none focus:border-emerald-500 transition font-mono"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">Contraseña</label>
            <div className="relative">
              <Lock className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-9 pr-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 text-sm focus:outline-none focus:border-emerald-500 transition"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-semibold rounded-xl text-sm shadow-lg shadow-emerald-600/20 flex items-center justify-center space-x-2 transition"
          >
            {loading ? (
              <span className="text-xs">Autenticando en Azure...</span>
            ) : (
              <>
                <span>Ingresar al Sistema</span>
                <ArrowRight className="h-4 w-4" />
              </>
            )}
          </button>

          {/* Quick presets for evaluation */}
          <div className="pt-3 border-t border-slate-800/80">
            <span className="text-[11px] font-semibold text-slate-400 block mb-2">Credenciales de Prueba (Lab 4):</span>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button
                type="button"
                onClick={() => fillCredentials('admin@uptc.edu.co', 'Admin123*')}
                className="p-2 text-left rounded-lg bg-slate-800/40 hover:bg-slate-800 border border-slate-800 hover:border-purple-500/30 transition group"
              >
                <div className="flex items-center space-x-1.5 text-purple-400 font-semibold text-[11px]">
                  <Shield className="h-3 w-3" />
                  <span>Rol ADMIN</span>
                </div>
                <div className="text-[10px] text-slate-400 font-mono mt-0.5 truncate">admin@uptc.edu.co</div>
              </button>

              <button
                type="button"
                onClick={() => fillCredentials('estudiante1@uptc.edu.co', 'Student123*')}
                className="p-2 text-left rounded-lg bg-slate-800/40 hover:bg-slate-800 border border-slate-800 hover:border-emerald-500/30 transition group"
              >
                <div className="flex items-center space-x-1.5 text-emerald-400 font-semibold text-[11px]">
                  <UserCheck className="h-3 w-3" />
                  <span>Rol ESTUDIANTE</span>
                </div>
                <div className="text-[10px] text-slate-400 font-mono mt-0.5 truncate">estudiante1 (ID: 1)</div>
              </button>
            </div>
          </div>

        </form>

      </div>
    </div>
  );
}
