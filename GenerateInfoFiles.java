package entregaUno;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Clase para generar archivos de prueba para el sistema de ventas.
 * Genera archivos con vendedores, productos y ventas aleatorias.
 */
public class GenerateInfoFiles {
    
    // Constantes para la generación de archivos
    private static final String[] DOCUMENT_TYPES = {"CC", "CE", "NIT", "TI", "PP"};
    private static final String[] NAMES = {"Juan", "María", "Carlos", "Ana", "Pedro", "Laura", "Diego", "Sofía", "Miguel", "Valentina"};
    private static final String[] LAST_NAMES = {"Gómez", "Rodríguez", "López", "Martínez", "González", "Pérez", "Sánchez", "Ramírez", "Torres", "Díaz"};
    private static final String[] PRODUCTS = {"Laptop", "Smartphone", "Tablet", "Monitor", "Teclado", "Mouse", "Audífonos", "Impresora", "Cámara", "Altavoces"};
    
    // Directorio donde se guardarán los archivos
    private static final String OUTPUT_DIR = "data/";
    
    // Generador de números aleatorios
    private static final Random random = new Random();
    
    /**
     * Método principal para generar los archivos de prueba
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        System.out.println("Iniciando generación de archivos de prueba...");
        
        // Crear directorio si no existe
        File directory = new File(OUTPUT_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        try {
            // Generar vendedores y productos por defecto si no se especifican argumentos
            int salesmenCount = 5;
            int productsCount = 10;
            
            // Si se proporcionan argumentos, usar esos valores
            if (args.length >= 1) {
                salesmenCount = Integer.parseInt(args[0]);
            }
            if (args.length >= 2) {
                productsCount = Integer.parseInt(args[1]);
            }
            
            // Generar archivos
            List<String> salesmen = createSalesmenFile(salesmenCount);
            createProductsFile(productsCount);
            createSalesFiles(salesmen, productsCount);
            
            System.out.println("Generación de archivos completada exitosamente.");
            System.out.println("Archivos generados en el directorio: " + OUTPUT_DIR);
        } catch (IOException e) {
            System.err.println("Error al generar los archivos: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error en los argumentos. Uso: java GenerateInfoFiles [numVendedores] [numProductos]");
        }
    }
    
    /**
     * Crea un archivo con información de vendedores
     * @param count número de vendedores a generar
     * @return lista de identificadores de vendedores (para crear archivos de ventas)
     * @throws IOException si hay un error al escribir el archivo
     */
    private static List<String> createSalesmenFile(int count) throws IOException {
        List<String> salesmen = new ArrayList<>();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "vendedores.txt"))) {
            for (int i = 0; i < count; i++) {
                String docType = DOCUMENT_TYPES[random.nextInt(DOCUMENT_TYPES.length)];
                String docNumber = String.format("%08d", random.nextInt(100000000));
                String name = NAMES[random.nextInt(NAMES.length)];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                
                String seller = docType + ":" + docNumber + ":" + name + ":" + lastName;
                writer.write(seller);
                writer.newLine();
                
                // Guardar identificador para crear archivos de ventas
                salesmen.add(docType + ":" + docNumber);
            }
        }
        
        System.out.println("Archivo de vendedores generado con " + count + " registros.");
        return salesmen;
    }
    
    /**
     * Crea un archivo con información de productos
     * @param count número de productos a generar
     * @throws IOException si hay un error al escribir el archivo
     */
    private static void createProductsFile(int count) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "productos.txt"))) {
            for (int i = 0; i < count; i++) {
                String id = "P" + String.format("%03d", i + 1);
                String name = PRODUCTS[i % PRODUCTS.length] + " " + (i / PRODUCTS.length + 1);
                double price = 10000 + random.nextDouble() * 990000; // Precio entre 10,000 y 1,000,000
                
                String product = id + ":" + name + ":" + String.format("%.2f", price);
                writer.write(product);
                writer.newLine();
            }
        }
        
        System.out.println("Archivo de productos generado con " + count + " registros.");
    }
    
    /**
     * Crea archivos de ventas para cada vendedor
     * @param salesmen lista de identificadores de vendedores
     * @param productsCount número total de productos disponibles
     * @throws IOException si hay un error al escribir los archivos
     */
    private static void createSalesFiles(List<String> salesmen, int productsCount) throws IOException {
        for (String seller : salesmen) {
            // Crear un nombre de archivo basado en el ID del vendedor
            String fileName = seller.replace(":", "_") + ".txt";
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + fileName))) {
                // Primera línea: identificación del vendedor
                writer.write(seller);
                writer.newLine();
                
                // Generar ventas aleatorias (entre 1 y 5 productos vendidos)
                int numProductsSold = 1 + random.nextInt(5);
                List<Integer> productsSold = new ArrayList<>();
                
                for (int i = 0; i < numProductsSold; i++) {
                    int productId;
                    // Evitar productos duplicados
                    do {
                        productId = random.nextInt(productsCount);
                    } while (productsSold.contains(productId));
                    
                    productsSold.add(productId);
                    
                    String id = "P" + String.format("%03d", productId + 1);
                    int quantity = 1 + random.nextInt(10); // Entre 1 y 10 unidades
                    
                    String sale = id + ":" + quantity + ";";
                    writer.write(sale);
                    writer.newLine();
                }
            }
        }
        
        System.out.println("Archivos de ventas generados para " + salesmen.size() + " vendedores.");
    }
}