import React, { useState, useEffect } from 'react';
import {
  GraduationCap,
  BookOpen,
  LogOut,
  User,
  Download,
  ShieldAlert,
  CheckCircle2,
  AlertTriangle,
  Award,
  Layers,
  Clock,
  Zap,
  RefreshCw,
  FileText
} from 'lucide-react';
import { apiRequest } from '../api/client';

export default function StudentPortal({ currentUser, onLogout }) {
  const [activeTab, setActiveTab] = useState('detail'); // 'detail' | 'subjects'
  
  // Student Detail State
  const [detail, setDetail] = useState(null);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [detailError, setDetailError] = useState(null);

  // Subjects State
  const [subjects, setSubjects] = useState([]);
  const [loadingSubjects, setLoadingSubjects] = useState(false);
  const [subjectsError, setSubjectsError] = useState(null);
  const [lastCacheStatus, setLastCacheStatus] = useState(null);
  const [lastLatency, setLastLatency] = useState(null);

  // RBAC Demonstration modal/alert state
  const [rbacTestResult, setRbacTestResult] = useState(null);

  const myStudentId = currentUser.studentId || 1;

  useEffect(() => {
    fetchMyDetail();
  }, [myStudentId]);

  useEffect(() => {
    if (activeTab === 'subjects') {
      fetchSubjects();
    }
  }, [activeTab]);

  const fetchMyDetail = async () => {
    setLoadingDetail(true);
    setDetailError(null);
    try {
      const res = await apiRequest(`/api/students/${myStudentId}/detail`);
      setDetail(res.data);
    } catch (err) {
      setDetailError(err.message);
    } finally {
      setLoadingDetail(false);
    }
  };

  const fetchSubjects = async () => {
    setLoadingSubjects(true);
    setSubjectsError(null);
    try {
      const res = await apiRequest('/api/subjects?pageSize=50');
      const list = res.data?.content || (Array.isArray(res.data) ? res.data : []);
      setSubjects(list);
      setLastCacheStatus(res.cacheHeader);
      setLastLatency(res.durationMs);
    } catch (err) {
      setSubjectsError(err.message);
    } finally {
      setLoadingSubjects(false);
    }
  };

  // RBAC Test 1: Student tries to create a subject -> Expect 403
  const handleTryCreateSubject = async () => {
    setRbacTestResult(null);
    try {
      await apiRequest('/api/subjects', {
        method: 'POST',
        body: JSON.stringify({
          name: 'Materia No Autorizada',
          credits: 3,
          program: 'Ingeniería de Sistemas',
        }),
      });
      setRbacTestResult({
        success: false,
        title: 'Fallo en la prueba de seguridad',
        message: 'La materia fue creada cuando debió haber sido rechazada por el Gateway.',
      });
    } catch (err) {
      setRbacTestResult({
        success: true,
        status: err.status,
        title: 'Demostración de Permiso: HTTP 403 Forbidden',
        message: err.message,
        explanation: 'El API Gateway interceptó la petición antes de enrutarla y validó que el rol ESTUDIANTE no tiene permiso para crear o modificar materias en el catálogo.',
      });
    }
  };

  // RBAC Test 2: Student tries to view another student's detail -> Expect 403
  const handleTryOtherStudentDetail = async () => {
    setRbacTestResult(null);
    const otherId = myStudentId === 1 ? 2 : 1;
    try {
      await apiRequest(`/api/students/${otherId}/detail`);
      setRbacTestResult({
        success: false,
        title: 'Fallo en la prueba de seguridad',
        message: `El estudiante pudo ver el detalle del estudiante #${otherId}.`,
      });
    } catch (err) {
      setRbacTestResult({
        success: true,
        status: err.status,
        title: 'Demostración de Permiso: HTTP 403 Forbidden',
        message: err.message,
        explanation: `El API Gateway verificó el token y constató que el estudiante #${myStudentId} no está autorizado para acceder a la ficha académica del estudiante #${otherId}.`,
      });
    }
  };

  const student = detail?.student;
  const enrollments = detail?.enrollments || [];

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      
      {/* Top Header for Student */}
      <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur-xl sticky top-0 z-30">
        <div className="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="h-9 w-9 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold">
              <GraduationCap className="h-5 w-5" />
            </div>
            <div>
              <span className="font-extrabold text-white text-base tracking-tight">Portal Estudiantil</span>
              <span className="hidden sm:inline-block ml-2 text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                UPTC Sogamoso
              </span>
            </div>
          </div>

          {/* User profile & Logout */}
          <div className="flex items-center space-x-3">
            <div className="text-right">
              <div className="text-xs font-semibold text-slate-200">
                {currentUser.fullName || currentUser.username}
              </div>
              <div className="text-[10px] text-emerald-400 font-mono">
                Rol: ESTUDIANTE (ID #{myStudentId})
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
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 py-6 space-y-6">
        
        {/* Navigation Tabs */}
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-3">
          <div className="flex space-x-2 bg-slate-900 p-1 rounded-xl border border-slate-800">
            <button
              onClick={() => setActiveTab('detail')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-xs font-semibold transition cursor-pointer ${
                activeTab === 'detail'
                  ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <User className="h-4 w-4" />
              <span>Mi Ficha Académica (/detalle)</span>
            </button>

            <button
              onClick={() => setActiveTab('subjects')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-xs font-semibold transition cursor-pointer ${
                activeTab === 'subjects'
                  ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <BookOpen className="h-4 w-4" />
              <span>Catálogo de Asignaturas</span>
            </button>
          </div>

          {/* Quick RBAC Demonstration Test Buttons for Oral Defense */}
          <div className="flex items-center space-x-2">
            <button
              onClick={handleTryCreateSubject}
              className="px-3 py-1.5 text-xs font-semibold rounded-lg bg-amber-500/10 text-amber-300 border border-amber-500/20 hover:bg-amber-500/20 transition cursor-pointer flex items-center space-x-1"
              title="Prueba exigida en la sustentación: demostrar que un estudiante no puede crear materias (HTTP 403)"
            >
              <ShieldAlert className="h-3.5 w-3.5 text-amber-400" />
              <span>Demostrar 403 (Crear Materia)</span>
            </button>

            <button
              onClick={handleTryOtherStudentDetail}
              className="px-3 py-1.5 text-xs font-semibold rounded-lg bg-rose-500/10 text-rose-300 border border-rose-500/20 hover:bg-rose-500/20 transition cursor-pointer flex items-center space-x-1"
              title="Prueba exigida en la sustentación: demostrar que un estudiante no puede ver el /detalle de otro (HTTP 403)"
            >
              <ShieldAlert className="h-3.5 w-3.5 text-rose-400" />
              <span>Demostrar 403 (Otro /detalle)</span>
            </button>
          </div>
        </div>

        {/* RBAC Demonstration Notification */}
        {rbacTestResult && (
          <div className="p-4 rounded-xl bg-slate-900 border border-slate-700 space-y-2 animate-fadeIn">
            <div className="flex items-center space-x-2 text-emerald-400 font-bold text-sm">
              <CheckCircle2 className="h-5 w-5" />
              <span>{rbacTestResult.title}</span>
            </div>
            <div className="text-xs text-rose-300 font-mono bg-rose-950/40 p-2 rounded border border-rose-900/60">
              {rbacTestResult.message}
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              💡 {rbacTestResult.explanation}
            </p>
          </div>
        )}

        {/* TAB 1: MI FICHA ACADÉMICA (/detalle) */}
        {activeTab === 'detail' && (
          <div className="space-y-6">
            {loadingDetail && (
              <div className="py-16 text-center text-slate-400">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500 mx-auto mb-3"></div>
                <p className="text-sm">Consultando ficha académica consolidada en el Gateway...</p>
              </div>
            )}

            {detailError && (
              <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs">
                {detailError}
              </div>
            )}

            {!loadingDetail && student && (
              <div className="space-y-6">
                
                {/* Profile Card */}
                <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl flex flex-col sm:flex-row items-center sm:items-start gap-6">
                  
                  {/* Photo from Azure Blob Storage */}
                  <div className="relative shrink-0">
                    {student.photoUrl ? (
                      <img
                        src={student.photoUrl}
                        alt={student.firstName}
                        className="w-28 h-28 rounded-2xl object-cover border-2 border-emerald-500/40 shadow-lg shadow-emerald-500/10"
                      />
                    ) : (
                      <div className="w-28 h-28 rounded-2xl bg-slate-800 border-2 border-slate-700 flex items-center justify-center text-slate-500">
                        <User className="h-14 w-14" />
                      </div>
                    )}
                    {student.photoUrl && (
                      <span className="absolute -bottom-2 -right-2 px-2 py-0.5 rounded-full bg-emerald-500 text-slate-950 font-bold text-[10px] shadow">
                        Azure Blob
                      </span>
                    )}
                  </div>

                  <div className="flex-1 text-center sm:text-left space-y-1">
                    <div className="inline-block text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700 mb-1">
                      Código Estudiantil: #{student.id}
                    </div>
                    <h2 className="text-2xl font-bold text-white">
                      {student.firstName} {student.lastName}
                    </h2>
                    <p className="text-xs text-slate-400 font-mono">{student.email}</p>
                    <p className="text-xs text-emerald-400 font-medium pt-1">
                      Programa Académico: Ingeniería de Sistemas y Computación
                    </p>
                  </div>
                </div>

                {/* Enrolled Courses */}
                <div className="space-y-3">
                  <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 flex items-center space-x-2">
                    <BookOpen className="h-4 w-4 text-emerald-400" />
                    <span>Asignaturas Matriculadas ({enrollments.length})</span>
                  </h3>

                  {enrollments.length === 0 ? (
                    <div className="p-8 rounded-2xl bg-slate-900 border border-slate-800 text-center text-xs text-slate-400">
                      No registras asignaturas inscritas en el periodo actual.
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {enrollments.map((enr, idx) => (
                        <div
                          key={idx}
                          className="p-4 rounded-xl bg-slate-900 border border-slate-800 hover:border-slate-700 transition space-y-3"
                        >
                          <div className="flex items-start justify-between">
                            <div>
                              <h4 className="font-bold text-slate-100 text-sm">
                                {enr.course?.name || enr.courseName || `Curso #${enr.courseId || enr.id}`}
                              </h4>
                              <p className="text-xs text-slate-400 mt-0.5">
                                Docente: <span className="text-slate-300 font-medium">{enr.teacher?.name || enr.teacherName || 'Docente UPTC'}</span>
                              </p>
                            </div>
                            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                              ACTIVO
                            </span>
                          </div>

                          {enr.grades && enr.grades.length > 0 && (
                            <div className="pt-2 border-t border-slate-800 flex items-center gap-2 text-xs">
                              <Award className="h-4 w-4 text-amber-400 shrink-0" />
                              <span className="text-slate-400">Calificaciones:</span>
                              <div className="flex gap-1.5">
                                {enr.grades.map((g, gIdx) => (
                                  <span
                                    key={gIdx}
                                    className="px-2 py-0.5 rounded bg-slate-800 text-slate-200 font-mono text-[11px]"
                                  >
                                    {g.gradeValue || g.value || g}
                                  </span>
                                ))}
                              </div>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>

              </div>
            )}
          </div>
        )}

        {/* TAB 2: CATÁLOGO DE ASIGNATURAS (Lectura y Descarga PDF) */}
        {activeTab === 'subjects' && (
          <div className="space-y-4">
            
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-white text-base">Catálogo Institucional de Asignaturas</h3>
                <p className="text-xs text-slate-400">Descarga los planes temáticos en formato PDF</p>
              </div>

              {lastCacheStatus && (
                <div className="flex items-center space-x-2 px-3 py-1.5 rounded-xl bg-slate-900 border border-slate-800 text-xs">
                  <span className="text-slate-400">Respuesta Caché:</span>
                  <span
                    className={`font-mono font-bold px-2 py-0.5 rounded text-[11px] ${
                      lastCacheStatus === 'HIT'
                        ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                        : 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                    }`}
                  >
                    X-Cache: {lastCacheStatus}
                  </span>
                  <span className="font-mono text-slate-400 text-[11px]">{lastLatency} ms</span>
                </div>
              )}
            </div>

            <div className="bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-950 border-b border-slate-800 text-slate-400 font-semibold uppercase text-[11px]">
                  <tr>
                    <th className="py-3 px-4">ID</th>
                    <th className="py-3 px-4">Asignatura</th>
                    <th className="py-3 px-4">Créditos</th>
                    <th className="py-3 px-4">Programa</th>
                    <th className="py-3 px-4 text-right">Contenido Temático (PDF)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60 text-slate-300">
                  {subjects.map((sub) => (
                    <tr key={sub.id} className="hover:bg-slate-850/40 transition">
                      <td className="py-3 px-4 font-mono text-slate-400">#{sub.id}</td>
                      <td className="py-3 px-4 font-bold text-white">{sub.name}</td>
                      <td className="py-3 px-4">{sub.credits} créditos</td>
                      <td className="py-3 px-4 text-slate-400">{sub.program}</td>
                      <td className="py-3 px-4 text-right">
                        {sub.pdfUrl ? (
                          <a
                            href={sub.pdfUrl}
                            target="_blank"
                            rel="noreferrer"
                            className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border border-emerald-500/20 text-xs font-medium transition cursor-pointer"
                            title="Descargar PDF con URL temporal firmada (SAS Token)"
                          >
                            <Download className="h-3.5 w-3.5 text-emerald-400" />
                            <span>Descargar PDF</span>
                          </a>
                        ) : (
                          <span className="text-slate-500 italic text-[11px]">Sin PDF adjunto</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

          </div>
        )}

      </main>

    </div>
  );
}
