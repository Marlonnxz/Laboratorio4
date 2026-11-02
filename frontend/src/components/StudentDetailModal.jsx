import React, { useEffect, useState } from 'react';
import { X, User, BookOpen, GraduationCap, Award, Calendar, Clock } from 'lucide-react';
import { apiRequest } from '../api/client';

export default function StudentDetailModal({ studentId, isOpen, onClose }) {
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen && studentId) {
      loadDetail(studentId);
    } else {
      setDetail(null);
      setError(null);
    }
  }, [isOpen, studentId]);

  const loadDetail = async (id) => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiRequest(`/api/students/${id}/detail`);
      setDetail(res.data);
    } catch (err) {
      setError({
        status: err.status || 500,
        message: err.message || 'No fue posible consultar el detalle del estudiante.',
      });
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const student = detail?.student;
  const enrollments = detail?.enrollments || [];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fadeIn">
      <div className="relative w-full max-w-2xl bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden max-h-[90vh] flex flex-col">
        
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between bg-slate-850/50">
          <div className="flex items-center space-x-3">
            <div className="p-2 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
              <GraduationCap className="h-5 w-5" />
            </div>
            <div>
              <h3 className="font-bold text-white text-base">
                Ficha Académica Consolidada
              </h3>
              <p className="text-xs text-slate-400">Expediente curricular y calificaciones del estudiante</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-200 hover:bg-slate-800 rounded-lg transition cursor-pointer"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Modal Content */}
        <div className="p-6 overflow-y-auto flex-1 space-y-6">
          
          {loading && (
            <div className="py-16 text-center text-slate-400">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500 mx-auto mb-3"></div>
              <p className="text-sm font-medium">Consultando expediente académico...</p>
            </div>
          )}

          {error && (
            <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs space-y-1">
              <p className="font-bold">Error al consultar el expediente ({error.status})</p>
              <p className="leading-relaxed">{error.message}</p>
            </div>
          )}

          {!loading && !error && student && (
            <div className="space-y-6 animate-fadeIn">
              
              {/* Profile Card */}
              <div className="flex flex-col sm:flex-row items-center sm:items-start gap-4 p-4 rounded-xl bg-slate-950/60 border border-slate-800">
                <div className="relative shrink-0">
                  {student.photoUrl ? (
                    <img
                      src={student.photoUrl}
                      alt={student.firstName}
                      className="w-20 h-20 rounded-2xl object-cover border-2 border-purple-500/40 shadow-lg shadow-purple-500/10"
                    />
                  ) : (
                    <div className="w-20 h-20 rounded-2xl bg-slate-800 border-2 border-slate-700 flex items-center justify-center text-slate-500">
                      <User className="h-10 w-10" />
                    </div>
                  )}
                </div>

                <div className="flex-1 text-center sm:text-left">
                  <h4 className="text-lg font-bold text-white">
                    {student.firstName} {student.lastName}
                  </h4>
                  <p className="text-xs text-slate-400 font-mono mt-0.5">{student.email}</p>
                  
                  <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2 mt-2">
                    <span className="text-xs px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                      Código: #{student.id}
                    </span>
                    <span className="text-xs px-2.5 py-0.5 rounded-full bg-purple-500/10 text-purple-300 border border-purple-500/20">
                      Ingeniería de Sistemas y Computación
                    </span>
                  </div>
                </div>
              </div>

              {/* Enrollments & Courses Section */}
              <div>
                <h5 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center space-x-2">
                  <BookOpen className="h-4 w-4 text-purple-400" />
                  <span>Asignaturas Registradas ({enrollments.length})</span>
                </h5>

                {enrollments.length === 0 ? (
                  <div className="p-6 rounded-xl bg-slate-950/40 border border-slate-800 text-xs text-slate-400 text-center">
                    El estudiante no registra asignaturas en el periodo lectivo.
                  </div>
                ) : (
                  <div className="space-y-3">
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
                          className="p-4 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-slate-700 transition space-y-3"
                        >
                          <div className="flex items-start justify-between gap-2">
                            <div>
                              <div className="font-bold text-slate-200 text-sm">
                                {courseName}
                              </div>
                              <div className="text-xs text-slate-400 mt-0.5">
                                Docente: <span className="text-slate-300 font-medium">{teacherName}</span>
                              </div>
                            </div>
                            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-purple-500/10 text-purple-300 border border-purple-500/20">
                              {enr.status || 'ACTIVO'}
                            </span>
                          </div>

                          <div className="flex items-center gap-4 text-xs text-slate-400 pt-2 border-t border-slate-800/80">
                            <span className="flex items-center gap-1.5">
                              <Calendar className="h-3.5 w-3.5 text-slate-500" />
                              <span>{period}</span>
                            </span>
                            <span className="flex items-center gap-1.5">
                              <Clock className="h-3.5 w-3.5 text-slate-500" />
                              <span>{schedule}</span>
                            </span>
                          </div>

                          {enr.grades && enr.grades.length > 0 && (
                            <div className="pt-2 border-t border-slate-800/80">
                              <div className="text-[11px] font-semibold text-slate-400 mb-1.5 flex items-center gap-1.5">
                                <Award className="h-3.5 w-3.5 text-amber-400" />
                                <span>Calificaciones:</span>
                              </div>
                              <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                                {enr.grades.map((g, gIdx) => (
                                  <div
                                    key={gIdx}
                                    className="p-2 rounded bg-slate-900 border border-slate-800 flex items-center justify-between"
                                  >
                                    <span className="text-[11px] text-slate-400 truncate max-w-[90px]" title={g.type}>
                                      {g.type} ({g.percentage}%)
                                    </span>
                                    <span className="text-xs font-mono font-bold text-purple-300">
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

        {/* Modal Footer */}
        <div className="px-6 py-3 border-t border-slate-800 bg-slate-950/60 flex items-center justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl transition cursor-pointer"
          >
            Cerrar
          </button>
        </div>

      </div>
    </div>
  );
}
