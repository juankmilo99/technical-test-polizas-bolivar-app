-- ============================================================================
-- SEMILLA DE DATOS INICIALES (H2 DATABASE)
-- ============================================================================

-- Póliza 1: Individual Vigente (1 riesgo)
INSERT INTO polizas (id, numero_poliza, tipo, estado, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima_total, tomador, asegurado, beneficiario)
VALUES (1, 'POL-IND-001', 'INDIVIDUAL', 'VIGENTE', '2026-01-01', '2026-12-31', 12, 1500000.00, 18000000.00, 'Carlos Perez (Arrendatario)', 'Carlos Perez (Arrendatario)', 'Maria Gomez (Arrendadora)');

INSERT INTO riesgos (id, poliza_id, estado, direccion_inmueble, ciudad, nombre_asegurado, valor_asegurado)
VALUES (1, 1, 'ACTIVO', 'Carrera 7 # 123-45 Apt 501', 'Bogota', 'Carlos Perez', 1500000.00);

-- Póliza 2: Colectiva Vigente (2 riesgos activos)
INSERT INTO polizas (id, numero_poliza, tipo, estado, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima_total, tomador, asegurado, beneficiario)
VALUES (2, 'POL-COL-002', 'COLECTIVA', 'VIGENTE', '2026-03-01', '2027-02-28', 12, 4500000.00, 54000000.00, 'Inmobiliaria Los Andes SAS', 'Varios Arrendatarios', 'Propietarios Inmuebles');

INSERT INTO riesgos (id, poliza_id, estado, direccion_inmueble, ciudad, nombre_asegurado, valor_asegurado)
VALUES (2, 2, 'ACTIVO', 'Calle 100 # 15-20 Local 102', 'Bogota', 'Juanita Morales', 2500000.00);

INSERT INTO riesgos (id, poliza_id, estado, direccion_inmueble, ciudad, nombre_asegurado, valor_asegurado)
VALUES (3, 2, 'ACTIVO', 'Avenida El Dorado # 68-80 Of 304', 'Bogota', 'Tech Services Ltda', 2000000.00);

-- Póliza 3: Individual Cancelada (para probar regla de renovación fallida)
INSERT INTO polizas (id, numero_poliza, tipo, estado, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima_total, tomador, asegurado, beneficiario)
VALUES (3, 'POL-IND-003', 'INDIVIDUAL', 'CANCELADA', '2025-01-01', '2025-12-31', 12, 1200000.00, 14400000.00, 'Andres Castro', 'Andres Castro', 'Luisa Fernandez');

INSERT INTO riesgos (id, poliza_id, estado, direccion_inmueble, ciudad, nombre_asegurado, valor_asegurado)
VALUES (4, 3, 'CANCELADO', 'Transversal 23 # 45-67 Casa 4', 'Medellin', 'Andres Castro', 1200000.00);

-- Póliza 4: Colectiva Renovada (para probar consultas con filtro estado=RENOVADA)
INSERT INTO polizas (id, numero_poliza, tipo, estado, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima_total, tomador, asegurado, beneficiario)
VALUES (4, 'POL-COL-004', 'COLECTIVA', 'RENOVADA', '2026-02-01', '2027-01-31', 12, 6200000.00, 74400000.00, 'Administradora Metropolitan SAS', 'Varios Copropietarios', 'Edificio Residencial Titanium');

INSERT INTO riesgos (id, poliza_id, estado, direccion_inmueble, ciudad, nombre_asegurado, valor_asegurado)
VALUES (5, 4, 'ACTIVO', 'Carrera 11 # 93-50 Apt 1201', 'Bogota', 'Felipe Mendoza', 3100000.00);

INSERT INTO riesgos (id, poliza_id, estado, direccion_inmueble, ciudad, nombre_asegurado, valor_asegurado)
VALUES (6, 4, 'ACTIVO', 'Carrera 11 # 93-50 Apt 1202', 'Bogota', 'Sofia Restrepo', 3100000.00);

-- Póliza 5: Individual Vigente de 24 meses (para probar vigencias extendidas y montos altos)
INSERT INTO polizas (id, numero_poliza, tipo, estado, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima_total, tomador, asegurado, beneficiario)
VALUES (5, 'POL-IND-005', 'INDIVIDUAL', 'VIGENTE', '2026-01-15', '2028-01-14', 24, 3800000.00, 91200000.00, 'David Sanabria (Arrendatario)', 'David Sanabria (Arrendatario)', 'Inversiones Inmobiliarias del Norte');

INSERT INTO riesgos (id, poliza_id, estado, direccion_inmueble, ciudad, nombre_asegurado, valor_asegurado)
VALUES (7, 5, 'ACTIVO', 'Calle 140 # 19-40 Casa 8', 'Bogota', 'David Sanabria', 3800000.00);

-- Póliza 6: Colectiva Cancelada (para probar pólizas colectivas con historial de riesgos cancelados)
INSERT INTO polizas (id, numero_poliza, tipo, estado, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima_total, tomador, asegurado, beneficiario)
VALUES (6, 'POL-COL-006', 'COLECTIVA', 'CANCELADA', '2025-06-01', '2026-05-31', 12, 8500000.00, 102000000.00, 'Centro Comercial Paseo Real', 'Comerciantes Varios', 'Fiduciaria Central');

INSERT INTO riesgos (id, poliza_id, estado, direccion_inmueble, ciudad, nombre_asegurado, valor_asegurado)
VALUES (8, 6, 'CANCELADO', 'Calle 85 # 11-53 Local 201', 'Bogota', 'Boutique Elegance', 4500000.00);

INSERT INTO riesgos (id, poliza_id, estado, direccion_inmueble, ciudad, nombre_asegurado, valor_asegurado)
VALUES (9, 6, 'CANCELADO', 'Calle 85 # 11-53 Local 202', 'Bogota', 'Cafe Gourmet', 4000000.00);

-- Ajuste de secuencia H2 para que nuevos inserts generados por API inicien en IDs libres
ALTER TABLE polizas ALTER COLUMN id RESTART WITH 20;
ALTER TABLE riesgos ALTER COLUMN id RESTART WITH 30;
