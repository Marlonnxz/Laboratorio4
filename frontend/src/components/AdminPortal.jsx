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
  RefreshCw,
  LogOut,
  User,
  CheckCircle2,
  AlertCircle,
  Play,
  Copy,
  Clock,
  Search,
  FileText,
  X
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
  const [studentSearch, setStudentSearch] = useState('');
  const photoInputRef = useRef(null);

  // Subjects state
  const [subjects, setSubjects] = useState([]);
  const [loadingSubjects, setLoadingSubjects] = useState(false);
  const [subjectNotice, setSubjectNotice] = useState(null);
  const [showCreateSubjectModal, setShowCreateSubjectModal] = useState(false);
  const [newSubject, setNewSubject] = useState({ name: '', credits: 3, program: 'Ingeniería de Sistemas y Computación' });
  const [uploadingSubjectId, setUploadingSubjectId] = useState(null);
  const [subjectSearch, setSubjectSearch] = useState('');
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
      setStudentNotice({ type: 'error', message: err.message || 'Error al obtener la lista de estudiantes.' });
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
      setSubjectNotice({ type: 'error', message: err.message || 'Error al obtener el catálogo de materias.' });
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
      alert('Error: La imagen no puede superar 2 MB.');
      return;
    }
    if (!file.type.match(/^image\/(jpeg|png|jpg)$/i)) {
      alert('Error: Solo se permiten imágenes en formato JPG o PNG.');
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
        message: 'Fotografía actualizada exitosamente para el estudiante.',
      });
      setTimeout(() => setStudentNotice(null), 4000);
      fetchStudents();
    } catch (err) {
      alert(`Error al cargar fotografía: ${err.message}`);
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
        message: 'Asignatura registrada correctamente en el catálogo.',
      });
      setTimeout(() => setSubjectNotice(null), 4000);
      fetchSubjects();
    } catch (err) {
      alert(`Error al registrar asignatura: ${err.message}`);
    }
  };

  const handleDeleteSubject = async (id, name) => {
    if (!confirm(`¿Está seguro de eliminar la asignatura "${name}"?`)) return;
    try {
      await apiRequest(`/api/subjects/${id}`, { method: 'DELETE' });
      setSubjectNotice({
        type: 'success',
        message: 'Asignatura eliminada correctamente del catálogo.',
      });
      setTimeout(() => setSubjectNotice(null), 4000);
      fetchSubjects();
    } catch (err) {
      alert(`Error al eliminar asignatura: ${err.message}`);
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
      alert('Error: El archivo no puede superar 10 MB.');
      return;
    }
    if (file.type !== 'application/pdf' && !file.name.endsWith('.pdf')) {
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
        message: `El archivo "${file.name}" se cargó exitosamente como syllabus oficial.`,
      });
      setTimeout(() => setSubjectNotice(null), 4000);
      fetchSubjects();
    } catch (err) {
      alert(`Error al cargar el PDF: ${err.message}`);
    } finally {
      setUploadingSubjectId(null);
    }
  };

  // --- Benchmark Execution ---
  const runCacheBenchmark = async () => {
    setRunningBenchmark(true);
    setBenchmarkProgress(0);
    setBenchmarkResults(null);

    const TOTAL = 50;
    const records = [];

    for (let i = 1; i <= TOTAL; i++) {
      try {
        const res = await apiRequest('/api/subjects?pageSize=50');
        records.push({
          iteration: i,
          durationMs: res.durationMs,
          cacheHeader: res.cacheHeader || 'MISS',
          status: res.status,
        });
      } catch (err) {
        records.push({
          iteration: i,
          durationMs: 0,
          cacheHeader: 'ERR',
          status: err.status || 500,
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
    const md = `| Métrica de Rendimiento | Resultado Obtenido |
| :--- | :--- |
| Peticiones Totales Evaluadas | ${benchmarkResults.total} |
| Tasa de Aciertos (Hit Ratio) | **${benchmarkResults.hitRatio}%** |
| Latencia Promedio BD (Sin Caché) | ${benchmarkResults.avgMiss} ms |
| Latencia Promedio Memoria (Con Caché) | ${benchmarkResults.avgHit} ms |
| Factor de Aceleración | **${benchmarkResults.speedup}x más rápido** |
`;
    navigator.clipboard.writeText(md);
    setCopiedBenchmark(true);
    setTimeout(() => setCopiedBenchmark(false), 2000);
  };

  const filteredStudents = students.filter((st) =>
    `${st.firstName} ${st.lastName}`.toLowerCase().includes(studentSearch.toLowerCase()) ||
    (st.email || '').toLowerCase().includes(studentSearch.toLowerCase())
  );

  const filteredSubjects = subjects.filter((sb) =>
    (sb.name || '').toLowerCase().includes(subjectSearch.toLowerCase()) ||
    (sb.program || '').toLowerCase().includes(subjectSearch.toLowerCase())
  );

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      
      {/* Hidden file inputs */}
      <input type="file" ref={photoInputRef} accept="image/png, image/jpeg, image/jpg" onChange={handlePhotoUpload} className="hidden" />
      <input type="file" ref={pdfInputRef} accept="application/pdf,.pdf" onChange={handlePdfUpload} className="hidden" />

      {/* Top Header */}
      <header className="border-b border-slate-800 bg-slate-900/90 backdrop-blur-xl sticky top-0 z-30 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
          
          <div className="flex items-center space-x-3">
            <div className="h-10 w-10 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400 flex items-center justify-center font-bold">
              <Shield className="h-6 w-6" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-bold text-white text-base tracking-tight">Portal Administrativo</span>
                <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-purple-500/10 text-purple-300 border border-purple-500/20">
                  UPTC Sogamoso
                </span>
              </div>
              <p className="text-[11px] text-slate-400">Sistema de Gestión Académica</p>
            </div>
          </div>

          <div className="flex items-center space-x-4">
            <div className="text-right hidden sm:block">
              <div className="text-xs font-semibold text-slate-200">
                {currentUser.fullName || currentUser.username}
              </div>
              <div className="text-[11px] text-purple-400 font-medium">
                Administrador del Sistema
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
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
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
              <span>Directorio de Estudiantes</span>
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
              <span>Catálogo de Asignaturas</span>
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
              <span>Rendimiento del Sistema</span>
            </button>
          </div>

          {activeTab !== 'benchmark' && (
            <button
              onClick={activeTab === 'students' ? fetchStudents : fetchSubjects}
              className="p-2 text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800 rounded-xl transition cursor-pointer"
              title="Actualizar datos"
            >
              <RefreshCw className={`h-4 w-4 ${loadingStudents || loadingSubjects ? 'animate-spin text-purple-400' : ''}`} />
            </button>
          )}
        </div>

        {/* TAB 1: ESTUDIANTES */}
        {activeTab === 'students' && (
          <div className="space-y-4">
            
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <h3 className="font-bold text-white text-base">Directorio General de Estudiantes</h3>
                <p className="text-xs text-slate-400">
                  Gestión integral de expedientes académicos, fotografías institucionales y consulta de información consolidada.
                </p>
              </div>

              {/* Student search */}
              <div className="relative w-full sm:w-64">
                <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
                <input
                  type="text"
                  value={studentSearch}
                  onChange={(e) => setStudentSearch(e.target.value)}
                  placeholder="Buscar estudiante..."
                  className="w-full pl-9 pr-3 py-1.5 bg-slate-900 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-purple-500 transition placeholder:text-slate-600"
                />
              </div>
            </div>

            {studentNotice && (
              <div className="p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs flex items-center space-x-2 animate-fadeIn">
                <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                <span>{studentNotice.message}</span>
              </div>
            )}

            {loadingStudents ? (
              <div className="py-20 text-center text-slate-400 bg-slate-900/50 rounded-2xl border border-slate-800">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500 mx-auto mb-3"></div>
                <p className="text-sm font-medium">Cargando directorio de estudiantes...</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {filteredStudents.map((student) => (
                  <div
                    key={student.id}
                    className="bg-slate-900 rounded-2xl p-5 border border-slate-800 hover:border-slate-700 transition flex flex-col justify-between space-y-4 shadow-sm"
                  >
                    <div>
                      <div className="flex items-start justify-between gap-3 mb-3">
                        <div className="relative">
                          {student.photoUrl ? (
                            <img
                              src={student.photoUrl}
                              alt={student.firstName}
                              className="w-14 h-14 rounded-2xl object-cover border-2 border-purple-500/40 shadow"
                            />
                          ) : (
                            <div className="w-14 h-14 rounded-2xl bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-400">
                              <User className="h-7 w-7" />
                            </div>
                          )}
                        </div>

                        <span className="text-[11px] font-mono px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                          Código #{student.id}
                        </span>
                      </div>

                      <h4 className="font-bold text-white text-base">
                        {student.firstName} {student.lastName}
                      </h4>
                      <p className="text-xs text-slate-400 font-mono mt-0.5 truncate">{student.email}</p>
                      <p className="text-xs text-slate-300 mt-2">
                        Ingeniería de Sistemas y Computación
                      </p>
                    </div>

                    <div className="pt-3 border-t border-slate-800/80 flex items-center gap-2">
                      <button
                        onClick={() => setSelectedStudentId(student.id)}
                        className="flex-1 py-2 px-3 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-200 text-xs font-semibold transition cursor-pointer flex items-center justify-center space-x-1.5"
                      >
                        <span>Ver Ficha Académica</span>
                      </button>

                      <button
                        onClick={() => handleSelectPhoto(student.id)}
                        className="py-2 px-3 rounded-xl bg-purple-600/20 hover:bg-purple-600/30 text-purple-300 border border-purple-500/30 text-xs font-semibold transition cursor-pointer flex items-center space-x-1.5"
                        title="Cargar fotografía institucional (JPG o PNG hasta 2 MB)"
                      >
                        <Upload className="h-3.5 w-3.5" />
                        <span>Foto</span>
                      </button>
                    </div>

                  </div>
                ))}
              </div>
            )}

          </div>
        )}

        {/* TAB 2: MATERIAS */}
        {activeTab === 'subjects' && (
          <div className="space-y-4">
            
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <h3 className="font-bold text-white text-base">Catálogo Oficial de Asignaturas</h3>
                <p className="text-xs text-slate-400">
                  Gestión curricular de materias, créditos académicos y programas temáticos oficiales.
                </p>
              </div>

              <div className="flex items-center space-x-2">
                <div className="relative w-full sm:w-60">
                  <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
                  <input
                    type="text"
                    value={subjectSearch}
                    onChange={(e) => setSubjectSearch(e.target.value)}
                    placeholder="Buscar materia..."
                    className="w-full pl-9 pr-3 py-1.5 bg-slate-900 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-purple-500 transition placeholder:text-slate-600"
                  />
                </div>

                <button
                  onClick={() => setShowCreateSubjectModal(true)}
                  className="px-3.5 py-1.5 rounded-xl bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold shadow-md shadow-purple-600/20 flex items-center space-x-1.5 transition cursor-pointer shrink-0"
                >
                  <Plus className="h-4 w-4" />
                  <span>Nueva Asignatura</span>
                </button>
              </div>
            </div>

            {subjectNotice && (
              <div className="p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs flex items-center space-x-2 animate-fadeIn">
                <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                <span>{subjectNotice.message}</span>
              </div>
            )}

            {loadingSubjects ? (
              <div className="py-20 text-center text-slate-400 bg-slate-900/50 rounded-2xl border border-slate-800">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500 mx-auto mb-3"></div>
                <p className="text-sm font-medium">Cargando catálogo de asignaturas...</p>
              </div>
            ) : (
              <div className="bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-950 border-b border-slate-800 text-slate-400 font-semibold uppercase text-[11px]">
                    <tr>
                      <th className="py-3.5 px-4">Código</th>
                      <th className="py-3.5 px-4">Asignatura</th>
                      <th className="py-3.5 px-4">Créditos</th>
                      <th className="py-3.5 px-4">Programa Académico</th>
                      <th className="py-3.5 px-4">Programa Temático (PDF)</th>
                      <th className="py-3.5 px-4 text-right">Acciones</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60 text-slate-300">
                    {filteredSubjects.length === 0 ? (
                      <tr>
                        <td colSpan={6} className="py-8 text-center text-slate-500">
                          No se encontraron asignaturas registradas.
                        </td>
                      </tr>
                    ) : (
                      filteredSubjects.map((sub) => (
                        <tr key={sub.id} className="hover:bg-slate-850/40 transition">
                          <td className="py-3 px-4 font-mono text-slate-400">#{sub.id}</td>
                          <td className="py-3 px-4 font-bold text-white">{sub.name}</td>
                          <td className="py-3 px-4">{sub.credits} créditos</td>
                          <td className="py-3 px-4 text-slate-400">{sub.program}</td>
                          <td className="py-3 px-4">
                            <div className="flex items-center space-x-2">
                              {sub.pdfUrl ? (
                                <a
                                  href={sub.pdfUrl}
                                  target="_blank"
                                  rel="noreferrer"
                                  className="inline-flex items-center space-x-1.5 px-2.5 py-1 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border border-emerald-500/20 text-xs font-medium transition cursor-pointer"
                                >
                                  <Download className="h-3 w-3 text-emerald-400" />
                                  <span>Descargar PDF</span>
                                </a>
                              ) : (
                                <span className="text-slate-500 italic text-[11px]">Sin PDF adjunto</span>
                              )}

                              <button
                                onClick={() => handleSelectPdf(sub.id)}
                                className="px-2 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs transition cursor-pointer flex items-center space-x-1"
                                title="Cargar archivo PDF (máximo 10 MB)"
                              >
                                <Upload className="h-3 w-3" />
                                <span>{sub.pdfUrl ? 'Cambiar' : 'Cargar'}</span>
                              </button>
                            </div>
                          </td>
                          <td className="py-3 px-4 text-right">
                            <button
                              onClick={() => handleDeleteSubject(sub.id, sub.name)}
                              className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition cursor-pointer"
                              title="Eliminar Asignatura"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            )}

          </div>
        )}

        {/* TAB 3: RENDIMIENTO DEL SISTEMA */}
        {activeTab === 'benchmark' && (
          <div className="space-y-6">
            
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-white text-base">Evaluación de Rendimiento y Aceleración en Memoria</h3>
                <p className="text-xs text-slate-400">
                  Medición de latencia y desempeño de la capa de aceleración en memoria (Redis Cache).
                </p>
              </div>

              {benchmarkResults && (
                <button
                  onClick={copyBenchmarkMarkdown}
                  className="px-3.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold flex items-center space-x-1.5 transition cursor-pointer"
                >
                  <Copy className="h-3.5 w-3.5" />
                  <span>{copiedBenchmark ? '¡Copiado!' : 'Copiar Reporte'}</span>
                </button>
              )}
            </div>

            {/* Test Launcher Card */}
            <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4 shadow-sm">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h4 className="font-bold text-white text-sm">Prueba de Carga Concurrente (50 Peticiones Consecutivas)</h4>
                  <p className="text-xs text-slate-400 mt-0.5">
                    Envía 50 consultas consecutivas al catálogo para verificar la tasa de aciertos (Hit Ratio $\ge$ 95%) y la reducción de latencia.
                  </p>
                </div>

                <button
                  onClick={runCacheBenchmark}
                  disabled={runningBenchmark}
                  className="px-5 py-2.5 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 disabled:opacity-50 text-slate-950 font-bold rounded-xl text-xs shadow-lg shadow-amber-500/20 flex items-center justify-center space-x-2 transition cursor-pointer shrink-0"
                >
                  {runningBenchmark ? (
                    <>
                      <div className="animate-spin rounded-full h-3.5 w-3.5 border-b-2 border-slate-950"></div>
                      <span>Ejecutando ({benchmarkProgress}/50)...</span>
                    </>
                  ) : (
                    <>
                      <Play className="h-3.5 w-3.5 fill-current" />
                      <span>Ejecutar Diagnóstico</span>
                    </>
                  )}
                </button>
              </div>

              {runningBenchmark && (
                <div className="w-full bg-slate-950 rounded-full h-2 overflow-hidden border border-slate-800">
                  <div
                    className="bg-amber-400 h-2 transition-all duration-150"
                    style={{ width: `${(benchmarkProgress / 50) * 100}%` }}
                  ></div>
                </div>
              )}
            </div>

            {/* Benchmark KPIs */}
            {benchmarkResults && (
              <div className="space-y-6 animate-fadeIn">
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-1">
                    <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Hit Ratio</span>
                    <span className="text-2xl font-black text-emerald-400 block">{benchmarkResults.hitRatio}%</span>
                    <span className="text-[10px] text-slate-500">{benchmarkResults.hits} de {benchmarkResults.total} aciertos en memoria</span>
                  </div>

                  <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-1">
                    <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Latencia en Memoria</span>
                    <span className="text-2xl font-black text-white block">{benchmarkResults.avgHit} ms</span>
                    <span className="text-[10px] text-slate-500">Tiempo promedio con aceleración</span>
                  </div>

                  <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-1">
                    <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Latencia Base de Datos</span>
                    <span className="text-2xl font-black text-amber-400 block">{benchmarkResults.avgMiss} ms</span>
                    <span className="text-[10px] text-slate-500">Consulta directa sin memoria</span>
                  </div>

                  <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-1">
                    <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Factor de Aceleración</span>
                    <span className="text-2xl font-black text-purple-400 block">{benchmarkResults.speedup}x</span>
                    <span className="text-[10px] text-slate-500">Multiplicador de velocidad</span>
                  </div>
                </div>

                {/* Detailed requests table */}
                <div className="bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden shadow-xl max-h-72 overflow-y-auto">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-slate-950 border-b border-slate-800 text-slate-400 font-semibold uppercase text-[11px] sticky top-0">
                      <tr>
                        <th className="py-2.5 px-4"># Petición</th>
                        <th className="py-2.5 px-4">Estado Caché</th>
                        <th className="py-2.5 px-4">Tiempo de Respuesta</th>
                        <th className="py-2.5 px-4 text-right">Código HTTP</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-800/60 text-slate-300">
                      {benchmarkResults.records.map((rec) => (
                        <tr key={rec.iteration} className="hover:bg-slate-850/40">
                          <td className="py-2 px-4 font-mono text-slate-400">Petición #{rec.iteration}</td>
                          <td className="py-2 px-4">
                            <span
                              className={`font-mono font-bold text-[11px] px-2 py-0.5 rounded ${
                                rec.cacheHeader === 'HIT'
                                  ? 'bg-emerald-500/10 text-emerald-300 border border-emerald-500/20'
                                  : 'bg-amber-500/10 text-amber-300 border border-amber-500/20'
                              }`}
                            >
                              {rec.cacheHeader}
                            </span>
                          </td>
                          <td className="py-2 px-4 font-mono">{rec.durationMs} ms</td>
                          <td className="py-2 px-4 text-right font-mono text-slate-400">{rec.status}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

              </div>
            )}

          </div>
        )}

      </main>

      {/* Modal: Registrar Asignatura */}
      {showCreateSubjectModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fadeIn">
          <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800 mb-4">
              <h4 className="font-bold text-white text-base">Registrar Nueva Asignatura</h4>
              <button
                onClick={() => setShowCreateSubjectModal(false)}
                className="p-1 text-slate-400 hover:text-slate-200 transition"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={handleCreateSubject} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Nombre de la Asignatura
                </label>
                <input
                  type="text"
                  required
                  value={newSubject.name}
                  onChange={(e) => setNewSubject({ ...newSubject, name: e.target.value })}
                  placeholder="ej: Arquitectura de Software"
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-purple-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Número de Créditos Académicos
                </label>
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
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Programa Académico
                </label>
                <input
                  type="text"
                  required
                  value={newSubject.program}
                  onChange={(e) => setNewSubject({ ...newSubject, program: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-purple-500"
                />
              </div>

              <div className="pt-2 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowCreateSubjectModal(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl transition"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-purple-600/20 transition cursor-pointer"
                >
                  Guardar Asignatura
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Student Detail Modal */}
      <StudentDetailModal
        studentId={selectedStudentId}
        isOpen={!!selectedStudentId}
        onClose={() => setSelectedStudentId(null)}
        currentUser={currentUser}
      />

      <footer className="border-t border-slate-800 py-4 text-center text-[11px] text-slate-500">
        © 2026 Universidad Pedagógica y Tecnológica de Colombia (UPTC) • Sistema de Gestión Académica
      </footer>

    </div>
  );
}
