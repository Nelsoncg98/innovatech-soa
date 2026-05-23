-- ============================================================================
-- 🏛️ SCRIPT DE INICIALIZACIÓN: BASE DE DATOS KARDEXSAP (ERP CENTRAL)
-- Proyecto: InnovaTech Retail S.A.C - Arquitectura SOA (Semestre 2026-1)
-- ============================================================================

-- 1. Crear la Base de Datos si no existe
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'KardexSAP')
BEGIN
    CREATE DATABASE KardexSAP;
END
GO

USE KardexSAP;
GO

-- 2. Eliminar tablas en orden de dependencias si ya existen (para re-ejecución limpia)
IF OBJECT_ID('dbo.VentasDetalle', 'U') IS NOT NULL DROP TABLE dbo.VentasDetalle;
IF OBJECT_ID('dbo.VentasCabecera', 'U') IS NOT NULL DROP TABLE dbo.VentasCabecera;
IF OBJECT_ID('dbo.Clientes', 'U') IS NOT NULL DROP TABLE dbo.Clientes;
IF OBJECT_ID('dbo.StockInventario', 'U') IS NOT NULL DROP TABLE dbo.StockInventario;
IF OBJECT_ID('dbo.Articulos', 'U') IS NOT NULL DROP TABLE dbo.Articulos;
GO

-- 3. Crear Tabla: Articulos (Maestro de Productos)
CREATE TABLE dbo.Articulos (
    articulo_id INT IDENTITY(1,1) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(MAX) NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    categoria VARCHAR(50) NULL,
    CONSTRAINT PK_Articulos PRIMARY KEY (articulo_id),
    CONSTRAINT UQ_Articulos_SKU UNIQUE (sku)
);
GO

-- 4. Crear Tabla: StockInventario (Kardex y Bloqueos)
CREATE TABLE dbo.StockInventario (
    inventario_id INT IDENTITY(1,1) NOT NULL,
    articulo_id INT NOT NULL,
    stock_fisico INT NOT NULL,
    stock_reservado INT NOT NULL CONSTRAINT DF_StockInventario_Reservado DEFAULT 0,
    stock_disponible AS (stock_fisico - stock_reservado),
    ultima_actualizacion DATETIME NOT NULL CONSTRAINT DF_StockInventario_Actualizacion DEFAULT GETDATE(),
    CONSTRAINT PK_StockInventario PRIMARY KEY (inventario_id),
    CONSTRAINT FK_StockInventario_Articulos FOREIGN KEY (articulo_id) 
        REFERENCES dbo.Articulos (articulo_id) ON DELETE CASCADE
);
GO

-- 5. Crear Tabla: Clientes (Maestro Central)
CREATE TABLE dbo.Clientes (
    cliente_id INT IDENTITY(1,1) NOT NULL,
    tipo_documento VARCHAR(5) NOT NULL,
    numero_documento VARCHAR(15) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NULL,
    correo VARCHAR(150) NULL,
    telefono VARCHAR(20) NULL,
    CONSTRAINT PK_Clientes PRIMARY KEY (cliente_id),
    CONSTRAINT UQ_Clientes_Documento UNIQUE (numero_documento)
);
GO

-- 6. Crear Tabla: VentasCabecera (Transacciones consolidadas)
CREATE TABLE dbo.VentasCabecera (
    venta_id INT IDENTITY(1,1) NOT NULL,
    transaccion_id VARCHAR(50) NOT NULL,
    cliente_id INT NOT NULL,
    canal_origen VARCHAR(50) NOT NULL,
    fecha_hora DATETIME NOT NULL CONSTRAINT DF_VentasCabecera_Fecha DEFAULT GETDATE(),
    metodo_pago VARCHAR(30) NULL,
    total DECIMAL(10, 2) NOT NULL,
    CONSTRAINT PK_VentasCabecera PRIMARY KEY (venta_id),
    CONSTRAINT UQ_VentasCabecera_Transaccion UNIQUE (transaccion_id),
    CONSTRAINT FK_VentasCabecera_Clientes FOREIGN KEY (cliente_id)
        REFERENCES dbo.Clientes (cliente_id)
);
GO

-- 7. Crear Tabla: VentasDetalle (Líneas de venta)
CREATE TABLE dbo.VentasDetalle (
    detalle_id INT IDENTITY(1,1) NOT NULL,
    venta_id INT NOT NULL,
    articulo_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_aplicado DECIMAL(10, 2) NOT NULL,
    CONSTRAINT PK_VentasDetalle PRIMARY KEY (detalle_id),
    CONSTRAINT FK_VentasDetalle_VentasCabecera FOREIGN KEY (venta_id)
        REFERENCES dbo.VentasCabecera (venta_id) ON DELETE CASCADE,
    CONSTRAINT FK_VentasDetalle_Articulos FOREIGN KEY (articulo_id)
        REFERENCES dbo.Articulos (articulo_id)
);
GO


-- ============================================================================
-- 📦 INSERTS INICIALES: SEMILLAS DE PRUEBA (DATA SEEDING)
-- ============================================================================

-- A. Insertar Productos de Prueba (Maestro de Artículos)
INSERT INTO dbo.Articulos (sku, nombre, descripcion, precio_unitario, categoria) VALUES
('TECH-LAP-001', 'Laptop Dell Latitude 5430', 'Intel Core i5, 16GB RAM, 512GB SSD, Pantalla 14 FHD', 3200.00, 'Laptops'),
('TECH-LAP-002', 'MacBook Air M2', 'Chip M2, 8GB RAM, 256GB SSD, Pantalla Liquid Retina 13.6', 4500.00, 'Laptops'),
('TECH-PHN-001', 'Samsung Galaxy S23 Ultra', 'Pantalla 6.8, 256GB Almacenamiento, Camara 200MP', 3800.00, 'Smartphones'),
('TECH-PHN-002', 'iPhone 15 Pro Max', 'Pantalla Super Retina XDR 6.7, 256GB, Chip A17 Pro', 5500.00, 'Smartphones'),
('TECH-AUD-001', 'Audifonos Sony WH-1000XM5', 'Cancelacion de ruido activa, Inalambricos, Bluetooth 5.2', 1200.00, 'Accesorios');
GO

-- B. Insertar Stock Inicial en Almacén Central
INSERT INTO dbo.StockInventario (articulo_id, stock_fisico, stock_reservado) VALUES
(1, 25, 0), -- Laptop Dell (25 en stock real, 0 reservados -> 25 disponibles)
(2, 15, 0), -- MacBook Air (15 disponibles)
(3, 40, 0), -- Galaxy S23 (40 disponibles)
(4, 10, 0), -- iPhone 15 (10 disponibles)
(5, 50, 0); -- Audifonos Sony (50 disponibles)
GO

-- C. Insertar Clientes Base (Facturación)
INSERT INTO dbo.Clientes (tipo_documento, numero_documento, nombres, apellidos, correo, telefono) VALUES
('DNI', '76543210', 'Nelson Alfredo', 'Correa Guadalupe', 'nelson.correa@utp.edu.pe', '987654321'),
('DNI', '12345678', 'Luis Enrique', 'Yanac Caballero', 'luis.yanac@utp.edu.pe', '912345678'),
('RUC', '20100200304', 'InnovaTech Consultores S.A.C', NULL, 'contacto@innovatech.com.pe', '01-4445555');
GO

PRINT 'Base de Datos KardexSAP inicializada correctamente con datos de prueba de InnovaTech.';
GO
