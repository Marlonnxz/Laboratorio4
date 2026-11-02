import React, { useEffect, useState, useRef } from 'react';
import {
  Users,
  Search,
  Upload,
  User,
  ExternalLink,
  RefreshCw,
  Image as ImageIcon,
  CheckCircle2,
  AlertCircle,
  FileCheck
} from 'lucide-react';
import { apiRequest } from '../api/client';
import StudentDetailModal from './StudentDetailModal';

export default function StudentsView({ currentUser }) {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  
  // Detail modal state
  const [selectedStudentId, setSelectedStudentId] = useState(null);

  // Upload Photo state
  const [uploadingId, setUploadingId] = useState(null);
  const [uploadSuccess, setUploadSuccess] = useState(null);
  const fileInputRef = useRef(null);

  useEffect(() => {
    fetchStudents();
  }, [search]);

  const fetchStudents = async () => {
    setLoading(true);
    setError(null);
    try {
      const query = search ? `?firstName=${encodeURIComponent(search)}` : '';
      const res = await apiRequest(`/api/students${query}`);
      const list = res.data?.content || (Array.isArray(res.data) ? res.data : []);
      setStudents(list);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectFile = (studentId) => {
    setUploadingId(studentId);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
      fileInputRef.current.click();
    }
  };

  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file || !uploadingId) return;

    // Validate client-side
    if (file.size > 2 * 1024 * 1024) {
      alert('Error: La imagen no puede superar 2 MB (requisito Lab 4).');
      return;
    }
    if (!file.type.match(/^image\/(jpeg|png|jpg)$/i)) {
      alert('Error: Solo se permiten imágenes en formato JPG o PNG (requisito Lab 4).');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    setLoading(true);
    try {
      await apiRequest(`/api/students/${uploadingId}/photo`, {
        method: 'POST',
        body: formData,
      });

      setUploadSuccess(`¡Fotografía subida a Azure Blob Storage para estudiante #${uploadingId}!`);
      setTimeout(() => setUploadSuccess(null), 3500);
      fetchStudents();
    } catch (err) {
      alert(`Error al subir imagen: ${err.message}`);
    } finally {
      setLoading(false);
      setUploadingId(null);
    }
  };

  return (
    <div className="space-y-6">
      
      {/* Hidden file input */}
      <input
        type="file"
        ref={fileInputRef}
        accept="image/png, image/jpeg, image/jpg"
        onChange={handleFileChange}
        className="hidden"
      />

      {/* Header & Search */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center space-x-2">
            <Users className="h-5 w-5 text-emerald-400" />
            <span>Directorio de Estudiantes</span>
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Módulo Estudiantes con fotografías en Azure Blob Storage (bucket privado + SAS temporal)
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <div className="relative">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar por nombre..."
              className="pl-9 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-emerald-500 w-52 sm:w-64 transition"
            />
          </div>

          <button
            onClick={fetchStudents}
            className="p-2 text-slate-400 hover:text-slate-200 bg-slate-900 hover:bg-slate-800 border border-slate-800 rounded-xl transition"
            title="Refrescar lista"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin text-emerald-400' : ''}`} />
          </button>
        </div>
      </div>

      {uploadSuccess && (
        <div className="p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs flex items-center space-x-2 animate-fadeIn">
          <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
          <span>{uploadSuccess}</span>
        </div>
      )}

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs flex items-center space-x-2">
          <AlertCircle className="h-4 w-4 text-rose-400 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Grid of Student Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {students.map((student) => (
          <div
            key={student.id}
            className="glass-card rounded-2xl p-5 border border-slate-800/80 hover:border-slate-700 hover:shadow-xl transition flex flex-col justify-between"
          >
            <div>
              {/* Card top: Avatar + Program Badge */}
              <div className="flex items-start justify-between gap-3 mb-4">
                <div className="relative">
                  {student.photoUrl ? (
                    <img
                      src={student.photoUrl}
                      alt={student.firstName}
                      className="w-14 h-14 rounded-2xl object-cover border-2 border-emerald-500/30 shadow-md"
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = 'https://ui-avatars.com/api/?name=' + student.firstName + '+' + student.lastName + '&background=0D9488&color=fff';
                      }}
                    />
                  ) : (
                    <div className="w-14 h-14 rounded-2xl bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-400">
                      <User className="h-7 w-7" />
                    </div>
                  )}
                  {student.photoUrl && (
                    <div className="absolute -bottom-1 -right-1 p-0.5 bg-emerald-500 text-slate-950 rounded-full" title="Fotografía activa en Azure Blob Storage">
                      <FileCheck className="h-3 w-3" />
                    </div>
                  )}
                </div>

                <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                  ID: #{student.id}
                </span>
              </div>

              {/* Student info */}
              <h3 className="font-bold text-slate-100 text-base">
                {student.firstName} {student.lastName}
              </h3>
              <p className="text-xs text-slate-400 font-mono mt-0.5 truncate">{student.email}</p>
              
              <div className="mt-3 flex items-center space-x-2 text-xs text-slate-400">
                <span className="px-2 py-0.5 rounded bg-slate-800/60 border border-slate-800 text-[11px]">
                  Prog #{student.programId || '1'}
                </span>
                {student.photoOriginalName && (
                  <span className="text-[10px] text-emerald-400 truncate max-w-[140px]" title={student.photoOriginalName}>
                    📷 {student.photoOriginalName}
                  </span>
                )}
              </div>
            </div>

            {/* Actions */}
            <div className="mt-5 pt-3 border-t border-slate-800/80 flex items-center gap-2">
              <button
                onClick={() => setSelectedStudentId(student.id)}
                className="flex-1 py-1.5 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold flex items-center justify-center space-x-1.5 transition"
              >
                <span>Ver Detalle</span>
                <ExternalLink className="h-3.5 w-3.5" />
              </button>

              <button
                onClick={() => handleSelectFile(student.id)}
                className="py-1.5 px-3 rounded-xl bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border border-emerald-500/20 text-xs font-semibold flex items-center space-x-1.5 transition"
                title="Subir foto JPG o PNG (máximo 2 MB)"
              >
                <Upload className="h-3.5 w-3.5 text-emerald-400" />
                <span className="hidden sm:inline">Foto</span>
              </button>
            </div>

          </div>
        ))}

        {!loading && students.length === 0 && (
          <div className="col-span-full py-12 text-center text-slate-500 text-sm">
            No se encontraron estudiantes registrados.
          </div>
        )}
      </div>

      {/* Student Detail Modal */}
      <StudentDetailModal
        studentId={selectedStudentId}
        isOpen={!!selectedStudentId}
        onClose={() => setSelectedStudentId(null)}
        currentUser={currentUser}
      />

    </div>
  );
}
