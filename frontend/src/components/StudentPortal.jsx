import React, { useState, useEffect } from 'react';
import {
  GraduationCap,
  BookOpen,
  LogOut,
  User,
  Download,
  Award,
  Calendar,
  Clock,
  RefreshCw,
  Search,
  CheckCircle,
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
  const [searchTerm, setSearchTerm] = useState('');

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
      setDetailError(err.message || 'No fue posible cargar el expediente académico.');
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
    } catch (err) {
      setSubjectsError(err.message || 'No fue posible consultar el catálogo de asignaturas.');
    } finally {
      setLoadingSubjects(false);
    }
  };

  const student = detail?.student;
  const enrollments = detail?.enrollments || [];

  const filteredSubjects = subjects.filter((s) =>
    (s.name || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
    (s.program || '').toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      
      {/* Top Header */}
      <header className="border-b border-slate-800 bg-slate-900/90 backdrop-blur-xl sticky top-0 z-30 shadow-sm">
        <div className="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
          
          <div className="flex items-center space-x-3">
            <div className="h-10 w-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold">
              <GraduationCap className="h-6 w-6" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-bold text-white text-base tracking-tight">Portal Estudiantil</span>
                <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  UPTC Sogamoso
                </span>
              </div>
              <p className="text-[11px] text-slate-400">Sistema de Gestión Académica</p>
            </div>
          </div>

          {/* User profile & Logout */}
          <div className="flex items-center space-x-4">
            <div className="text-right hidden sm:block">
              <div className="text-xs font-semibold text-slate-200">
                {currentUser.fullName || currentUser.username}
              </div>
              <div className="text-[11px] text-slate-400">
                Estudiante • Código #{myStudentId}
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
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
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
              <span>Mi Ficha Académica</span>
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

          <button
            onClick={activeTab === 'detail' ? fetchMyDetail : fetchSubjects}
            className="p-2 text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800 rounded-xl transition cursor-pointer"
            title="Actualizar datos"
          >
            <RefreshCw className={`h-4 w-4 ${loadingDetail || loadingSubjects ? 'animate-spin text-emerald-400' : ''}`} />
          </button>
        </div>

        {/* TAB 1: MI FICHA ACADÉMICA */}
        {activeTab === 'detail' && (
          <div className="space-y-6">
            
            {loadingDetail && (
              <div className="py-20 text-center text-slate-400 bg-slate-900/50 rounded-2xl border border-slate-800">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500 mx-auto mb-3"></div>
                <p className="text-sm font-medium">Cargando expediente académico...</p>
              </div>
            )}

            {detailError && (
              <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs">
                {detailError}
              </div>
            )}

            {!loadingDetail && student && (
              <div className="space-y-6 animate-fadeIn">
                
                {/* Profile Card */}
                <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl flex flex-col sm:flex-row items-center sm:items-start gap-6">
                  
                  {/* Photo */}
                  <div className="relative shrink-0">
                    {student.photoUrl ? (
                      <img
                        src={student.photoUrl}
                        alt={`${student.firstName} ${student.lastName}`}
                        className="w-28 h-28 rounded-2xl object-cover border-2 border-emerald-500/40 shadow-lg shadow-emerald-500/10"
                      />
                    ) : (
                      <div className="w-28 h-28 rounded-2xl bg-slate-800 border-2 border-slate-700 flex items-center justify-center text-slate-500">
                        <User className="h-14 w-14" />
                      </div>
                    )}
                  </div>

                  <div className="flex-1 text-center sm:text-left space-y-1">
                    <div className="inline-block text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 mb-1">
                      Código Estudiantil: #{student.id}
                    </div>
                    <h2 className="text-2xl font-bold text-white tracking-tight">
                      {student.firstName} {student.lastName}
                    </h2>
                    <p className="text-xs text-slate-400 font-mono">{student.email}</p>
                    <p className="text-xs text-slate-300 font-medium pt-1">
                      Programa Académico: <span className="text-emerald-400">Ingeniería de Sistemas y Computación</span>
                    </p>
                  </div>
                </div>

                {/* Enrolled Courses */}
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 flex items-center space-x-2">
                      <BookOpen className="h-4 w-4 text-emerald-400" />
                      <span>Asignaturas Inscritas ({enrollments.length})</span>
                    </h3>
                  </div>

                  {enrollments.length === 0 ? (
                    <div className="p-8 rounded-2xl bg-slate-900 border border-slate-800 text-center text-xs text-slate-400">
                      No registras asignaturas inscritas en el periodo actual.
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {enrollments.map((enr) => {
                        const courseName = enr.course?.name || enr.courseName || `Curso #${enr.courseId || enr.id}`;
                        const teacherName = enr.teacher
                          ? `${enr.teacher.firstName} ${enr.teacher.lastName}`
                          : enr.teacherName || 'Docente UPTC';
                        const schedule = enr.course?.schedule || 'Horario por asignar';
                        const period = enr.period || enr.course?.period || '2026-1';

                        return (
                          <div
                            key={enr.id}
                            className="p-5 rounded-2xl bg-slate-900 border border-slate-800 hover:border-slate-700 transition flex flex-col justify-between space-y-4"
                          >
                            <div>
                              <div className="flex items-start justify-between gap-2">
                                <div>
                                  <h4 className="font-bold text-white text-base">
                                    {courseName}
                                  </h4>
                                  <p className="text-xs text-slate-400 mt-1">
                                    Docente: <span className="text-slate-200">{teacherName}</span>
                                  </p>
                                </div>
                                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                                  {enr.status || 'ACTIVO'}
                                </span>
                              </div>

                              <div className="flex items-center gap-4 text-xs text-slate-400 mt-3 pt-3 border-t border-slate-800/80">
                                <span className="flex items-center gap-1.5">
                                  <Calendar className="h-3.5 w-3.5 text-slate-500" />
                                  <span>{period}</span>
                                </span>
                                <span className="flex items-center gap-1.5">
                                  <Clock className="h-3.5 w-3.5 text-slate-500" />
                                  <span>{schedule}</span>
                                </span>
                              </div>
                            </div>

                            {/* Grades section */}
                            {enr.grades && enr.grades.length > 0 && (
                              <div className="pt-3 border-t border-slate-800/80">
                                <div className="text-[11px] font-semibold text-slate-400 mb-2 flex items-center gap-1.5">
                                  <Award className="h-3.5 w-3.5 text-amber-400" />
                                  <span>Registro de Calificaciones:</span>
                                </div>
                                <div className="grid grid-cols-2 gap-2">
                                  {enr.grades.map((g, gIdx) => (
                                    <div
                                      key={gIdx}
                                      className="p-2 rounded-lg bg-slate-950 border border-slate-800/80 flex items-center justify-between"
                                    >
                                      <span className="text-[11px] text-slate-400 truncate max-w-[110px]" title={g.type}>
                                        {g.type} ({g.percentage}%)
                                      </span>
                                      <span className="text-xs font-mono font-bold text-emerald-400">
                                        {g.value}
                                      </span>
                                    </div>
                                  ))}
                                </div>
                              </div>
                            )}

                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>

              </div>
            )}
          </div>
        )}

        {/* TAB 2: CATÁLOGO DE ASIGNATURAS */}
        {activeTab === 'subjects' && (
          <div className="space-y-4">
            
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <h3 className="font-bold text-white text-base">Catálogo Institucional de Asignaturas</h3>
                <p className="text-xs text-slate-400">Consulta los planes de estudio y descarga los programas temáticos oficiales</p>
              </div>

              {/* Search input */}
              <div className="relative w-full sm:w-64">
                <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Buscar asignatura..."
                  className="w-full pl-9 pr-3 py-1.5 bg-slate-900 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-emerald-500 transition placeholder:text-slate-600"
                />
              </div>
            </div>

            {loadingSubjects && (
              <div className="py-20 text-center text-slate-400 bg-slate-900/50 rounded-2xl border border-slate-800">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500 mx-auto mb-3"></div>
                <p className="text-sm font-medium">Cargando catálogo de asignaturas...</p>
              </div>
            )}

            {subjectsError && (
              <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs">
                {subjectsError}
              </div>
            )}

            {!loadingSubjects && (
              <div className="bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-950 border-b border-slate-800 text-slate-400 font-semibold uppercase text-[11px]">
                    <tr>
                      <th className="py-3.5 px-4">Código</th>
                      <th className="py-3.5 px-4">Asignatura</th>
                      <th className="py-3.5 px-4">Créditos</th>
                      <th className="py-3.5 px-4">Programa Académico</th>
                      <th className="py-3.5 px-4 text-right">Programa Temático</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60 text-slate-300">
                    {filteredSubjects.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="py-8 text-center text-slate-500">
                          No se encontraron asignaturas con el criterio ingresado.
                        </td>
                      </tr>
                    ) : (
                      filteredSubjects.map((sub) => (
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
                                className="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border border-emerald-500/20 text-xs font-medium transition cursor-pointer"
                              >
                                <Download className="h-3.5 w-3.5 text-emerald-400" />
                                <span>Descargar PDF</span>
                              </a>
                            ) : (
                              <span className="text-slate-500 italic text-[11px]">No disponible</span>
                            )}
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

      </main>

      <footer className="border-t border-slate-800 py-4 text-center text-[11px] text-slate-500">
        © 2026 Universidad Pedagógica y Tecnológica de Colombia (UPTC) • Sistema de Gestión Académica
      </footer>

    </div>
  );
}
