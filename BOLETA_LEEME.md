# Módulo de boletas MangaHub

Se completa el flujo usando los packages existentes:
- dto: VentaDTO y DetalleVentaDTO reciben cliente y líneas, sin aceptar precios ni vendedor del navegador.
- model: se reutilizan Venta y DetalleVenta con su relación y cascada existentes.
- repository: se reutilizan VentaRepository y DetalleVentaRepository; MangaRepository agrega bloqueo para evitar sobreventa concurrente.
- service: VentaService declara dependencias por constructor, valida cliente/usuario activos, cantidades, mangas y stock; calcula importes BigDecimal y guarda cabecera, detalles y descuento de stock en una transacción.
- controller: VentaController declara las dependencias y expone listado, formulario, registro y boleta. LoginController conserva idUsuario en sesión.
- templates/ventas: listado.html, nueva.html y boleta.html; acceso desde Inicio.

## Ejecutar
1. Importar como proyecto Maven en el IDE con Java 17 o superior.
2. Ejecutar database/MangaHub.sql en MySQL (es el script original: elimina y recrea las tablas; úselo únicamente en una base de pruebas).
3. Configurar la conexión y el usuario en src/main/resources/application.properties. La contraseña de MySQL se toma de la variable de entorno DB_PASSWORD; definirla antes de iniciar el IDE o la aplicación. En PowerShell: `$env:DB_PASSWORD = "TU_CONTRASENA_MYSQL"`.
4. Ejecutar ProyectoMangahubApplication; entrar a http://localhost:8080/login e iniciar sesión con un usuario de la base.
5. Inicio > Boletas de venta > Nueva boleta. Seleccionar cliente y mangas, guardar, consultar e imprimir o guardar PDF desde el navegador.
6. Si había una sesión abierta antes de actualizar, cerrar sesión y volver a entrar para cargar idUsuario.

No se requieren nuevas tablas: la boleta utiliza tbl_venta y tbl_detalle_venta ya declaradas en el SQL original. El número corresponde al id autogenerado de venta. Es un comprobante interno académico; no implementa emisión electrónica SUNAT.

## Verificación manual
- Registrar dos mangas y comprobar total, cabecera/detalles en la BD y descuento de stock.
- Intentar cantidades cero, negativas, mayores al stock y líneas duplicadas que excedan el stock: se rechazan sin venta parcial.
- Abrir /ventas sin sesión: redirige al login.
- Consultar una boleta existente e imprimirla.
- Abrir dos sesiones y vender el último ejemplar: solo una operación debe completarse.

Se mantiene el pom.xml original. Consultar VALIDACION.txt para el resultado de la verificación en este entorno.

## Botones y eliminación
Las vistas incluyen Inicio, Nueva boleta y Eliminar. En el formulario, Eliminar quita una línea antes de guardar. En listado y detalle, Eliminar solicita confirmación y borra la venta y sus detalles en una transacción, devolviendo el stock. La eliminación exige sesión iniciada y usa POST. Verificar en MySQL que una segunda eliminación del mismo ID no vuelve a incrementar el stock.
