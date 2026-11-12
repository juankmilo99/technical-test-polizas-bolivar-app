-- ============================================================================
-- INITIAL SEED DATA (H2 DATABASE) - POLICIES & RISKS
-- ============================================================================

-- Policy 1: Individual Active (1 risk)
INSERT INTO policies (id, policy_number, type, status, start_date, end_date, coverage_months, monthly_rent, total_premium, policy_holder, insured, beneficiary)
VALUES (1, 'POL-IND-001', 'INDIVIDUAL', 'ACTIVE', '2026-01-01', '2026-12-31', 12, 1500000.00, 18000000.00, 'Carlos Perez (Tenant)', 'Carlos Perez (Tenant)', 'Maria Gomez (Landlord)');

INSERT INTO risks (id, policy_id, status, property_address, city, insured_name, insured_amount)
VALUES (1, 1, 'ACTIVE', 'Carrera 7 # 123-45 Apt 501', 'Bogota', 'Carlos Perez', 1500000.00);

-- Policy 2: Collective Active (2 active risks)
INSERT INTO policies (id, policy_number, type, status, start_date, end_date, coverage_months, monthly_rent, total_premium, policy_holder, insured, beneficiary)
VALUES (2, 'POL-COL-002', 'COLLECTIVE', 'ACTIVE', '2026-03-01', '2027-02-28', 12, 4500000.00, 54000000.00, 'Inmobiliaria Los Andes SAS', 'Multiple Tenants', 'Property Owners');

INSERT INTO risks (id, policy_id, status, property_address, city, insured_name, insured_amount)
VALUES (2, 2, 'ACTIVE', 'Calle 100 # 15-20 Local 102', 'Bogota', 'Juanita Morales', 2500000.00);

INSERT INTO risks (id, policy_id, status, property_address, city, insured_name, insured_amount)
VALUES (3, 2, 'ACTIVE', 'Avenida El Dorado # 68-80 Of 304', 'Bogota', 'Tech Services Ltda', 2000000.00);

-- Policy 3: Individual Cancelled (to test renewal rule failure with HTTP 422)
INSERT INTO policies (id, policy_number, type, status, start_date, end_date, coverage_months, monthly_rent, total_premium, policy_holder, insured, beneficiary)
VALUES (3, 'POL-IND-003', 'INDIVIDUAL', 'CANCELLED', '2025-01-01', '2025-12-31', 12, 1200000.00, 14400000.00, 'Andres Castro', 'Andres Castro', 'Luisa Fernandez');

INSERT INTO risks (id, policy_id, status, property_address, city, insured_name, insured_amount)
VALUES (4, 3, 'CANCELLED', 'Transversal 23 # 45-67 Casa 4', 'Medellin', 'Andres Castro', 1200000.00);

-- Policy 4: Collective Renewed (to test filter status=RENEWED)
INSERT INTO policies (id, policy_number, type, status, start_date, end_date, coverage_months, monthly_rent, total_premium, policy_holder, insured, beneficiary)
VALUES (4, 'POL-COL-004', 'COLLECTIVE', 'RENEWED', '2026-02-01', '2027-01-31', 12, 6200000.00, 74400000.00, 'Administradora Metropolitan SAS', 'Multiple Co-owners', 'Edificio Residencial Titanium');

INSERT INTO risks (id, policy_id, status, property_address, city, insured_name, insured_amount)
VALUES (5, 4, 'ACTIVE', 'Carrera 11 # 93-50 Apt 1201', 'Bogota', 'Felipe Mendoza', 3100000.00);

INSERT INTO risks (id, policy_id, status, property_address, city, insured_name, insured_amount)
VALUES (6, 4, 'ACTIVE', 'Carrera 11 # 93-50 Apt 1202', 'Bogota', 'Sofia Restrepo', 3100000.00);

-- Policy 5: Individual Active with 24 months validity
INSERT INTO policies (id, policy_number, type, status, start_date, end_date, coverage_months, monthly_rent, total_premium, policy_holder, insured, beneficiary)
VALUES (5, 'POL-IND-005', 'INDIVIDUAL', 'ACTIVE', '2026-01-15', '2028-01-14', 24, 3800000.00, 91200000.00, 'David Sanabria (Tenant)', 'David Sanabria (Tenant)', 'Inversiones Inmobiliarias del Norte');

INSERT INTO risks (id, policy_id, status, property_address, city, insured_name, insured_amount)
VALUES (7, 5, 'ACTIVE', 'Calle 140 # 19-40 Casa 8', 'Bogota', 'David Sanabria', 3800000.00);

-- Policy 6: Collective Cancelled (to test collective policy with cancelled risks)
INSERT INTO policies (id, policy_number, type, status, start_date, end_date, coverage_months, monthly_rent, total_premium, policy_holder, insured, beneficiary)
VALUES (6, 'POL-COL-006', 'COLLECTIVE', 'CANCELLED', '2025-06-01', '2026-05-31', 12, 8500000.00, 102000000.00, 'Centro Comercial Paseo Real', 'Various Merchants', 'Fiduciaria Central');

INSERT INTO risks (id, policy_id, status, property_address, city, insured_name, insured_amount)
VALUES (8, 6, 'CANCELLED', 'Autopista Norte # 185-20 Local 12', 'Bogota', 'Almacen Deportes Sport', 4500000.00);

INSERT INTO risks (id, policy_id, status, property_address, city, insured_name, insured_amount)
VALUES (9, 6, 'CANCELLED', 'Autopista Norte # 185-20 Local 13', 'Bogota', 'Restaurante Gourmet SAS', 4000000.00);

-- Advance H2 identity sequences beyond preloaded seed data IDs
ALTER TABLE policies ALTER COLUMN id RESTART WITH 100;
ALTER TABLE risks ALTER COLUMN id RESTART WITH 100;

