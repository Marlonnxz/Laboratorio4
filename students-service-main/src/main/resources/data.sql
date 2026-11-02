-- =========================================
-- 1. Programas académicos
-- =========================================
INSERT INTO programs (id, name, code) VALUES 
(1, 'Ingenieria de Sistemas y Computacion', 'INC-01'), 
(2, 'Ingenieria Electronica', 'INE-02'), 
(3, 'Ingenieria Industrial', 'INI-03'), 
(4, 'Licenciatura en Matematicas', 'LCM-04'), 
(5, 'Administracion de Empresas', 'ADM-05') 
ON CONFLICT (id) DO NOTHING;

-- =========================================
-- 2. Estudiantes (20 registros)
-- =========================================
INSERT INTO students (id, first_name, last_name, email, program_id) VALUES 
(1, 'Alejandro', 'Perez Gomez', 'alejandro.perez@uptc.edu.co', 1),
(2, 'Maria Fernanda', 'Rodriguez Silva', 'maria.rodriguez@uptc.edu.co', 1),
(3, 'Carlos Andres', 'Gomez Ruiz', 'carlos.gomez@uptc.edu.co', 2),
(4, 'Laura Valentina', 'Torres Mora', 'laura.torres@uptc.edu.co', 3),
(5, 'Juan Pablo', 'Ramirez Castro', 'juan.ramirez@uptc.edu.co', 5),
(6, 'Diana Carolina', 'Vargas Rojas', 'diana.vargas@uptc.edu.co', 4),
(7, 'Andres Felipe', 'Castro Morales', 'andres.castro@uptc.edu.co', 1),
(8, 'Valentina', 'Ortiz Mendoza', 'valentina.ortiz@uptc.edu.co', 2),
(9, 'Mateo', 'Silva Paredes', 'mateo.silva@uptc.edu.co', 3),
(10, 'Sofia Jimena', 'Rincon Cruz', 'sofia.rincon@uptc.edu.co', 5),
(11, 'Daniel Esteban', 'Herrera Lancheros', 'daniel.herrera@uptc.edu.co', 1),
(12, 'Paula Andrea', 'Morales Daza', 'paula.morales@uptc.edu.co', 4),
(13, 'Santiago', 'Bermudez Avila', 'santiago.bermudez@uptc.edu.co', 2),
(14, 'Camila Andrea', 'Suarez Pinto', 'camila.suarez@uptc.edu.co', 3),
(15, 'Nicolas Alejandro', 'Rojas Galindo', 'nicolas.rojas@uptc.edu.co', 5),
(16, 'Natalia Estefania', 'Romero Cardenas', 'natalia.romero@uptc.edu.co', 4),
(17, 'David Ricardo', 'Gutierrez Nieto', 'david.gutierrez@uptc.edu.co', 1),
(18, 'Karen Daniela', 'Silva Rincon', 'karen.silva@uptc.edu.co', 2),
(19, 'Cristian Camilo', 'Medina Vargas', 'cristian.medina@uptc.edu.co', 3),
(20, 'Angelica Maria', 'Parra Guio', 'angelica.parra@uptc.edu.co', 5)
ON CONFLICT (id) DO NOTHING;

-- =========================================
-- 3. Contactos de los estudiantes (20 registros)
-- =========================================
INSERT INTO contacts (id, student_id, phone, address, emergency_contact_name, emergency_contact_phone) VALUES 
(1, 1, '3111234567', 'Calle 10 # 2-31, Tunja', 'Rosa Gomez', '3109876543'),
(2, 2, '3127654321', 'Carrera 5 # 15-40, Duitama', 'Pedro Rodriguez', '3201112233'),
(3, 3, '3159988776', 'Avenida Central # 8-12, Sogamoso', 'Carmen Ruiz', '3134445566'),
(4, 4, '3172233445', 'Calle 20 # 6-50, Tunja', 'Luis Torres', '3115556677'),
(5, 5, '3189900112', 'Carrera 11 # 22-10, Chiquinquira', 'Ana Castro', '3126667788'),
(6, 6, '3101122334', 'Calle 14 # 9-20, Tunja', 'Jorge Vargas', '3158889900'),
(7, 7, '3142233445', 'Carrera 8 # 19-33, Duitama', 'Martha Morales', '3167778899'),
(8, 8, '3193344556', 'Calle 3 # 4-55, Sogamoso', 'Humberto Ortiz', '3176665544'),
(9, 9, '3204455667', 'Transversal 12 # 30-11, Tunja', 'Lucia Paredes', '3185554433'),
(10, 10, '3215566778', 'Diagonal 18 # 7-88, Paipa', 'Marco Rincon', '3194443322'),
(11, 11, '3116677889', 'Calle 24 # 10-05, Tunja', 'Claudia Lancheros', '3103332211'),
(12, 12, '3127788990', 'Carrera 6 # 12-45, Chiquinquira', 'Alberto Daza', '3112221100'),
(13, 13, '3158899001', 'Avenida Norte # 50-20, Tunja', 'Beatriz Avila', '3121110099'),
(14, 14, '3179900112', 'Calle 50 # 14-23, Duitama', 'Fernando Pinto', '3130009988'),
(15, 15, '3180011223', 'Carrera 13 # 2-19, Sogamoso', 'Gloria Galindo', '3149998877'),
(16, 16, '3101122445', 'Calle 9 # 11-40, Tunja', 'Nestor Cardenas', '3158887766'),
(17, 17, '3142233556', 'Carrera 22 # 18-90, Paipa', 'Sandra Nieto', '3167776655'),
(18, 18, '3193344667', 'Calle 16 # 5-12, Duitama', 'Raul Silva', '3176665543'),
(19, 19, '3204455778', 'Carrera 7 # 21-30, Tunja', 'Patricia Vargas', '3185554432'),
(20, 20, '3215566889', 'Avenida Circunvalar # 4-10, Sogamoso', 'Guillermo Guio', '3194443321')
ON CONFLICT (id) DO NOTHING;

-- =========================================
-- 4. Documentos de identidad (20 registros)
-- =========================================
INSERT INTO documents (id, student_id, type, number, issue_date) VALUES 
(1, 1, 'CC', '1052345678', '2020-05-15'),
(2, 2, 'CC', '1053456789', '2021-08-20'),
(3, 3, 'CC', '1054567890', '2019-11-10'),
(4, 4, 'TI', '1055678901', '2018-03-05'),
(5, 5, 'CC', '1056789012', '2022-01-30'),
(6, 6, 'CC', '1057890123', '2020-09-12'),
(7, 7, 'CC', '1058901234', '2021-02-18'),
(8, 8, 'CC', '1059012345', '2019-07-22'),
(9, 9, 'CC', '1060123456', '2022-04-14'),
(10, 10, 'CC', '1061234567', '2020-11-05'),
(11, 11, 'CC', '1062345678', '2021-06-25'),
(12, 12, 'CC', '1063456789', '2019-10-08'),
(13, 13, 'CC', '1064567890', '2022-03-19'),
(14, 14, 'CC', '1065678901', '2020-12-01'),
(15, 15, 'CC', '1066789012', '2021-05-17'),
(16, 16, 'CC', '1067890123', '2019-08-29'),
(17, 17, 'CC', '1068901234', '2022-02-11'),
(18, 18, 'CC', '1069012345', '2020-07-04'),
(19, 19, 'CC', '1070123456', '2021-09-15'),
(20, 20, 'CC', '1071234567', '2019-12-20')
ON CONFLICT (id) DO NOTHING;