-- Datos de prueba para ver el flujo de consultarUbicacionDeBolsines()

INSERT IGNORE INTO usuario (id, nombre, password) VALUES
    (1, 'jperez', '1234'),
    (2, 'mgomez', 'abcd');

INSERT IGNORE INTO rol (id, nombre, descripcion) VALUES
    (1, 'GCM', 'Gerente de la Comision Medica'),
    (2, 'EMPLEADO', 'Empleado raso');

-- Los codigos de CM y los numeros de bolsin (101-110) coinciden con los del mock de la API GPS (dsi-api-bolsines).
-- Ids de CM: 1=CBA-01 2=ROS-02 3=MDZ-03 4=BA-01 5=TUC-01 6=SLA-01 7=JUJ-01 8=NQN-01 9=MDQ-01 10=LPL-01 11=CBA-02
INSERT IGNORE INTO comision_medica (id, codigo, direccion, email, nombre, telefono) VALUES
    (1, 'CM-CBA-01', 'Av. Siempre Viva 123', 'cba01@bolsin.com', 'Comision Medica Cordoba 01', '0351-444-0001'),
    (2, 'CM-ROS-02', 'Calle Falsa 456', 'ros02@bolsin.com', 'Comision Medica Rosario 02', '0341-444-0002'),
    (3, 'CM-MDZ-03', 'Av. San Martin 789', 'mdz03@bolsin.com', 'Comision Medica Mendoza 03', '0261-444-0003'),
    (4, 'CM-BA-01', 'Av. de Mayo 1000', 'ba01@bolsin.com', 'Comision Medica Buenos Aires 01', '011-4444-0004'),
    (5, 'CM-TUC-01', 'Calle 24 de Septiembre 55', 'tuc01@bolsin.com', 'Comision Medica Tucuman 01', '0381-444-0005'),
    (6, 'CM-SLA-01', 'Av. Belgrano 300', 'sla01@bolsin.com', 'Comision Medica Salta 01', '0387-444-0006'),
    (7, 'CM-JUJ-01', 'Calle Lavalle 120', 'juj01@bolsin.com', 'Comision Medica Jujuy 01', '0388-444-0007'),
    (8, 'CM-NQN-01', 'Av. Argentina 210', 'nqn01@bolsin.com', 'Comision Medica Neuquen 01', '0299-444-0008'),
    (9, 'CM-MDQ-01', 'Av. Colon 1500', 'mdq01@bolsin.com', 'Comision Medica Mar del Plata 01', '0223-444-0009'),
    (10, 'CM-LPL-01', 'Calle 7 Nro 800', 'lpl01@bolsin.com', 'Comision Medica La Plata 01', '0221-444-0010'),
    (11, 'CM-CBA-02', 'Av. Velez Sarsfield 450', 'cba02@bolsin.com', 'Comision Medica Cordoba 02', '0351-444-0011');

INSERT IGNORE INTO empleado (id, apellido, email, nombre, usuario_id, rol_id, comision_medica_id) VALUES
    (1, 'Perez', 'jperez@bolsin.com', 'Juan', 1, 1, 1),
    (2, 'Gomez', 'mgomez@bolsin.com', 'Maria', 2, 2, 2);

