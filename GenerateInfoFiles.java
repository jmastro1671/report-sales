package entregaUno;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class for generating test files for the sales system.
 * Generates files with sellers, products and random sales.
 */
public class GenerateInfoFiles {
    
    // Constants for file generation
    private static final String[] DOCUMENT_TYPES = {"CC", "CE", "NIT", "TI", "PP"};
    private static final String[] NAMES = {"Juan", "María", "Carlos", "Ana", "Pedro", "Laura", "Diego", "Sofía", "Miguel", "Valentina"};
    private static final String[] LAST_NAMES = {"Gómez", "Rodríguez", "López", "Martínez", "González", "Pérez", "Sánchez", "Ramírez", "Torres", "Díaz"};
    private static final String[] PRODUCTS = {"Laptop", "Smartphone", "Tablet", "Monitor", "Teclado", "Mouse", "Audífonos", "Impresora", "Cámara", "Altavoces"};
    
    // Directory where files will be saved
    private static final String OUTPUT_DIR = "data/";
    
    // Random number generator
    private static final Random random = new Random();
    
    /**
     * Main method for generating test files
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Create directory if it doesn't exist
        File directory = new File(OUTPUT_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        try {
            // Generate default sellers and products if no arguments are specified
            int salesmenCount = 5;
            int productsCount = 10;
            
            // If arguments are provided, use those values
            if (args.length >= 1) {
                salesmenCount = Integer.parseInt(args[0]);
            }
            if (args.length >= 2) {
                productsCount = Integer.parseInt(args[1]);
            }
            
            // Generate files
            List<String> salesmen = createSalesmenFile(salesmenCount);
            createProductsFile(productsCount);
            createSalesFiles(salesmen, productsCount);
            
            System.out.println("Archivos generados exitosamente en el directorio: " + OUTPUT_DIR);
        } catch (IOException e) {
            System.err.println("Error al generar los archivos: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error en los argumentos. Uso: java GenerateInfoFiles [numVendedores] [numProductos]");
        }
    }
    
    /**
     * Creates a file with seller information
     * @param count number of sellers to generate
     * @return list of seller identifiers (for creating sales files)
     * @throws IOException if there's an error writing to the file
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
                
                // Save identifier for creating sales files
                salesmen.add(docType + ":" + docNumber);
            }
        }
        
        return salesmen;
    }
    
    /**
     * Creates a file with product information
     * @param count number of products to generate
     * @throws IOException if there's an error writing to the file
     */
    private static void createProductsFile(int count) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "productos.txt"))) {
            for (int i = 0; i < count; i++) {
                String id = "P" + String.format("%03d", i + 1);
                String name = PRODUCTS[i % PRODUCTS.length] + " " + (i / PRODUCTS.length + 1);
                double price = 10000 + random.nextDouble() * 990000; // Price between 10,000 and 1,000,000
                
                String product = id + ":" + name + ":" + String.format("%.2f", price);
                writer.write(product);
                writer.newLine();
            }
        }
    }
    
    /**
     * Creates sales files for each seller
     * @param salesmen list of seller identifiers
     * @param productsCount total number of available products
     * @throws IOException if there's an error writing to the files
     */
    private static void createSalesFiles(List<String> salesmen, int productsCount) throws IOException {
        for (String seller : salesmen) {
            // Create a filename based on the seller's ID
            String fileName = seller.replace(":", "_") + ".txt";
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + fileName))) {
                // First line: seller identification
                writer.write(seller);
                writer.newLine();
                
                // Generate random sales (between 1 and 5 products sold)
                int numProductsSold = 1 + random.nextInt(5);
                List<Integer> productsSold = new ArrayList<>();
                
                for (int i = 0; i < numProductsSold; i++) {
                    int productId;
                    // Avoid duplicate products
                    do {
                        productId = random.nextInt(productsCount);
                    } while (productsSold.contains(productId));
                    
                    productsSold.add(productId);
                    
                    String id = "P" + String.format("%03d", productId + 1);
                    int quantity = 1 + random.nextInt(10); // Between 1 and 10 units
                    
                    String sale = id + ":" + quantity + ";";
                    writer.write(sale);
                    writer.newLine();
                }
            }
        }
    }
    
    /**
     * Generates a single sales file for a specific seller
     * @param randomSalesCount number of sales to generate
     * @param name seller's name
     * @param id seller's identifier
     * @throws IOException if there's an error writing to the file
     */
    public static void createSalesManFile(int randomSalesCount, String name, String id) throws IOException {
        // Create directory if it doesn't exist
        File directory = new File(OUTPUT_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Generate random document type and number if none is provided
        String sellerId = id;
        if (sellerId == null || sellerId.isEmpty()) {
            String docType = DOCUMENT_TYPES[random.nextInt(DOCUMENT_TYPES.length)];
            String docNumber = String.format("%08d", random.nextInt(100000000));
            sellerId = docType + ":" + docNumber;
        }
        
        // Create seller file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "vendedor_" + name + ".txt"))) {
            // First line: seller identification
            writer.write(sellerId);
            writer.newLine();
            
            // Generate random sales
            for (int i = 0; i < randomSalesCount; i++) {
                String productId = "P" + String.format("%03d", i + 1);
                int quantity = 1 + random.nextInt(10); // Between 1 and 10 units
                
                String sale = productId + ":" + quantity + ";";
                writer.write(sale);
                writer.newLine();
            }
        }
    }
    
}
