# report-sales
 Reportes de ventas
 GenerateInfoFiles.java
Este archivo contiene una clase para generar datos de prueba para el sistema de ventas. Crea archivos de vendedores, productos y ventas con información aleatoria en el directorio "data/". Incluye métodos para generar archivos de vendedores con tipos de documento y nombres aleatorios, archivos de productos con precios variables, y archivos de ventas individuales para cada vendedor.
SalesProcessor.java
Esta clase procesa los archivos generados y produce reportes. Lee los archivos de vendedores, productos y ventas, calcula los totales de venta por vendedor y la cantidad de productos vendidos, y genera dos reportes CSV: uno con los vendedores ordenados por total vendido y otro con los productos ordenados por cantidad vendida. Incluye manejo especial para números decimales con coma.
