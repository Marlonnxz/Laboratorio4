import React, { useState, useEffect, useRef } from 'react';
import {
  Shield,
  Users,
  BookOpen,
  Zap,
  Plus,
  Upload,
  Download,
  Trash2,
  ExternalLink,
  RefreshCw,
  LogOut,
  User,
  CheckCircle2,
  AlertCircle,
  FileCheck,
  Play,
  Copy,
  Clock
} from 'lucide-react';
import { apiRequest } from '../api/client';
import StudentDetailModal from './StudentDetailModal';

export default function AdminPortal({ currentUser, onLogout }) {
  const [activeTab, setActiveTab] = useState('students'); // 'students' | 'subjects' | 'benchmark'

  // Students state
  const [students, setStudents] = useState([]);
  const [loadingStudents, setLoadingStudents] = useState(false);
  const [selectedStudentId, setSelectedStudentId] = useState(null);
  const [uploadingStudentId, setUploadingStudentId] = useState(null);
  const [studentNotice, setStudentNotice] = useState(null);
  const photoInputRef = useRef(null);

  // Subjects state
  const [subjects, setSubjects] = useState([]);
  const [loadingSubjects, setLoadingSubjects] = useState(false);
  const [subjectNotice, setSubjectNotice] = useState(null);
  const [showCreateSubjectModal, setShowCreateSubjectModal] = useState(false);
  const [newSubject, setNewSubject] = useState({ name: '', credits: 3, program: 'Ingeniería de Sistemas y Computación' });
  const [uploadingSubjectId, setUploadingSubjectId] = useState(null);
  const pdfInputRef = useRef(null);

  // Benchmark state
  const [runningBenchmark, setRunningBenchmark] = useState(false);
  const [benchmarkProgress, setBenchmarkProgress] = useState(0);
  const [benchmarkResults, setBenchmarkResults] = useState(null);
  const [copiedBenchmark, setCopiedBenchmark] = useState(false);

  useEffect(() => {
    if (activeTab === 'students') {
      fetchStudents();
    } else if (activeTab === 'subjects') {
      fetchSubjects();
    }
  }, [activeTab]);

  const fetchStudents = async () => {
    setLoadingStudents(true);
    try {
      const res = await apiRequest('/api/students?pageSize=50');
      const list = res.data?.content || (Array.isArray(res.data) ? res.data : []);
      setStudents(list);
    } catch (err) {
      setStudentNotice({ type: 'error', message: err.message });
    } finally {
      setLoadingStudents(false);
    }
  };

  const fetchSubjects = async () => {
    setLoadingSubjects(true);
    try {
      const res = await apiRequest('/api/subjects?pageSize=50');
      const list = res.data?.content || (Array.isArray(res.data) ? res.data : []);
      setSubjects(list);
    } catch (err) {
      setSubjectNotice({ type: 'error', message: err.message });
    } finally {
      setLoadingSubjects(false);
    }
  };

  // --- Photo Upload ---
  const handleSelectPhoto = (id) => {
    setUploadingStudentId(id);
    if (photoInputRef.current) {
      photoInputRef.current.value = '';
      photoInputRef.current.click();
    }
  };

  const handlePhotoUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file || !uploadingStudentId) return;

    if (file.size > 2 * 1024 * 1024) {
      alert('Error: La imagen no puede superar 2 MB (requisito Lab 4).');
      return;
    }
    if (!file.type.match(/^image\/(jpeg|png|jpg)$/i)) {
      alert('Error: Solo se permiten imágenes JPG o PNG (requisito Lab 4).');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
      await apiRequest(`/api/students/${uploadingStudentId}/photo`, {
        method: 'POST',
        body: formData,
      });
      setStudentNotice({
        type: 'success',
        message: `Fotografía subida al contenedor privado de Azure Blob Storage para el estudiante #${uploadingStudentId}.`,
      });
      setTimeout(() => setStudentNotice(null), 4000);
      fetchStudents();
    } catch (err) {
      alert(`Error al subir imagen: ${err.message}`);
    } finally {
      setUploadingStudentId(null);
    }
  };

  // --- Subject Management ---
  const handleCreateSubject = async (e) => {
    e.preventDefault();
    try {
      await apiRequest('/api/subjects', {
        method: 'POST',
        body: JSON.stringify(newSubject),
      });
      setShowCreateSubjectModal(false);
      setNewSubject({ name: '', credits: 3, program: 'Ingeniería de Sistemas y Computación' });
      setSubjectNotice({
        type: 'success',
        title: 'Materia Creada (HTTP 201)',
        message: 'El API Gateway invalidó automáticamente el caché de Redis. La próxima lectura del catálogo reflejará X-Cache: MISS.',
      });
      fetchSubjects();
    } catch (err) {
      alert(`Error al crear materia: ${err.message}`);
    }
  };

  const handleDeleteSubject = async (id, name) => {
    if (!confirm(`¿Eliminar la materia "${name}"? Esta acción invalidará el caché de Redis.`)) return;
    try {
      await apiRequest(`/api/subjects/${id}`, { method: 'DELETE' });
      setSubjectNotice({
        type: 'success',
        title: 'Materia Eliminada (HTTP 204)',
        message: 'Caché de catálogo invalidado en Redis tras la eliminación.',
      });
      fetchSubjects();
    } catch (err) {
      alert(`Error al eliminar materia: ${err.message}`);
    }
  };

  // --- PDF Upload ---
  const handleSelectPdf = (id) => {
    setUploadingSubjectId(id);
    if (pdfInputRef.current) {
      pdfInputRef.current.value = '';
      pdfInputRef.current.click();
    }
  };

  const handlePdfUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file || !uploadingSubjectId) return;

    if (file.size > 10 * 1024 * 1024) {
      alert('Error: El PDF no puede superar 10 MB (requisito Lab 4).');
      return;
    }
    if (!file.name.toLowerCase().endsWith('.pdf') && file.type !== 'application/pdf') {
      alert('Error: Solo se permiten archivos en formato PDF.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
      await apiRequest(`/api/subjects/${uploadingSubjectId}/pdf`, {
        method: 'POST',
        body: formData,
      });
      setSubjectNotice({
        type: 'success',
        title: 'PDF Temático Almacenado',
        message: `El archivo "${file.name}" se cargó en Azure Blob Storage con URL temporal SAS.`,
      });
      fetchSubjects();
    } catch (err) {
      alert(`Error al subir PDF: ${err.message}`);
    } finally {
      setUploadingSubjectId(null);
    }
  };

  // --- Benchmark Tool (50 Requests) ---
  const runBenchmark = async () => {
    setRunningBenchmark(true);
    setBenchmarkProgress(0);
    setBenchmarkResults(null);

    const TOTAL = 50;
    const records = [];

    for (let i = 1; i <= TOTAL; i++) {
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
      setBenchmarkProgress(i);
      await new Promise((r) => setTimeout(r, 20));
    }

    const hits = records.filter((r) => r.cacheHeader === 'HIT');
    const misses = records.filter((r) => r.cacheHeader === 'MISS');
    const hitRatio = ((hits.length / TOTAL) * 100).toFixed(1);
    const avgHit = hits.length > 0 ? (hits.reduce((acc, r) => acc + r.durationMs, 0) / hits.length).toFixed(1) : 0;
    const avgMiss = misses.length > 0 ? (misses.reduce((acc, r) => acc + r.durationMs, 0) / misses.length).toFixed(1) : 0;
    const speedup = avgHit > 0 ? (avgMiss / avgHit).toFixed(1) : 'N/A';

    setBenchmarkResults({
      total: TOTAL,
      hits: hits.length,
      misses: misses.length,
      hitRatio,
      avgHit,
      avgMiss,
      speedup,
      records,
    });
    setRunningBenchmark(false);
  };

  const copyBenchmarkMarkdown = () => {
    if (!benchmarkResults) return;
    const md = `| Métrica | Valor Obtenido |
| :--- | :--- |
| Total de Peticiones | ${benchmarkResults.total} |
| Hit Ratio (% aciertos) | **${benchmarkResults.hitRatio}%** |
| Latencia Promedio MISS (Base de Datos) | ${benchmarkResults.avgMiss} ms |
| Latencia Promedio HIT (Redis en Memoria) | ${benchmarkResults.avgHit} ms |
| Factor de Aceleración | **${benchmarkResults.speedup}x más rápido** |
`;
    navigator.clipboard.writeText(md);
    setCopiedBenchmark(true);
    setTimeout(() => setCopiedBenchmark(false), 2000);
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      
      {/* Hidden file inputs */}
      <input type="file" ref={photoInputRef} accept="image/png, image/jpeg, image/jpg" onChange={handlePhotoUpload} className="hidden" />
      <input type="file" ref={pdfInputRef} accept="application/pdf,.pdf" onChange={handlePdfUpload} className="hidden" />

      {/* Top Header for Admin */}
      <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur-xl sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="h-9 w-9 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400 flex items-center justify-center font-bold">
              <Shield className="h-5 w-5" />
            </div>
            <div>
              <span className="font-extrabold text-white text-base tracking-tight">Portal Administrativo</span>
              <span className="hidden sm:inline-block ml-2 text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-purple-500/10 text-purple-300 border border-purple-500/20">
                Control Total
              </span>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <div className="text-right">
              <div className="text-xs font-semibold text-slate-200">
                {currentUser.fullName || currentUser.username}
              </div>
              <div className="text-[10px] text-purple-400 font-mono">
                Rol: ADMIN (Gestor del Sistema)
              </div>
            </div>

            <button
              onClick={onLogout}
              className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-xl transition cursor-pointer"
              title="Cerrar Sesión"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 py-6 space-y-6">
        
        {/* Navigation Tabs */}
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-3">
          <div className="flex space-x-2 bg-slate-900 p-1 rounded-xl border border-slate-800">
            <button
              onClick={() => setActiveTab('students')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-xs font-semibold transition cursor-pointer ${
                activeTab === 'students'
                  ? 'bg-purple-600 text-white shadow-md shadow-purple-600/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <Users className="h-4 w-4" />
              <span>Gestión de Estudiantes & Fotos</span>
            </button>

            <button
              onClick={() => setActiveTab('subjects')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-xs font-semibold transition cursor-pointer ${
                activeTab === 'subjects'
                  ? 'bg-purple-600 text-white shadow-md shadow-purple-600/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <BookOpen className="h-4 w-4" />
              <span>Catálogo de Materias & PDFs</span>
            </button>

            <button
              onClick={() => setActiveTab('benchmark')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-xs font-semibold transition cursor-pointer ${
                activeTab === 'benchmark'
                  ? 'bg-purple-600 text-white shadow-md shadow-purple-600/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <Zap className="h-4 w-4 text-amber-400" />
              <span>Auditoría de Caché (50 Peticiones)</span>
            </button>
          </div>

          <div className="text-xs text-slate-400 font-mono">
            Ambiente: <span className="text-emerald-400">Microsoft Azure Cloud</span>
          </div>
        </div>

        {/* TAB 1: ESTUDIANTES */}
        {activeTab === 'students' && (
          <div className="space-y-4">
            
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-white text-base">Directorio General de Estudiantes</h3>
                <p className="text-xs text-slate-400">
                  Como Administrador puedes subir fotos (Azure Blob) y auditar cualquier ficha académica (/detail)
                </p>
              </div>
              <button
                onClick={fetchStudents}
                className="p-2 text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800 rounded-xl transition cursor-pointer"
                title="Refrescar"
              >
                <RefreshCw className={`h-4 w-4 ${loadingStudents ? 'animate-spin text-purple-400' : ''}`} />
              </button>
            </div>

            {studentNotice && (
              <div className="p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs flex items-center space-x-2">
                <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                <span>{studentNotice.message}</span>
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {students.map((student) => (
                <div
                  key={student.id}
                  className="bg-slate-900 rounded-2xl p-5 border border-slate-800 hover:border-slate-700 transition flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-start justify-between gap-3 mb-3">
                      <div className="relative">
                        {student.photoUrl ? (
                          <img
                            src={student.photoUrl}
                            alt={student.firstName}
                            className="w-14 h-14 rounded-2xl object-cover border-2 border-emerald-500/40"
                          />
                        ) : (
                          <div className="w-14 h-14 rounded-2xl bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-400">
                            <User className="h-7 w-7" />
                          </div>
                        )}
                        {student.photoUrl && (
                          <span className="absolute -bottom-1 -right-1 p-0.5 bg-emerald-500 text-slate-950 rounded-full" title="Azure Blob Storage">
                            <FileCheck className="h-3 w-3" />
                          </span>
                        )}
                      </div>
                      <span className="text-[11px] font-mono px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                        ID #{student.id}
                      </span>
                    </div>

                    <h4 className="font-bold text-white text-base">
                      {student.firstName} {student.lastName}
                    </h4>
                    <p className="text-xs text-slate-400 font-mono mt-0.5 truncate">{student.email}</p>
                    <p className="text-xs text-slate-500 mt-1">Programa #{student.programId || '1'}</p>
                  </div>

                  <div className="mt-4 pt-3 border-t border-slate-800/80 flex items-center gap-2">
                    <button
                      onClick={() => setSelectedStudentId(student.id)}
                      className="flex-1 py-1.5 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold flex items-center justify-center space-x-1 transition cursor-pointer"
                    >
                      <span>Auditar /detalle</span>
                      <ExternalLink className="h-3.5 w-3.5" />
                    </button>

                    <button
                      onClick={() => handleSelectPhoto(student.id)}
                      className="py-1.5 px-3 rounded-xl bg-purple-500/10 hover:bg-purple-500/20 text-purple-300 border border-purple-500/20 text-xs font-semibold flex items-center space-x-1 transition cursor-pointer"
                      title="Subir foto JPG o PNG (máximo 2 MB a Azure Blob Storage)"
                    >
                      <Upload className="h-3.5 w-3.5 text-purple-400" />
                      <span>Subir Foto</span>
                    </button>
                  </div>
                </div>
              ))}
            </div>

          </div>
        )}

        {/* TAB 2: MATERIAS */}
        {activeTab === 'subjects' && (
          <div className="space-y-4">
            
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <h3 className="font-bold text-white text-base">Gestión del Catálogo de Materias & Caché Redis</h3>
                <p className="text-xs text-slate-400">
                  Cualquier creación, edición o eliminación de materia invalida automáticamente el caché en Redis
                </p>
              </div>

              <div className="flex items-center space-x-2">
                <button
                  onClick={fetchSubjects}
                  className="p-2 text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800 rounded-xl transition cursor-pointer"
                  title="Refrescar catálogo"
                >
                  <RefreshCw className={`h-4 w-4 ${loadingSubjects ? 'animate-spin text-purple-400' : ''}`} />
                </button>

                <button
                  onClick={() => setShowCreateSubjectModal(true)}
                  className="py-2 px-3.5 bg-purple-600 hover:bg-purple-500 text-white font-semibold rounded-xl text-xs flex items-center space-x-1.5 shadow-lg shadow-purple-600/20 transition cursor-pointer"
                >
                  <Plus className="h-4 w-4" />
                  <span>+ Nueva Materia</span>
                </button>
              </div>
            </div>

            {subjectNotice && (
              <div className="p-4 rounded-xl bg-slate-900 border border-slate-700 text-xs flex items-start space-x-3 animate-fadeIn">
                <CheckCircle2 className="h-5 w-5 text-emerald-400 shrink-0 mt-0.5" />
                <div className="flex-1">
                  <h5 className="font-bold text-slate-100">{subjectNotice.title}</h5>
                  <p className="text-slate-300 mt-0.5">{subjectNotice.message}</p>
                </div>
                <button onClick={() => setSubjectNotice(null)} className="text-slate-400 hover:text-slate-200 text-sm font-bold">×</button>
              </div>
            )}

            <div className="bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-950 border-b border-slate-800 text-slate-400 font-semibold uppercase text-[11px]">
                  <tr>
                    <th className="py-3 px-4">ID</th>
                    <th className="py-3 px-4">Asignatura</th>
                    <th className="py-3 px-4">Créditos</th>
                    <th className="py-3 px-4">Programa</th>
                    <th className="py-3 px-4">Contenido Temático (PDF)</th>
                    <th className="py-3 px-4 text-right">Acciones (Admin)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60 text-slate-300">
                  {subjects.map((sub) => (
                    <tr key={sub.id} className="hover:bg-slate-850/40 transition">
                      <td className="py-3 px-4 font-mono text-slate-400">#{sub.id}</td>
                      <td className="py-3 px-4 font-bold text-white">{sub.name}</td>
                      <td className="py-3 px-4">{sub.credits} créditos</td>
                      <td className="py-3 px-4 text-slate-400">{sub.program}</td>
                      <td className="py-3 px-4">
                        {sub.pdfUrl ? (
                          <div className="flex items-center space-x-2">
                            <a
                              href={sub.pdfUrl}
                              target="_blank"
                              rel="noreferrer"
                              className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border border-emerald-500/20 text-xs transition"
                              title="Descargar con SAS Token temporal"
                            >
                              <Download className="h-3 w-3 text-emerald-400" />
                              <span className="truncate max-w-[130px]">{sub.pdfOriginalName || 'Descargar PDF'}</span>
                            </a>
                            <button
                              onClick={() => handleSelectPdf(sub.id)}
                              className="p-1 text-slate-500 hover:text-slate-300 rounded transition cursor-pointer"
                              title="Reemplazar PDF"
                            >
                              <Upload className="h-3.5 w-3.5" />
                            </button>
                          </div>
                        ) : (
                          <button
                            onClick={() => handleSelectPdf(sub.id)}
                            className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 text-xs transition cursor-pointer"
                          >
                            <Upload className="h-3 w-3 text-slate-400" />
                            <span>Subir PDF (10MB)</span>
                          </button>
                        )}
                      </td>
                      <td className="py-3 px-4 text-right">
                        <button
                          onClick={() => handleDeleteSubject(sub.id, sub.name)}
                          className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition cursor-pointer"
                          title="Eliminar Materia"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

          </div>
        )}

        {/* TAB 3: AUDITORÍA DE CACHÉ (50 PETICIONES) */}
        {activeTab === 'benchmark' && (
          <div className="space-y-6">
            
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h3 className="font-bold text-white text-lg flex items-center space-x-2">
                    <Zap className="h-5 w-5 text-amber-400" />
                    <span>Medición de Rendimiento de Caché Redis (Sección 5.3)</span>
                  </h3>
                  <p className="text-xs text-slate-400 mt-1">
                    Ejecuta en vivo 50 peticiones consecutivas al catálogo para evidenciar latencias MISS vs HIT y calcular el Hit Ratio.
                  </p>
                </div>

                <button
                  onClick={runBenchmark}
                  disabled={runningBenchmark}
                  className="px-5 py-2.5 bg-amber-600 hover:bg-amber-500 disabled:opacity-50 text-slate-950 font-bold rounded-xl text-xs flex items-center space-x-2 shadow-lg shadow-amber-600/20 transition cursor-pointer shrink-0"
                >
                  <Play className="h-4 w-4 fill-slate-950" />
                  <span>{runningBenchmark ? `Midiendo (${benchmarkProgress}/50)...` : 'Ejecutar 50 Peticiones'}</span>
                </button>
              </div>

              {runningBenchmark && (
                <div className="space-y-2 pt-2">
                  <div className="flex justify-between text-xs text-slate-400">
                    <span>Progreso del Benchmark</span>
                    <span className="font-mono">{benchmarkProgress} / 50</span>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
                    <div
                      className="bg-amber-500 h-2.5 rounded-full transition-all duration-150"
                      style={{ width: `${(benchmarkProgress / 50) * 100}%` }}
                    ></div>
                  </div>
                </div>
              )}

              {benchmarkResults && (
                <div className="space-y-6 pt-4 animate-fadeIn">
                  
                  {/* Results cards */}
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                    <div className="p-4 rounded-xl bg-slate-950 border border-slate-800">
                      <span className="text-[11px] text-slate-400 uppercase font-semibold">Hit Ratio</span>
                      <div className="text-2xl font-extrabold text-emerald-400 mt-1 font-mono">
                        {benchmarkResults.hitRatio}%
                      </div>
                      <span className="text-[10px] text-slate-500">{benchmarkResults.hits} hits / 50 reqs</span>
                    </div>

                    <div className="p-4 rounded-xl bg-slate-950 border border-slate-800">
                      <span className="text-[11px] text-slate-400 uppercase font-semibold">Latencia MISS</span>
                      <div className="text-2xl font-extrabold text-amber-400 mt-1 font-mono">
                        {benchmarkResults.avgMiss} <span className="text-sm font-normal">ms</span>
                      </div>
                      <span className="text-[10px] text-slate-500">Base de datos PostgreSQL</span>
                    </div>

                    <div className="p-4 rounded-xl bg-slate-950 border border-slate-800">
                      <span className="text-[11px] text-slate-400 uppercase font-semibold">Latencia HIT</span>
                      <div className="text-2xl font-extrabold text-teal-400 mt-1 font-mono">
                        {benchmarkResults.avgHit} <span className="text-sm font-normal">ms</span>
                      </div>
                      <span className="text-[10px] text-slate-500">Redis en Memoria</span>
                    </div>

                    <div className="p-4 rounded-xl bg-slate-950 border border-slate-800">
                      <span className="text-[11px] text-slate-400 uppercase font-semibold">Aceleración</span>
                      <div className="text-2xl font-extrabold text-indigo-400 mt-1 font-mono">
                        {benchmarkResults.speedup}x
                      </div>
                      <span className="text-[10px] text-slate-500">Factor de mejora</span>
                    </div>
                  </div>

                  {/* Summary table */}
                  <div className="rounded-xl border border-slate-800 overflow-hidden bg-slate-950">
                    <div className="px-4 py-3 bg-slate-900 border-b border-slate-800 flex items-center justify-between text-xs">
                      <span className="font-semibold text-slate-300">Tabla de Resultados Oficial para el Informe:</span>
                      <button
                        onClick={copyBenchmarkMarkdown}
                        className="flex items-center space-x-1.5 px-3 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 transition text-[11px] cursor-pointer"
                      >
                        {copiedBenchmark ? <CheckCircle2 className="h-3.5 w-3.5 text-emerald-400" /> : <Copy className="h-3.5 w-3.5" />}
                        <span>{copiedBenchmark ? '¡Copiado!' : 'Copiar Tabla en Markdown'}</span>
                      </button>
                    </div>

                    <div className="p-4">
                      <table className="w-full text-left text-xs">
                        <tbody className="divide-y divide-slate-800 text-slate-300">
                          <tr>
                            <td className="py-2 text-slate-400">Total de Peticiones Ejecutadas</td>
                            <td className="py-2 font-mono font-bold">{benchmarkResults.total}</td>
                          </tr>
                          <tr>
                            <td className="py-2 text-slate-400">Porcentaje de Aciertos (Hit Ratio)</td>
                            <td className="py-2 font-mono font-bold text-emerald-400">{benchmarkResults.hitRatio}%</td>
                          </tr>
                          <tr>
                            <td className="py-2 text-slate-400">Latencia Promedio SIN Caché (MISS)</td>
                            <td className="py-2 font-mono text-amber-400">{benchmarkResults.avgMiss} ms</td>
                          </tr>
                          <tr>
                            <td className="py-2 text-slate-400">Latencia Promedio CON Caché (HIT)</td>
                            <td className="py-2 font-mono text-teal-400">{benchmarkResults.avgHit} ms</td>
                          </tr>
                          <tr>
                            <td className="py-2 text-slate-400">Factor de Aceleración (Speedup)</td>
                            <td className="py-2 font-mono font-bold text-indigo-400">{benchmarkResults.speedup}x más rápido</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>

                </div>
              )}
            </div>

          </div>
        )}

      </main>

      {/* Student Detail Modal */}
      <StudentDetailModal
        studentId={selectedStudentId}
        isOpen={!!selectedStudentId}
        onClose={() => setSelectedStudentId(null)}
        currentUser={currentUser}
      />

      {/* Modal Nueva Materia */}
      {showCreateSubjectModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fadeIn">
          <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 space-y-4">
            <h4 className="font-bold text-slate-100 text-base">Registrar Nueva Asignatura (Admin)</h4>
            <p className="text-xs text-slate-400">
              Al guardar la materia, el API Gateway invalidará automáticamente la versión de caché en Redis.
            </p>

            <form onSubmit={handleCreateSubject} className="space-y-3 pt-2">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Nombre de la Asignatura</label>
                <input
                  type="text"
                  required
                  value={newSubject.name}
                  onChange={(e) => setNewSubject({ ...newSubject, name: e.target.value })}
                  placeholder="ej: Computación en la Nube"
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-purple-500"
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
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-purple-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Programa</label>
                  <input
                    type="text"
                    required
                    value={newSubject.program}
                    onChange={(e) => setNewSubject({ ...newSubject, program: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-purple-500"
                  />
                </div>
              </div>

              <div className="pt-3 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowCreateSubjectModal(false)}
                  className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition cursor-pointer"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-xs font-semibold rounded-xl bg-purple-600 hover:bg-purple-500 text-white transition cursor-pointer"
                >
                  Crear e Invalidar Caché
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
