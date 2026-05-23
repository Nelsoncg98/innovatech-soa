# 🚀 Proyecto InnovaTech: Arquitectura Orientada a Servicios (SOA)

> **Integración Omnicanal en Tiempo Real para Ventas e Inventario (ERP, POS y E-commerce)**  
> **Integrantes:** Yanac Caballero, Luis Enrique (U18127171) & Correa Guadalupe, Nelson Alfredo (U23256402)  
> **Curso:** Arquitectura Orientada al Servicio (SOA - UTP 2026-1)  
> **Docente:** Narro Andrade, Manuel Guillermo  

---

## 🏛️ 1. Descripción del Proyecto y Visión Ejecutiva
El proyecto **InnovaTech SOA** tiene como objetivo erradicar de forma definitiva los silos de información transaccionales y geográficos de **InnovaTech Retail S.A.C.** 

### 🛑 La Problemática (AS-IS)
Actualmente, las 12 sucursales de tiendas físicas (POS en .NET C#) y el canal digital (E-commerce) operan de forma desconectada con el almacén central (ERP SAP en SQL Server). Al depender de **procesos por lotes (Batch) nocturnos** para cruzar saldos, se genera una latencia de 24 horas que provoca **"inventario fantasma"** (ventas duplicadas y quiebres de stock continuos).

### 🟢 La Solución (TO-BE)
Establecemos un modelo orientado a servicios interoperable, desacoplado y en tiempo real. Al centralizar las operaciones core a través de un **API Gateway** y un **Orquestador de Ventas**, logramos que cualquier transacción afecte el Kardex maestro de la empresa en el acto, logrando una consistencia financiera y logística omnicanal absoluta.

---

## 🚦 2. Modelo de Referencia SOA (Capas de la Arquitectura)

Para asegurar el cumplimiento de los principios SOA (bajo acoplamiento, neutralidad y reusabilidad), la arquitectura del software está diseñada bajo una estricta separación de responsabilidades:

```mermaid
flowchart TD
    %% Estilos de los subgrafos (Capas)
    classDef capa fill:#f9f9fb,stroke:#4a5568,stroke-width:2px,stroke-dasharray: 5 5;
    classDef componente fill:#ffffff,stroke:#3182ce,stroke-width:1.5px;
    classDef middleware fill:#ebf8ff,stroke:#2b6cb0,stroke-width:2px;
    classDef datos fill:#f7fafc,stroke:#2d3748,stroke-width:2px;

    %% Subgrafos de Capas
    subgraph Capa1["🏛️ Capa de Presentación Consumidores"]
        POS["💻 POS Sucursal C# .NET 8 CLI"]
        WEB["🌐 E-commerce React.js SPA"]
    end
    
    subgraph Capa2["🚦 Capa de Integración / Mensajería Middleware"]
        GW["🌐 API Gateway Spring Cloud Gateway <br> Rate Limiting y TokenValidationFilter JWT"]
    end

    subgraph Capa3["☕ Capa de Servicios de Negocio Microservicios REST"]
        subgraph TaskServices["🎼 Servicios de Tarea Task Services"]
            SalesOrq["Orquestador de Ventas <br> SalesTaskService"]
        end
        subgraph EntityServices["📦 Servicios de Entidad Entity Services"]
            InvSvc["Servicio Inventario <br> InventoryEntityService"]
            CustSvc["Servicio Clientes <br> CustomerEntityService"]
        end
    end

    subgraph Capa4["🗄️ Capa de Datos Persistencia Core"]
        DB[("🛢️ ERP Central SAP <br> SQL Server Database")]
    end

    Niubiz["💳 Pasarela de Pagos Externa <br> Visa y Niubiz API"]

    %% Relaciones y Flujos de Comunicación
    POS -->|HTTP REST JSON| GW
    WEB -->|HTTP REST JSON| GW

    GW -->|/api/v1/ventas/*| SalesOrq
    GW -->|/api/v1/inventario/*| InvSvc

    SalesOrq -->|Paso 1: Consulta y Reserva| InvSvc
    SalesOrq -->|Paso 2: Pago Externo| Niubiz
    SalesOrq -->|Paso 3: Registrar Venta| DB
    SalesOrq -->|Paso 4: Confirmar Baja| InvSvc

    InvSvc -->|JDBC JPA| DB
    CustSvc -->|JDBC JPA| DB

    %% Aplicar clases
    class Capa1,Capa2,Capa3,Capa4,TaskServices,EntityServices capa;
    class POS,WEB,InvSvc,CustSvc,Niubiz componente;
    class GW,SalesOrq middleware;
    class DB datos;
```

---

## 🗄️ 3. Modelo de Datos Relacional (ERP SAP Central: `KardexSAP`)

La persistencia core reside en una única fuente de la verdad en SQL Server. El mapeo físico de los datos está diseñado en **Tercera Forma Normal (3FN)** para garantizar la integridad y auditoría de la facturación:

```mermaid
erDiagram
    ARTICULOS {
        int articulo_id PK
        varchar sku UK
        varchar nombre
        text descripcion
        decimal precio_unitario
        varchar categoria
    }
    STOCK_INVENTARIO {
        int inventario_id PK
        int articulo_id FK
        int stock_fisico
        int stock_reservado
        int stock_disponible
        datetime ultima_actualizacion
    }
    CLIENTES {
        int cliente_id PK
        varchar tipo_documento
        varchar numero_documento UK
        varchar nombres
        varchar apellidos
        varchar correo
        varchar telefono
    }
    VENTAS_CABECERA {
        int venta_id PK
        varchar transaccion_id UK
        int cliente_id FK
        varchar canal_origen
        datetime fecha_hora
        varchar metodo_pago
        decimal total
    }
    VENTAS_DETALLE {
        int detalle_id PK
        int venta_id FK
        int articulo_id FK
        int cantidad
        decimal precio_aplicado
    }

    ARTICULOS ||--o| STOCK_INVENTARIO : "tiene"
    CLIENTES ||--o{ VENTAS_CABECERA : "compra"
    VENTAS_CABECERA ||--|{ VENTAS_DETALLE : "contiene"
    ARTICULOS ||--o{ VENTAS_DETALLE : "vendido_en"
```

---

## 🎼 4. Flujo Transaccional de Orquestación (SalesTaskService)

Para evitar inconsistencias en el inventario o "sobreventas cruzadas", el checkout se gobierna de forma síncrona y atómica por el **Orquestador de Ventas**:

```mermaid
sequenceDiagram
    participant POS_Web as POS / E-commerce
    participant GW as API Gateway
    participant Orq as SalesTaskService (Orquestador)
    participant Pay as Proveedor Pagos (API Externa)
    participant Inv as InventoryEntityService
    participant SAP as ERP SAP Central

    POS_Web->>GW: POST /api/v1/ventas (Payload JSON)
    GW->>Orq: Enruta solicitud de Checkout
    
    rect rgb(235, 245, 255)
        Note over Orq,SAP: Inicio de Orquestación Transaccional
        Orq->>Inv: GET /api/v1/inventario/validar
        Inv->>SAP: Consulta disponibilidad
        SAP-->>Inv: Stock OK
        Inv-->>Orq: Stock Validado
        
        Orq->>Inv: PUT /api/v1/inventario/reserva
        Inv->>SAP: Bloqueo de Stock temporal (Soft-lock)
        
        Note over Orq,Pay: Integración con API de Terceros
        Orq->>Pay: POST /v1/charges (Cargo a Tarjeta)
        Pay-->>Orq: 200 OK (Pago Aprobado)
        
        Orq->>SAP: POST /api/v1/erp/registrar_venta
        SAP-->>Orq: Transacción ERP #98223 Aprobada
        
        Orq->>Inv: POST /api/v1/inventario/confirmar_baja
        Inv->>SAP: Hard-lock (Actualiza Kardex)
    end
    
    Orq-->>GW: 200 OK (Transacción Completa)
    GW-->>POS_Web: 200 OK (Venta Finalizada)
```
*(Nota: Si en el Paso 2 de pago externo ocurre un error o la pasarela rechaza la tarjeta, el Orquestador intercepta la excepción e invoca de forma automática una llamada de compensación (Rollback) para liberar el stock reservado en el Paso 1).*

---

## 🚀 5. Guía de Inicialización del Entorno Local (Despliegue)

Para levantar la topología de base de datos de InnovaTech, asegurar el aislamiento en red y poblar la data inicial de prueba, sigue los siguientes pasos:

### Prerrequisitos
Asegúrate de contar con lo siguiente instalado en tu máquina local:
*   [Docker Desktop](https://www.docker.com/products/docker-desktop/)
*   [Visual Studio Code](https://code.visualstudio.com/)

### 🔌 Paso 1: Levantar el contenedor de Base de Datos
Abre tu terminal en la raíz de la carpeta `/infraestructura` y ejecuta:
```bash
docker-compose up -d sap-erp-db
```
*Este comando descargará la imagen oficial de SQL Server 2022 y la ejecutará exponiéndola en el puerto estándar `1433`.*

### ⚡ Paso 2: Inicializar las Tablas y Semillas (Seed Data)
Una vez que el contenedor de SQL Server esté activo, debes correr el script SQL de creación de base de datos e inyección de datos iniciales. Ejecuta la siguiente instrucción en tu terminal:
```bash
docker exec -i sap-erp-db /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "InnovaTech_2026!" -C -i /usr/config/schema.sql
```
*Este comando invocará el sqlcmd interno del contenedor e inicializará de forma automática las 5 tablas inyectando laptops, celulares, audífonosSony, stock disponible y clientes de prueba (incluyendo datos de facturación para Nelson y Luis).*

### 📦 Paso 3: Verificar la Conexión de Datos
Puedes conectarte a tu servidor SQL Server local utilizando la extensión de SQL Server de VS Code o Azure Data Studio usando las credenciales:
*   **Servidor:** `localhost,1433`
*   **Usuario:** `sa`
*   **Contraseña:** `InnovaTech_2026!`
*   **Base de Datos:** `KardexSAP`

---
*Este proyecto está estructurado bajo principios puristas de codificación. Toda la infraestructura se levanta y gestiona vía terminal para asegurar el dominio absoluto de la sintaxis y los protocolos de integración.*
