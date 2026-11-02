import React, { useEffect, useState, useRef } from 'react';
import {
  BookOpen,
  Plus,
  FileText,
  Download,
  Upload,
  RefreshCw,
  Trash2,
  Zap,
  Clock,
  ShieldAlert,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';
import { apiRequest } from '../api/client';

export default function SubjectsView({ currentUser }) {
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [lastCacheStatus, setLastCacheStatus] = useState(null);
  const [lastLatency, setLastLatency] = useState(null);

  // New Subject Modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newSubject, setNewSubject] = useState({ name: '', credits: 3, program: 'Ingeniería de Sistemas' });
  const [actionNotice, setActionNotice] = useState(null);

  // Upload PDF state
  const [uploadingSubjectId, setUploadingSubjectId] = useState(null);
  const pdfInputRef = useRef(null);

  useEffect(() => {
    fetchSubjects();
  }, []);

  const fetchSubjects = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiRequest('/api/subjects?pageSize=50');
      const list = res.data?.content || (Array.isArray(res.data) ? res.data : []);
      setSubjects(list);
      setLastCacheStatus(res.cacheHeader);
      setLastLatency(res.durationMs);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSubject = async (e) => {
    e.preventDefault();
    setActionNotice(null);
    setLoading(true);

    try {
      const res = await apiRequest('/api/subjects', {
        method: 'POST',
        body: JSON.stringify(newSubject),
      });

      setShowCreateModal(false);
      setNewSubject({ name: '', credits: 3, program: 'Ingeniería de Sistemas' });
      setActionNotice({
        type: 'success',
        title: 'Materia creada exitosamente (HTTP 201)',
        message: 'El Gateway invalidó automáticamente el caché de Redis. La próxima lectura del catálogo marcará X-Cache: MISS.',
      });
      fetchSubjects();
    } catch (err) {
      setActionNotice({
        type: 'error',
        status: err.status || 500,
        title: err.status === 403 ? 'Acceso Denegado (HTTP 403 Forbidden)' : 'Error al crear materia',
        message: err.message,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteSubject = async (id, name) => {
    if (!confirm(`¿Eliminar la materia "${name}"?`)) return;
    setActionNotice(null);

    try {
      await apiRequest(`/api/subjects/${id}`, {
        method: 'DELETE',
      });

      setActionNotice({
        type: 'success',
        title: 'Materia eliminada (HTTP 204)',
        message: 'Caché de catálogo invalidado en Redis tras la eliminación.',
      });
      fetchSubjects();
    } catch (err) {
      setActionNotice({
        type: 'error',
        status: err.status || 500,
        title: err.status === 403 ? 'Acceso Denegado (HTTP 403 Forbidden)' : 'Error al eliminar materia',
        message: err.message,
      });
    }
  };

  const handleSelectPdf = (subjectId) => {
    setUploadingSubjectId(subjectId);
    if (pdfInputRef.current) {
      pdfInputRef.current.value = '';
      pdfInputRef.current.click();
    }
  };

  const handlePdfUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file || !uploadingSubjectId) return;

    if (file.size > 10 * 1024 * 1024) {
      alert('Error: El PDF no puede superar los 10 MB (requisito Lab 4).');
      return;
    }
    if (!file.name.toLowerCase().endsWith('.pdf') && file.type !== 'application/pdf') {
      alert('Error: Solo se permiten archivos en formato PDF (requisito Lab 4).');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    setLoading(true);
    try {
      await apiRequest(`/api/subjects/${uploadingSubjectId}/pdf`, {
        method: 'POST',
        body: formData,
      });

      setActionNotice({
        type: 'success',
        title: 'PDF temático subido a Azure Blob Storage',
        message: `El archivo "${file.name}" se guardó en el bucket privado con metadatos en PostgreSQL.`,
      });
      fetchSubjects();
    } catch (err) {
      alert(`Error al subir PDF: ${err.message}`);
    } finally {
      setLoading(false);
      setUploadingSubjectId(null);
    }
  };

  return (
    <div className="space-y-6">
      
      {/* Hidden PDF file input */}
      <input
        type="file"
        ref={pdfInputRef}
        accept="application/pdf,.pdf"
        onChange={handlePdfUpload}
        className="hidden"
      />

      {/* Header & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center space-x-2">
            <BookOpen className="h-5 w-5 text-indigo-400" />
            <span>Catálogo de Materias</span>
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Caché administrado en Redis (X-Cache: HIT/MISS) • PDFs temáticos en Azure Blob Storage
          </p>
        </div>

        <div className="flex items-center space-x-3">
          {/* Quick diagnostic badge for the last catalog read */}
          {lastCacheStatus && (
            <div className="hidden md:flex items-center space-x-2 px-3 py-1.5 rounded-xl bg-slate-900 border border-slate-800 text-xs">
              <span className="text-slate-400">Última lectura:</span>
              <span
                className={`font-mono font-bold px-1.5 py-0.5 rounded text-[10px] ${
                  lastCacheStatus === 'HIT'
                    ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                    : 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                }`}
              >
                {lastCacheStatus}
              </span>
              <span className="font-mono text-slate-300 text-[11px]">{lastLatency} ms</span>
            </div>
          )}

          <button
            onClick={fetchSubjects}
            className="p-2 text-slate-400 hover:text-slate-200 bg-slate-900 hover:bg-slate-800 border border-slate-800 rounded-xl transition"
            title="Refrescar catálogo (comprueba MISS -> HIT)"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin text-indigo-400' : ''}`} />
          </button>

          <button
            onClick={() => setShowCreateModal(true)}
            className="py-2 px-3.5 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold rounded-xl text-xs flex items-center space-x-1.5 shadow-lg shadow-indigo-600/20 transition"
          >
            <Plus className="h-4 w-4" />
            <span>Nueva Materia</span>
          </button>
        </div>
      </div>

      {/* Action Notices / RBAC Feedback */}
      {actionNotice && (
        <div
          className={`p-4 rounded-xl border text-xs flex items-start space-x-3 animate-fadeIn ${
            actionNotice.type === 'success'
              ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-200'
              : 'bg-rose-500/10 border-rose-500/30 text-rose-200'
          }`}
        >
          {actionNotice.type === 'success' ? (
            <CheckCircle2 className="h-5 w-5 text-emerald-400 shrink-0 mt-0.5" />
          ) : (
            <ShieldAlert className="h-5 w-5 text-rose-400 shrink-0 mt-0.5" />
          )}
          <div className="flex-1">
            <h4 className="font-bold text-sm mb-0.5">{actionNotice.title}</h4>
            <p className="leading-relaxed">{actionNotice.message}</p>
          </div>
          <button
            onClick={() => setActionNotice(null)}
            className="text-slate-400 hover:text-slate-200 text-sm font-bold"
          >
            ×
          </button>
        </div>
      )}

      {/* Table of Subjects */}
      <div className="glass-card rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 border-b border-slate-800 text-slate-400 font-semibold uppercase tracking-wider text-[11px]">
              <tr>
                <th className="py-3.5 px-4">ID</th>
                <th className="py-3.5 px-4">Asignatura</th>
                <th className="py-3.5 px-4">Créditos</th>
                <th className="py-3.5 px-4">Programa</th>
                <th className="py-3.5 px-4">Contenido Temático (PDF)</th>
                <th className="py-3.5 px-4 text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300">
              {subjects.map((sub) => (
                <tr key={sub.id} className="hover:bg-slate-850/40 transition">
                  <td className="py-3 px-4 font-mono font-semibold text-slate-400">#{sub.id}</td>
                  <td className="py-3 px-4 font-bold text-white">{sub.name}</td>
                  <td className="py-3 px-4">
                    <span className="px-2 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[11px]">
                      {sub.credits} créditos
                    </span>
                  </td>
                  <td className="py-3 px-4 text-slate-400">{sub.program}</td>
                  
                  {/* PDF Column */}
                  <td className="py-3 px-4">
                    {sub.pdfUrl ? (
                      <div className="flex items-center space-x-2">
                        <a
                          href={sub.pdfUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="inline-flex items-center space-x-1.5 px-2.5 py-1 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border border-emerald-500/20 font-medium transition"
                          title="Descargar syllabus con URL firmada (SAS Token)"
                        >
                          <Download className="h-3.5 w-3.5 text-emerald-400" />
                          <span className="truncate max-w-[120px]">{sub.pdfOriginalName || 'Descargar PDF'}</span>
                        </a>

                        <button
                          onClick={() => handleSelectPdf(sub.id)}
                          className="p-1 text-slate-500 hover:text-slate-300 rounded transition"
                          title="Reemplazar archivo PDF"
                        >
                          <Upload className="h-3.5 w-3.5" />
                        </button>
                      </div>
                    ) : (
                      <button
                        onClick={() => handleSelectPdf(sub.id)}
                        className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 transition"
                      >
                        <Upload className="h-3 w-3 text-slate-400" />
                        <span>Subir PDF (10MB)</span>
                      </button>
                    )}
                  </td>

                  {/* Actions Column */}
                  <td className="py-3 px-4 text-right">
                    <button
                      onClick={() => handleDeleteSubject(sub.id, sub.name)}
                      className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition"
                      title="Eliminar materia (ADMIN requerido)"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </td>
                </tr>
              ))}

              {!loading && subjects.length === 0 && (
                <tr>
                  <td colSpan="6" className="py-10 text-center text-slate-500">
                    No hay materias registradas en el catálogo.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create Subject Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fadeIn">
          <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 space-y-4">
            <h3 className="font-bold text-slate-100 text-base flex items-center space-x-2">
              <Plus className="h-4 w-4 text-indigo-400" />
              <span>Registrar Asignatura (Demostración RBAC & Invalidación)</span>
            </h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Recuerda: Si iniciaste sesión como <strong>ESTUDIANTE</strong>, el Gateway rechazará la petición con un error <strong>403 Forbidden</strong>. Si eres <strong>ADMIN</strong>, la creará e invalidará el caché de Redis.
            </p>

            <form onSubmit={handleCreateSubject} className="space-y-3 pt-2">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Nombre de la Materia</label>
                <input
                  type="text"
                  required
                  value={newSubject.name}
                  onChange={(e) => setNewSubject({ ...newSubject, name: e.target.value })}
                  placeholder="ej: Computación en la Nube"
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Créditos</label>
                  <input
                    type="number"
                    min="1"
                    max="10"
                    required
                    value={newSubject.credits}
                    onChange={(e) => setNewSubject({ ...newSubject, credits: parseInt(e.target.value) || 3 })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Programa</label>
                  <input
                    type="text"
                    required
                    value={newSubject.program}
                    onChange={(e) => setNewSubject({ ...newSubject, program: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="pt-3 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 text-xs font-semibold rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white transition flex items-center space-x-1.5"
                >
                  <span>Crear Asignatura</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}

