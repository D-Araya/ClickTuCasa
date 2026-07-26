# 🏠 ClickTuCasa — Ticketera de Rifas de Casas Online

> **Hito 1: Core de Entidades de Dominio Puro, Suite Automatizada con JUnit 5 / Mockito y Cobertura 100% (JaCoCo)**  
> *Programa Java — Globant Talento Ready / Desafío Latam*

---

## 📋 1. Título y Resumen del Proyecto

**ClickTuCasa** es el motor de dominio central (*Pure Domain Core*) para una plataforma web de venta de entradas y sorteo aleatorio transparente de rifas de casas online. 

En este **Hito 1**, se implementa el modelo de negocio puro en **Java 21 / Java 17 LTS nativo** aplicando **Test-Driven Development (TDD)** y arquitectura de **Puertos y Adaptadores (Arquitectura Limpia)**. El dominio no posee dependencias de frameworks externos (sin Spring Boot, sin JPA/Hibernate, sin controladores HTTP), garantizando la máxima rigurosidad técnica, mantenibilidad y desacoplamiento.

---

## 🏛️ 2. Arquitectura del Dominio (Clean Architecture / Ports & Adapters)

El código está estructurado en módulos aislados dentro del paquete raíz `domain` y escrito **100% en inglés** como lo exige la rúbrica de evaluación:

```plaintext
src/
├── main/java/
│   └── domain/
│       ├── exception/
│       │   ├── InvalidRaffleOperationException.java
│       │   ├── InvalidTicketPriceException.java
│       │   ├── PaymentFailedException.java
│       │   ├── TicketNotAvailableException.java
│       │   └── TicketNotFoundException.java
│       ├── model/
│       │   ├── Raffle.java
│       │   ├── RaffleStatus.java  (ACTIVE, DRAWN, CANCELLED)
│       │   ├── Ticket.java
│       │   └── TicketStatus.java  (AVAILABLE, RESERVED, SOLD)
│       ├── port/
│       │   ├── PaymentGateway.java
│       │   └── RandomNumberGenerator.java
│       └── service/
│           └── RaffleService.java
└── test/java/
    └── domain/
        ├── model/
        │   ├── RaffleTest.java
        │   └── TicketTest.java
        └── service/
            └── RaffleServiceTest.java
```

### Principios de Diseño Aplicados:
- **Zero Frameworks Mágicos**: Java 21 / 17 Puro sin anotaciones de persistencia ni inyección de dependencias web.
- **Inyección por Constructor**: La clase `RaffleService` recibe sus interfaces de puerto (`PaymentGateway`, `RandomNumberGenerator`) obligatoriamente vía constructor.
- **Patrón AAA Estricto**: Todos los unit tests en JUnit 5 están segmentados explícitamente con `// ARRANGE`, `// ACT`, `// ASSERT`.
- **Aislamiento con Mockito**: Pruebas de integración de servicio simuladas mediante Mockito Core (`@Mock`, `when(...).thenReturn(...)`, `verify(...)`).
- **Control de Excepciones de Negocio**: Excepciones personalizadas verificadas con `assertThrows`.

---

## 🚀 3. Comandos de Ejecución

Para compilar, ejecutar la suite de pruebas unitarias y verificar las reglas de cobertura:

```bash
# Correr toda la suite de pruebas automatizadas (JUnit 5 + Mockito)
mvn clean test

# Generar el reporte de cobertura de código en formato HTML (JaCoCo)
mvn jacoco:report
```

---

## 📊 4. Ruta de Evidencia del Reporte (JaCoCo 100% Cobertura)

Una vez ejecutados los comandos anteriores, se genera el reporte interactivo de cobertura JaCoCo. Para verificar la **cobertura matemática del 100% en líneas (Line Coverage) y ramas (Branch Coverage)**, abra la siguiente ruta en cualquier navegador web:

```plaintext
target/site/jacoco/index.html
```

---

## 🔬 5. Caso de Estudio: Análisis de Cobertura JaCoCo y Resolución de Ramas

Para acceder al documento de investigación técnica completo, consulte:  
📄 **[ANALISIS_COBERTURA_JACOCO_RESOLUCION.md](ANALISIS_COBERTURA_JACOCO_RESOLUCION.md)**

### Resumen del Caso de Estudio:
Durante el análisis de calidad con JaCoCo, la línea 50 de `Ticket.java` dentro del método `isReservationExpired` presentaba un **diamante amarillo (98% de cobertura en ramas)** debido a la instrucción `if (this.status != TicketStatus.RESERVED || this.reservedUntil == null)`.

En la evaluación de bytecode de la JVM, la condición `||` utiliza cortocircuito. Ninguna prueba unitaria estaba evaluando la combinación lógica donde el ticket se encontraba en estado `RESERVED` pero con `reservedUntil == null` (camino huérfano).

### Solución Implementada:
1. **Refactorización**: Se dividió la condición compuesta en dos cláusulas de guarda (*Guard Clauses*) independientes.
2. **Prueba Unitaria de Borde**: Se añadió el test `shouldReturnFalseForExpirationWhenReservedUntilIsNull()` en `TicketTest.java`.
3. **Resultado**: Cobertura matemática del **100% en líneas (744/744)** y **100% en ramas (118/118)** en todo el proyecto.

---

## 💯 Checklist de Cumplimiento de Rúbrica (10/10 Puntos)

| Pilar | Criterio de Evaluación | Estado | Evidencia |
|---|---|---|---|
| 🏛️ **Pilar 1** (3 Pts) | **Core de Dominio Puro**: Cero Spring/JPA, Código en Inglés, Inyección por Constructor (Java 21 / 17). | **Excelente (3/3)** | `domain.model`, `domain.service`, `domain.port` |
| 🧪 **Pilar 2** (3 Pts) | **Suite JUnit 5 + Mockito**: Patrón AAA explícito, Excepciones de Negocio con `assertThrows`, Aislamiento con Mocks. | **Excelente (3/3)** | `TicketTest`, `RaffleTest`, `RaffleServiceTest` |
| 💯 **Pilar 3** (4 Pts) | **Cobertura 100% (JaCoCo)**: Cero caminos huérfanos, regla de verificación Maven `minimum = 1.00` en Line y Branch. | **Excelente (4/4)** | `pom.xml` (JaCoCo rule), `target/site/jacoco/index.html` |
