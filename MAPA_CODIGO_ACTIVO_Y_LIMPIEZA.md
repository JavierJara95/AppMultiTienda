# MultiTienda — Limpieza de código antiguo (proyecto entregado)

## Objetivo y alcance

Se tomó como base **MultiTienda_Final_Depurado_Conservador(1).zip**. Se mantuvo el flujo activo **MainActivity → AppRoot**, así como el almacenamiento **Room existente** (`gestion_preferencias.db`), las migraciones y los cuatro roles. No se añadió servidor ni nueva base de datos.

El objetivo es que, al cambiar un botón de búsqueda, puedas ubicar **la pantalla que realmente se ejecuta**. Por eso se retiró la segunda implementación de demostración, que no era accesible desde la actividad de lanzamiento.

## Pantallas y clases que debes estudiar

| Función | Archivo activo / Composable | Estado |
|---|---|---|
| Arranque y Splash | `MainActivity.kt` / `core/ui/SplashMultiTienda.kt` | Conservado |
| Inicio de sesión | `gestion/ui/LoginScreen.kt`, `gestion/viewmodel/SesionViewModel.kt` | Conservado |
| Navegación según el rol | `core/ui/AppRoot.kt`, `core/navigation/Rutas.kt` | Conservado |
| Selección de sucursal | `operador/ui/OperadorScreens.kt` → `UbicacionScreen` | Conservado |
| Inicio operador | `operador/ui/OperadorScreens.kt` → `InicioOperadorScreen` | Conservado |
| Búsqueda manual | `operador/ui/OperadorScreens.kt` → `ManualScreen` | Conservado |
| Escaneo de códigos | `operador/scanner/ScannerScreen.kt` → `ScannerScreen` | Conservado |
| Producto / ficha | `operador/ui/OperadorScreens.kt` → `ProductoScreen` | Conservado |
| Movimientos y revisión | `operador/ui/OperadorScreens.kt` → `MovimientoScreen`, `RevisionMovimientoScreen` | Conservado |
| Conteos y confirmación | `operador/ui/OperadorScreens.kt` → `ConteoScreen`, `ConteoRecibidoScreen` | Conservado |
| Lógica y estado del operador | `operador/viewmodel/OperadorViewModel.kt` | Conservado |
| Validaciones del operador | `operador/validation/OperadorValidator.kt` | Conservado |
| Diferencias, aprobación, alertas, historial y dashboard | `gestion/ui/GestionScreens.kt` | Conservado |
| Lógica y estado de gestión | `gestion/viewmodel/GestionViewModel.kt` | Conservado |
| Gestión de productos de Admin | `core/ui/CatalogoProductosScreen.kt` | Conservado |
| Informes de Admin | `gestion/ui/ReportesScreen.kt` | Conservado |
| Informes de Gerencia original | `core/ui/GerenciaOriginalRoot.kt`, `gestion/ui/ReportesSoloScreen.kt` | Conservado |
| Acceso a inventario y usuarios | `core/data/LocalInventarioGateway.kt` | Conservado |
| Modelos de datos | `core/model/Models.kt` | Conservado |
| Persistencia local | `gestion/local/GestionLocalDatabase.kt` y entidades/DAO | Conservado |

### Botón «Buscar producto» que sí ves en la app

Abre `operador/ui/OperadorScreens.kt`, busca `ManualScreen`, después `Principal("Buscar producto", ...)`. Los estilos reutilizables de `Principal` están en `core/ui/Components.kt`. **No existe ya la pantalla antigua `BusquedaProductoScreen.kt`** para evitar confusión.

### Roles

- **Operador:** flujo de búsqueda, escáner, movimientos, conteos.
- **Encargado:** revisión y resolución de diferencias, alertas y reposición.
- **Admin:** dashboard, gestión de sucursales, reportes y **gestión de productos**.
- **Gerencia:** flujo propio de consulta y exportación CSV/PDF. No se fusiona con Admin.

## Cambios hechos

- **Archivos Kotlin de producción originales:** 90.
- **Conservados de producción:** 35.
- **Retirados por pertenecer a rutas alternativas o código de demostración:** 55.
- **Pruebas unitarias antiguas retiradas:** 21 (dependían de código anterior o eran ejemplos).
- **Pruebas unitarias nuevas del código vigente:** 3 archivos, con 10 métodos `@Test`.
- **Pruebas instrumentadas de catálogo y persistencia conservadas:** 2.
- **Prueba instrumentada de ejemplo retirada:** 1.
- **Documentos de entregas intermedias obsoletos retirados:** 6
- **Manifiesto:** se retiró solo la declaración de `OperadorPaso1DemoActivity`, que ya no se utiliza. La `MainActivity`, permisos e iconos quedaron intactos.
- **Navegación:** intacta, salvo corrección de comentario que mencionaba indebidamente Spring Boot.
- **Sin cambios** en `LocalInventarioGateway.kt`, el esquema o las migraciones Room, los Composables activos, los ViewModels activos, los recursos del icono y la configuración Gradle.

## Archivos de producción retirados

### `core/` (1 archivos)

- `core/integration/MultiTiendaShared.kt`

### `gestion/` (22 archivos)

