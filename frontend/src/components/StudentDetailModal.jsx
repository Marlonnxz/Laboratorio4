import React, { useEffect, useState } from 'react';
import { X, User, BookOpen, GraduationCap, Award, ShieldAlert, Code, CheckCircle2 } from 'lucide-react';
import { apiRequest } from '../api/client';

export default function StudentDetailModal({ studentId, isOpen, onClose, currentUser }) {
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showJson, setShowJson] = useState(false);

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
      // Calls Gateway composed BFF endpoint
      const res = await apiRequest(`/api/students/${id}/detail`);
      setDetail(res.data);
    } catch (err) {
      setError({
        status: err.status || 500,
        message: err.message,
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
        
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between bg-slate-850/50">
          <div className="flex items-center space-x-2.5">
            <div className="p-2 rounded-lg bg-teal-500/10 text-teal-400 border border-teal-500/20">
              <GraduationCap className="h-5 w-5" />
            </div>
            <div>
              <h3 className="font-bold text-slate-100 text-base">
                Ficha del Estudiante (Composición Gateway /detail)
              </h3>
              <p className="text-xs text-slate-400">Datos orquestados: Estudiantes + Inscripciones + Materias</p>
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
          
          {loading && (
            <div className="py-12 text-center text-slate-400">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500 mx-auto mb-3"></div>
              <p className="text-sm">Orquestando datos a través del API Gateway...</p>
            </div>
          )}

          {error && (
            <div className="p-5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-200 space-y-2">
              <div className="flex items-center space-x-2 text-rose-400 font-bold">
                <ShieldAlert className="h-5 w-5" />
                <span>Error {error.status}: Acceso Restringido por Rol (RBAC)</span>
              </div>
              <p className="text-xs text-rose-300 leading-relaxed">
                {error.message}
              </p>
              {error.status === 403 && (
                <div className="text-[11px] bg-rose-950/40 p-2.5 rounded-lg border border-rose-800/40 text-rose-300 mt-2">
                  💡 <strong>Regla del Laboratorio 4 comprobada:</strong> Un usuario con rol <code>ESTUDIANTE</code> sólo tiene autorización para consultar su propio perfil de detalle. Solo el rol <code>ADMIN</code> puede auditar la ficha de cualquier estudiante.
                </div>
              )}
            </div>
          )}

          {!loading && !error && student && (
            <>
              {/* Student Profile Banner */}
              <div className="flex flex-col sm:flex-row items-center sm:items-start gap-4 p-4 rounded-xl bg-slate-950/60 border border-slate-800">
                <div className="relative group shrink-0">
                  {student.photoUrl ? (
                    <img
                      src={student.photoUrl}
                      alt={student.firstName}
                      className="w-20 h-20 rounded-2xl object-cover border-2 border-emerald-500/40 shadow-lg shadow-emerald-500/10"
                    />
                  ) : (
                    <div className="w-20 h-20 rounded-2xl bg-slate-800 border-2 border-slate-700 flex items-center justify-center text-slate-500">
                      <User className="h-10 w-10" />
                    </div>
                  )}
                  {student.photoUrl && (
                    <div className="absolute -bottom-1 -right-1 p-1 bg-emerald-500 text-slate-950 rounded-full" title="Foto en Azure Blob Storage">
                      <CheckCircle2 className="h-3.5 w-3.5" />
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
                      ID: #{student.id}
                    </span>
                    <span className="text-xs px-2.5 py-0.5 rounded-full bg-indigo-500/10 text-indigo-300 border border-indigo-500/20">
                      Programa #{student.programId || '1'}
                    </span>
                    {student.photoOriginalName && (
                      <span className="text-[11px] px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 truncate max-w-[180px]" title={student.photoOriginalName}>
                        📷 {student.photoOriginalName}
                      </span>
                    )}
                  </div>
                </div>
              </div>

              {/* Enrollments & Courses Section */}
              <div>
                <h5 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center space-x-2">
                  <BookOpen className="h-4 w-4 text-emerald-400" />
                  <span>Materias Inscritas ({enrollments.length})</span>
                </h5>

                {enrollments.length === 0 ? (
                  <div className="p-4 rounded-xl bg-slate-950/40 border border-slate-800/80 text-xs text-slate-400 text-center">
                    El estudiante no registra asignaturas inscritas actualmente.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {enrollments.map((enr, idx) => (
                      <div
                        key={idx}
                        className="p-3.5 rounded-xl bg-slate-950/50 border border-slate-800 hover:border-slate-700 transition space-y-2"
                      >
                        <div className="flex items-start justify-between">
                          <div>
                            <div className="font-semibold text-sm text-slate-200">
                              {enr.course?.name || enr.courseName || `Curso #${enr.courseId || enr.id}`}
                            </div>
                            <div className="text-xs text-slate-400 mt-0.5">
                              Docente: <span className="text-slate-300 font-medium">{enr.teacher?.name || enr.teacherName || 'Por asignar'}</span>
                            </div>
                          </div>
                          <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-teal-500/10 text-teal-300 border border-teal-500/20">
                            Estado: {enr.status || 'ACTIVO'}
                          </span>
                        </div>

                        {enr.grades && enr.grades.length > 0 && (
                          <div className="pt-2 border-t border-slate-800/60 flex items-center gap-2 text-xs">
                            <Award className="h-3.5 w-3.5 text-amber-400 shrink-0" />
                            <span className="text-slate-400">Notas:</span>
                            <div className="flex flex-wrap gap-1.5">
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

              {/* JSON Payload Inspection for Defense */}
              <div className="pt-2 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowJson(!showJson)}
                  className="flex items-center space-x-1.5 text-xs text-slate-400 hover:text-slate-200 transition"
                >
                  <Code className="h-3.5 w-3.5 text-indigo-400" />
                  <span>{showJson ? 'Ocultar JSON' : 'Inspeccionar respuesta JSON del Gateway'}</span>
                </button>

                {showJson && (
                  <pre className="mt-2 p-3 rounded-xl bg-slate-950 border border-slate-800 text-[11px] font-mono text-emerald-400 overflow-x-auto max-h-48">
                    {JSON.stringify(detail, null, 2)}
                  </pre>
                )}
              </div>
            </>
          )}

        </div>

      </div>
    </div>
  );
}
