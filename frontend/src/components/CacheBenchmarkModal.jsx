import React, { useState } from 'react';
import { X, Zap, Play, CheckCircle2, Copy, BarChart3, Clock, Layers } from 'lucide-react';
import { apiRequest } from '../api/client';

export default function CacheBenchmarkModal({ isOpen, onClose }) {
  const [running, setRunning] = useState(false);
  const [progress, setProgress] = useState(0);
  const [results, setResults] = useState(null);
  const [copied, setCopied] = useState(false);

  if (!isOpen) return null;

  const runBenchmark = async () => {
    setRunning(true);
    setProgress(0);
    setResults(null);
    setCopied(false);

    const TOTAL_REQUESTS = 50;
    const records = [];

    for (let i = 1; i <= TOTAL_REQUESTS; i++) {
      try {
        const res = await apiRequest('/api/subjects?pageSize=10');
        records.push({
          reqIndex: i,
          durationMs: res.durationMs,
          cacheHeader: res.cacheHeader,
        });
      } catch (err) {
        records.push({
          reqIndex: i,
          durationMs: err.durationMs || 0,
          cacheHeader: 'ERR',
        });
      }
      setProgress(i);
      // Small tick between requests
      await new Promise((resolve) => setTimeout(resolve, 25));
    }

    // Calculations
    const hits = records.filter((r) => r.cacheHeader === 'HIT');
    const misses = records.filter((r) => r.cacheHeader === 'MISS');
    const hitRatio = ((hits.length / TOTAL_REQUESTS) * 100).toFixed(1);

    const avgHitLatency = hits.length > 0
      ? (hits.reduce((acc, r) => acc + r.durationMs, 0) / hits.length).toFixed(1)
      : 0;

    const avgMissLatency = misses.length > 0
      ? (misses.reduce((acc, r) => acc + r.durationMs, 0) / misses.length).toFixed(1)
      : 0;

    const speedup = avgHitLatency > 0
      ? (avgMissLatency / avgHitLatency).toFixed(1)
      : 'N/A';

    setResults({
      total: TOTAL_REQUESTS,
      hits: hits.length,
      misses: misses.length,
      hitRatio,
      avgHitLatency,
      avgMissLatency,
      speedup,
      records,
    });

    setRunning(false);
  };

  const copyMarkdownReport = () => {
    if (!results) return;

    const md = `### Resultados de la Medición de Rendimiento con Redis (Laboratorio 4)
- **Total Peticiones Ejecutadas:** ${results.total}
- **Aciertos de Caché (X-Cache: HIT):** ${results.hits}
- **Fallos de Caché (X-Cache: MISS):** ${results.misses}
- **Porcentaje de Aciertos (Hit Ratio):** ${results.hitRatio}%
- **Latencia promedio SIN caché (MISS):** ${results.avgMissLatency} ms
- **Latencia promedio CON caché (HIT):** ${results.avgHitLatency} ms
- **Factor de Aceleración (Speedup):** ${results.speedup}x

| Muestra Petición | Latencia (ms) | Cabecera X-Cache |
| :--- | :--- | :--- |
| Petición #1 (Calentamiento) | ${results.records[0]?.durationMs} ms | ${results.records[0]?.cacheHeader} |
| Petición #2 (Caché activo) | ${results.records[1]?.durationMs} ms | ${results.records[1]?.cacheHeader} |
| Petición #10 | ${results.records[9]?.durationMs} ms | ${results.records[9]?.cacheHeader} |
| Petición #25 | ${results.records[24]?.durationMs} ms | ${results.records[24]?.cacheHeader} |
| Petición #50 | ${results.records[49]?.durationMs} ms | ${results.records[49]?.cacheHeader} |
`;

    navigator.clipboard.writeText(md);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fadeIn">
      <div className="relative w-full max-w-3xl bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden max-h-[90vh] flex flex-col">
        
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between bg-slate-850/50">
          <div className="flex items-center space-x-2.5">
            <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              <Zap className="h-5 w-5" />
            </div>
            <div>
              <h3 className="font-bold text-slate-100 text-base">
                Evaluador de Rendimiento de Caché Redis
              </h3>
              <p className="text-xs text-slate-400">Requisito Sección 5.3: Prueba de 50 peticiones y cálculo de Hit Ratio</p>
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
        <div className="p-6 overflow-y-auto flex-1 space-y-6">
          
          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-300 leading-relaxed flex items-center justify-between">
            <div>
              Esta herramienta ejecuta 50 consultas consecutivas a <code>GET /api/subjects</code> contra el Gateway, recolectando las cabeceras <code>X-Cache: HIT</code> y <code>MISS</code> para calcular el Hit Ratio y la aceleración.
            </div>
            <button
              onClick={runBenchmark}
              disabled={running}
              className="ml-4 shrink-0 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-semibold rounded-xl text-xs flex items-center space-x-2 shadow-lg shadow-indigo-600/20 transition"
            >
              <Play className="h-4 w-4" />
              <span>{running ? `Ejecutando (${progress}/50)...` : 'Iniciar Test (50 reqs)'}</span>
            </button>
          </div>

          {/* Progress Bar */}
          {running && (
            <div className="space-y-2">
              <div className="flex justify-between text-xs text-slate-400">
                <span>Progreso de peticiones</span>
                <span className="font-mono">{progress} / 50</span>
              </div>
              <div className="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
                <div
                  className="bg-indigo-500 h-2.5 rounded-full transition-all duration-150"
                  style={{ width: `${(progress / 50) * 100}%` }}
                ></div>
              </div>
            </div>
          )}

          {/* Results Summary Cards */}
          {results && (
            <div className="space-y-5 animate-fadeIn">
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                <div className="p-4 rounded-xl bg-slate-950 border border-slate-800">
                  <span className="text-[11px] text-slate-400 uppercase font-semibold">Hit Ratio</span>
                  <div className="text-2xl font-extrabold text-emerald-400 mt-1 font-mono">
                    {results.hitRatio}%
                  </div>
                  <span className="text-[10px] text-slate-500">{results.hits} hits / {results.total} reqs</span>
                </div>

                <div className="p-4 rounded-xl bg-slate-950 border border-slate-800">
                  <span className="text-[11px] text-slate-400 uppercase font-semibold">Latencia MISS</span>
                  <div className="text-2xl font-extrabold text-amber-400 mt-1 font-mono">
                    {results.avgMissLatency} <span className="text-sm font-normal">ms</span>
                  </div>
                  <span className="text-[10px] text-slate-500">Sin caché (base de datos)</span>
                </div>

                <div className="p-4 rounded-xl bg-slate-950 border border-slate-800">
                  <span className="text-[11px] text-slate-400 uppercase font-semibold">Latencia HIT</span>
                  <div className="text-2xl font-extrabold text-teal-400 mt-1 font-mono">
                    {results.avgHitLatency} <span className="text-sm font-normal">ms</span>
                  </div>
                  <span className="text-[10px] text-slate-500">Con Redis en memoria</span>
                </div>

                <div className="p-4 rounded-xl bg-slate-950 border border-slate-800">
                  <span className="text-[11px] text-slate-400 uppercase font-semibold">Aceleración</span>
                  <div className="text-2xl font-extrabold text-indigo-400 mt-1 font-mono">
                    {results.speedup}x
                  </div>
                  <span className="text-[10px] text-slate-500">Más rápido con caché</span>
                </div>
              </div>

              {/* Sample list of requests */}
              <div className="rounded-xl border border-slate-800 overflow-hidden bg-slate-950/60">
                <div className="px-4 py-2.5 bg-slate-900 border-b border-slate-800 flex items-center justify-between text-xs">
                  <span className="font-semibold text-slate-300">Muestreo de Respuestas (Primeras 15):</span>
                  <button
                    onClick={copyMarkdownReport}
                    className="flex items-center space-x-1.5 px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-200 transition text-[11px]"
                  >
                    {copied ? <CheckCircle2 className="h-3.5 w-3.5 text-emerald-400" /> : <Copy className="h-3.5 w-3.5" />}
                    <span>{copied ? '¡Copiado!' : 'Copiar Tabla en Markdown'}</span>
                  </button>
                </div>

                <div className="max-h-52 overflow-y-auto divide-y divide-slate-800/60 font-mono text-xs">
                  {results.records.slice(0, 15).map((r) => (
                    <div key={r.reqIndex} className="px-4 py-2 flex items-center justify-between text-slate-300">
                      <span>Petición #{r.reqIndex}</span>
                      <span className={r.durationMs < 50 ? 'text-emerald-400' : 'text-amber-400'}>
                        {r.durationMs} ms
                      </span>
                      <span
                        className={`text-[10px] px-2 py-0.5 rounded font-bold ${
                          r.cacheHeader === 'HIT'
                            ? 'bg-emerald-500/20 text-emerald-300'
                            : 'bg-amber-500/20 text-amber-300'
                        }`}
                      >
                        X-Cache: {r.cacheHeader}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

            </div>
          )}

        </div>

      </div>
    </div>
  );
}

