# Microservicio de Gestión de KPIs - Grupo Cordillera

Este repositorio contiene el microservicio encargado de la administración y seguimiento de los indicadores clave de desempeño (KPIs). Es un componente especializado dentro del ecosistema **Grupo Cordillera**.

## 📝 Descripción del Servicio

El microservicio gestiona el ciclo de vida de los KPIs y sus métricas. Está diseñado para integrarse con un **BFF** (Backend for Frontend) y ser expuesto a través de un **API Gateway**, utilizando una base de datos persistente para el almacenamiento de información histórica y definiciones.

## 🛠 Stack Tecnológico

* **Lenguaje:** Java 17 o superior
* **Framework:** Spring Boot 3.x
* **Base de Datos:** PostgreSQL
* **Puerto de Servicio:** `8087`
* **Librerías Clave:**
    * Spring Data JPA
    * Lombok
    * Driver JDBC para PostgreSQL

---

## 🚀 Guía de Ejecución Local

### 1. Configuración de la Base de Datos

Antes de ejecutar el servicio, asegúrate de tener creada la base de datos y configurar las credenciales en el archivo `src/main/resources/application.properties`. 

```properties
# Configuración de conexión
spring.datasource.url=jdbc:postgresql://localhost:5432/kpi
spring.datasource.username=postgres
spring.datasource.password=12345

# Configuración de Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
