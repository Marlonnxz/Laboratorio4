import React, { useEffect, useState } from 'react';
import { Activity, Clock, Zap, CheckCircle2, AlertTriangle, ShieldAlert } from 'lucide-react';
import { onTelemetry } from '../api/client';

export default function DiagnosticsBar() {
  const [telemetry, setTelemetry] = useState(null);

  useEffect(() => {
    const unsubscribe = onTelemetry((event) => {
      setTelemetry(event);
    });
    return unsubscribe;
  }, []);

  if (!telemetry) {
    return (
      <div className="bg-slate-900/40 border-b border-slate-800/80 px-4 py-2">
        <div className="max-w-7xl mx-auto flex items-center justify-between text-xs text-slate-400">
          <div className="flex items-center space-x-2">
            <span className="flex h-2 w-2 relative">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
            </span>
            <span>Gateway & Telemetría en Vivo: Realiza peticiones para visualizar X-Cache y latencias</span>
          </div>
          <span className="text-[11px] text-slate-400">Patrón Cache-Aside • Redis Administrado</span>
        </div>
      </div>
    );
  }

  const isHit = telemetry.cacheHeader === 'HIT';
  const isMiss = telemetry.cacheHeader === 'MISS';
  const isError = telemetry.status >= 400 || telemetry.status === 0;

  return (
    <div className="bg-slate-900/60 border-b border-slate-800 px-4 py-2 transition-all">
      <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-2 text-xs">
        
        {/* Left: Last request detail */}
        <div className="flex items-center space-x-2 overflow-hidden">
          <span className="text-slate-400 font-mono text-[11px]">{telemetry.timestamp}</span>
          <span className="font-mono font-bold px-1.5 py-0.5 rounded bg-slate-800 text-slate-200 text-[11px]">
            {telemetry.method}
          </span>
          <span className="font-mono text-slate-300 truncate max-w-xs md:max-w-md lg:max-w-lg" title={telemetry.url}>
            {telemetry.url.replace(/^https?:\/\/[^/]+/, '')}
          </span>
        </div>

        {/* Right: Status, Cache Badge, Duration */}
        <div className="flex items-center space-x-3">
          
          {/* HTTP Status */}
          <span
            className={`font-mono text-[11px] font-semibold px-2 py-0.5 rounded-full flex items-center space-x-1 border ${
              telemetry.status === 200 || telemetry.status === 201 || telemetry.status === 204
                ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                : telemetry.status === 401
                ? 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                : telemetry.status === 403
                ? 'bg-rose-500/10 text-rose-400 border-rose-500/20'
                : 'bg-red-500/10 text-red-400 border-red-500/20'
            }`}
          >
            {telemetry.status === 403 ? <ShieldAlert className="h-3 w-3 inline mr-1" /> : null}
            <span>HTTP {telemetry.status || 'ERR'}</span>
          </span>

          {/* Cache X-Cache header */}
          <div className="flex items-center space-x-1">
            <span className="text-slate-400 text-[11px]">X-Cache:</span>
            {isHit ? (
              <span className="font-mono font-bold text-[11px] px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/40 glow-emerald flex items-center space-x-1 animate-pulse">
                <Zap className="h-3 w-3 text-emerald-400" />
                <span>HIT</span>
              </span>
            ) : isMiss ? (
              <span className="font-mono font-bold text-[11px] px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 border border-amber-500/40 flex items-center space-x-1">
                <Clock className="h-3 w-3 text-amber-400" />
                <span>MISS</span>
              </span>
            ) : (
              <span className="font-mono text-[11px] px-2 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                {telemetry.cacheHeader}
              </span>
            )}
          </div>

          {/* Latency */}
          <div className="flex items-center space-x-1 font-mono text-[11px] text-slate-300">
            <Clock className="h-3 w-3 text-slate-400" />
            <span className={telemetry.durationMs < 50 ? 'text-emerald-400 font-bold' : telemetry.durationMs < 200 ? 'text-teal-300' : 'text-slate-200'}>
              {telemetry.durationMs} ms
            </span>
          </div>

        </div>

      </div>
    </div>
  );
}