-- Un Gerente (rol GCM) por cada Comision Medica, para probar el envio del correo al GCM destino.
-- La CM 1 (CM-CBA-01) ya tiene como GCM a Juan Perez (empleado 1).
INSERT IGNORE INTO empleado (id, apellido, email, nombre, usuario_id, rol_id, comision_medica_id) VALUES
    (3, 'Rodriguez', 'gcm.ros02@bolsin.com', 'Carlos', NULL, 1, 2),
    (4, 'Fernandez', 'gcm.mdz03@bolsin.com', 'Laura', NULL, 1, 3),
    (5, 'Lopez', 'gcm.ba01@bolsin.com', 'Martin', NULL, 1, 4),
    (6, 'Martinez', 'gcm.tuc01@bolsin.com', 'Sofia', NULL, 1, 5),
    (7, 'Garcia', 'gcm.sla01@bolsin.com', 'Diego', NULL, 1, 6),
    (8, 'Sanchez', 'gcm.juj01@bolsin.com', 'Valeria', NULL, 1, 7),
    (9, 'Romero', 'gcm.nqn01@bolsin.com', 'Pablo', NULL, 1, 8),
    (10, 'Diaz', 'gcm.mdq01@bolsin.com', 'Lucia', NULL, 1, 9),
    (11, 'Torres', 'gcm.lpl01@bolsin.com', 'Federico', NULL, 1, 10),
    (12, 'Alvarez', 'gcm.cba02@bolsin.com', 'Carolina', NULL, 1, 11);

INSERT IGNORE INTO estado (id, nombre, descripcion, ambito) VALUES
    (1, 'ABIERTO', 'Bolsin abierto, todavia no fue enviado', 'BOLSIN'),
    (2, 'ENVIADO', 'Bolsin enviado a la CM destino', 'BOLSIN');

-- Bolsin id = numero_bolsin - 100. Origen y destino iguales a los del mock.
INSERT IGNORE INTO bolsin (id, fecha, numero_bolsin, numero_precinto, peso, cm_origen_id, cm_destino_id) VALUES
    (1, '2026-09-01', 101, 'P-1001', 12.5, 1, 2),
    (2, '2026-09-02', 102, 'P-1002', 8.3, 2, 3),
    (3, '2026-09-03', 103, 'P-1003', 15.0, 3, 4),
    (4, '2026-09-04', 104, 'P-1004', 20.1, 4, 1),
    (5, '2026-09-05', 105, 'P-1005', 5.7, 5, 6),
    (6, '2026-09-06', 106, 'P-1006', 9.9, 6, 7),
    (7, '2026-09-07', 107, 'P-1007', 18.4, 8, 9),
    (8, '2026-09-08', 108, 'P-1008', 7.2, 9, 4),
    (9, '2026-09-09', 109, 'P-1009', 11.6, 10, 1),
    (10, '2026-09-10', 110, 'P-1010', 14.8, 11, 2);

-- Solo se cargan los bolsines que conoce el mock del GPS (101-110).
-- Con el usuario jperez (CM-CBA-01) el unico bolsin Enviado con origen CM-CBA-01 es el 101.
-- 101: Abierto -> Enviado (actual)
-- 102: Enviado (origen otra CM)
-- 103: Abierto (actual)
-- 104: Enviado (origen otra CM)
-- 105: Abierto -> Enviado -> Abierto (reabierto)
-- 106 a 110: Enviado (origen otra CM), salvo 108 que esta Abierto
INSERT IGNORE INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, bolsin_id, estado_id) VALUES
    (1, '2026-09-01 08:00:00', '2026-09-01 10:00:00', 1, 1),
    (2, '2026-09-01 10:00:00', NULL, 1, 2),
    (3, '2026-09-02 08:00:00', NULL, 2, 2),
    (4, '2026-09-03 09:00:00', NULL, 3, 1),
    (5, '2026-09-04 08:00:00', NULL, 4, 2),
    (6, '2026-09-05 08:00:00', '2026-09-05 09:00:00', 5, 1),
    (7, '2026-09-05 09:00:00', '2026-09-05 12:00:00', 5, 2),
    (8, '2026-09-05 12:00:00', NULL, 5, 1),
    (9, '2026-09-06 08:00:00', NULL, 6, 2),
    (10, '2026-09-07 08:00:00', NULL, 7, 2),
    (11, '2026-09-08 08:00:00', NULL, 8, 1),
    (12, '2026-09-09 08:00:00', NULL, 9, 2),
    (13, '2026-09-10 08:00:00', NULL, 10, 2);
