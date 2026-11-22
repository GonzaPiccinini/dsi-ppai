# Changelog

Todos los cambios notables de este proyecto se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
y este proyecto adhiere a [Versionado Semántico](https://semver.org/lang/es/).

## [Unreleased]

## [1.0.3] - 2026-09-06

### Added

- Cambio de prueba GITLAB
- Documentación del proceso de release paso a paso en el `README.md`.

## [1.0.2] - 2026-09-06

### Added

- Jobs de GitLab CI para publicar imágenes Docker: Release Candidate
  (`build-release-candidate`, `tag-release-candidate`) al abrir la MR
  `release/* → master`, y release oficial (`promote-release`,
  `create-gitlab-release`) al mergearla.
- Job `backport-to-develop` que abre automáticamente una MR `master → develop`
  después de cada release oficial.
- Agregamos proceso de release
- `CHANGELOG.md` siguiendo el formato de [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
- Job `validate-changelog` en GitLab CI que exige una entrada nueva en el
  changelog en cada Merge Request.
- Tests unitarios para `PantallaSeguimientoBolsines` y `GestorSeguimientoBolsines`.
- Reporte de cobertura con JaCoCo y verificación en CI (`jacoco:check`, mínimo 80%).
- Pipeline de GitLab CI con etapas `validate` y `test`.
- Validación del nombre de rama según la convención GitFlow en Merge Requests.
- Entorno de desarrollo con `docker-compose.dev.yaml`.
- Documentación de la implementación 01 en `docs/`.
- Estructura inicial del proyecto Spring Boot para el caso de uso de seguimiento
  de bolsines (`PantallaSeguimientoBolsines`, `GestorSeguimientoBolsines`).
- Configuración base de Maven, wrapper y `README.md`.

### Changed

- Actualización de JaCoCo a 0.8.15 para compatibilidad con Java 26.

[Unreleased]: https://gitlab.com/julisanchez1991/ppai-bolsin/-/compare/v1.0.3...develop
[1.0.3]: https://gitlab.com/julisanchez1991/ppai-bolsin/-/compare/v1.0.2...v1.0.3
[1.0.2]: https://gitlab.com/julisanchez1991/ppai-bolsin/-/tags/v1.0.2