- `gestion/model/DiferenciaInventario.kt`
- `gestion/model/HistorialAlertaModels.kt`
- `gestion/model/LoginRepository.kt`
- `gestion/model/ReposicionDashboardModels.kt`
- `gestion/model/Rol.kt`
- `gestion/navigation/GestionPermisos.kt`
- `gestion/repository/DiferenciasRepository.kt`
- `gestion/repository/FuenteGestionCompartida.kt`
- `gestion/repository/HistorialAlertasRepository.kt`
- `gestion/repository/PreferenciaSucursalRepository.kt`
- `gestion/repository/ReposicionRepository.kt`
- `gestion/ui/DiferenciasScreen.kt`
- `gestion/ui/HistorialAlertasScreen.kt`
- `gestion/ui/ReportesSucursalesScreen.kt`
- `gestion/ui/ReposicionDashboardScreen.kt`
- `gestion/validation/DecisionValidator.kt`
- `gestion/validation/LoginValidator.kt`
- `gestion/viewmodel/DiferenciasViewModel.kt`
- `gestion/viewmodel/HistorialAlertasViewModel.kt`
- `gestion/viewmodel/LoginViewModel.kt`
- `gestion/viewmodel/PreferenciaSucursalViewModel.kt`
- `gestion/viewmodel/ReposicionDashboardViewModel.kt`

### `operador/` (32 archivos)

- `operador/IntegracionModuloOperador.kt`
- `operador/model/ComprobanteMovimientoDemo.kt`
- `operador/model/ConteoFisicoOperador.kt`
- `operador/model/ProductoOperador.kt`
- `operador/model/SucursalOperador.kt`
- `operador/repository/ConfirmacionMovimientoDemoRepository.kt`
- `operador/repository/ConteoFisicoRepository.kt`
- `operador/repository/ProductoOperadorRepository.kt`
- `operador/repository/SucursalOperadorRepository.kt`
- `operador/scanner/CamaraLectorScreen.kt`
- `operador/scanner/CodigoBarrasAnalyzer.kt`
- `operador/scanner/EscanerCodigoBarrasScreen.kt`
- `operador/scanner/ProductoEscaneadoMatcher.kt`
- `operador/ui/BusquedaProductoScreen.kt`
- `operador/ui/ComprobanteMovimientoDemoScreen.kt`
- `operador/ui/ConteoFisicoScreen.kt`
- `operador/ui/FichaProductoScreen.kt`
- `operador/ui/HistorialOperadorScreen.kt`
- `operador/ui/InicioOperadorScreen.kt`
- `operador/ui/MovimientoScreen.kt`
- `operador/ui/OperadorPaso1DemoActivity.kt`
- `operador/ui/PerfilOperadorScreen.kt`
- `operador/ui/RevisionMovimientoScreen.kt`
- `operador/ui/SeleccionSucursalScreen.kt`
- `operador/validation/MovimientoValidator.kt`
- `operador/validation/OperadorValidaciones.kt`
- `operador/validation/SeleccionSucursalValidator.kt`
- `operador/viewmodel/BusquedaProductoViewModel.kt`
- `operador/viewmodel/ConteoFisicoValidator.kt`
- `operador/viewmodel/ConteoFisicoViewModel.kt`
- `operador/viewmodel/MovimientoViewModel.kt`
- `operador/viewmodel/SeleccionSucursalViewModel.kt`

## Pruebas antiguas retiradas

- `app/src/androidTest/java/com/duoc/appmultitienda/ExampleInstrumentedTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/ExampleUnitTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/FakeLoginRepositoryTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/GestionPermisosTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/core/integration/FlujoCompartidoTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/GestionPermisosPaso10Test.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/IntegracionReposicionTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/model/DiferenciasModelTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/model/HistorialAlertasModelTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/model/ReportesSucursalesTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/repository/AdminAprobacionTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/repository/FakeDiferenciasRepositoryTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/repository/FuenteGestionCompartidaTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/validation/DecisionValidatorTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/validation/LoginValidatorTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/viewmodel/FiltrosHistorialTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/viewmodel/GestionPermisosTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/gestion/viewmodel/ReposicionDashboardTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/operador/OperadorValidacionesTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/operador/validation/SeleccionSucursalValidatorTest.kt`
- `app/src/test/java/com/duoc/appmultitienda/operador/viewmodel/MainDispatcherRule.kt`
- `app/src/test/java/com/duoc/appmultitienda/operador/viewmodel/SeleccionSucursalViewModelTest.kt`

## Documentos antiguos retirados

- `ADAPTACION_V101_LOCAL_CUATRO_ROLES.md`
- `ADMIN_GESTION_PRODUCTOS_LEEME.md`
- `CAMBIOS_SPLASH_ICONO_LOGIN.txt`
- `INTEGRACION_ESTEBAN_JAVIER_LEEME.txt`
- `LIMPIEZA_FINAL_CAMBIOS.md`
- `REDISENO_GESTION_LEEME.md`

## Verificación y límites

Se verificó el origen del arranque por `MainActivity`, la navegación de `AppRoot`, imports de los archivos conservados, XML del manifiesto y ausencia de declaración de la actividad retirada. **No fue posible compilar el APK en este entorno:** el wrapper exige Gradle 9.6.0 y el entorno no puede acceder a `services.gradle.org` para descargarlo. La compilación y prueba de los cuatro roles deben realizarse en Android Studio.

**Precaución:** no desinstales la aplicación del teléfono para probar el proyecto depurado si tienes datos importantes, porque desinstalarla borra los datos privados locales. Guarda una copia de la versión anterior y abre esta versión en una carpeta independiente.
