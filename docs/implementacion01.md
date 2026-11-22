# Documento de Decisiones Técnicas

## 1. Lenguaje de Programación

### Opción evaluada: Python

| Pros | Contras |
|---|---|
| Fácil de leer | No es orientado a objetos |
| | No conocemos / no sabemos cómo desplegar servicios en Python |

### Opción evaluada: Java

| Pros | Contras |
|---|---|
| Es orientado a objetos | No todos en el equipo lo dominan |
| Lo usamos en backend | |
| Sintaxis similar a JavaScript | |
| En backend vemos formas de despliegue y microservicios | |
| Recomendado por Germán Vélez | |

> **Decisión:** ✅ **Java**
>
> Justificación: mayor alineación con POO, experiencia previa del equipo en backend, familiaridad con la sintaxis y recomendación del docente/mentor. El único contra (dominio dispar del lenguaje) se puede mitigar con nivelación inicial del equipo.

---

## 2. Framework

| Ítem | Decisión |
|---|---|
| Framework backend | **Spring** (en caso de usar Java) |

---

## 3. Tecnología

| Ítem | Decisión |
|---|---|
| Tipo de aplicación | **Web responsive** |
| Origen del requisito | RNF (Requisito No Funcional) especificado en el enunciado del dominio |

---

## 4. Base de Datos y Framework de Persistencia

| Ítem | Decisión | Justificación |
|---|---|---|
| Base de datos | **PostgreSQL** | *(pendiente de completar)* |
| ORM / Framework de persistencia | **Hibernate** | Es un ORM consolidado y ampliamente probado |

---

## Pendientes a definir

- [ ] Justificación de PostgreSQL frente a otras alternativas (MySQL, MongoDB, etc.)
- [ ] Tecnología de frontend (React, Angular, Thymeleaf, etc.)
- [ ] Control de versiones y metodología de trabajo (Git, Scrum/Kanban)
- [ ] Estrategia de despliegue (Docker, cloud, on-premise)
- [ ] Framework de testing (JUnit, Mockito, etc.)
