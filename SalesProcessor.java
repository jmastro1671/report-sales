package entregaUno;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main class that processes seller, product, and sales files
 * to generate sales reports.
 */
public class SalesProcessor {
    
    // Input and output directories
    private static final String INPUT_DIR = "data/";
    private static final String OUTPUT_DIR = "reports/";
    
    // Data structures to store information
    private Map<String, Seller> sellers = new HashMap<>();
    private Map<String, Product> products = new HashMap<>();
    private Map<String, List<Sale>> salesBySeller = new HashMap<>();
    private Map<String, Integer> totalProductsSold = new HashMap<>();
    
    /**
     * Main method that executes file processing and report generation
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        SalesProcessor processor = new SalesProcessor();
        
        try {
            // Create output directory if it doesn't exist
            File directory = new File(OUTPUT_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Load seller and product information
            processor.loadSellers();
            processor.loadProducts();
            
            // Process sales files
            processor.processSalesFiles();
            
            // Generate reports
            processor.createSellerReport();
            processor.createProductReport();
            
            System.out.println("Procesamiento completado exitosamente. Reportes generados en: " + OUTPUT_DIR);
        } catch (IOException e) {
            System.err.println("Error durante el procesamiento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads seller information from the corresponding file
     * @throws IOException if there's an error reading the file
     */
    private void loadSellers() throws IOException {
        File sellersFile = new File(INPUT_DIR + "vendedores.txt");
        
        if (!sellersFile.exists()) {
            throw new IOException("Archivo de vendedores no encontrado: " + sellersFile.getPath());
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(sellersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 4) {
                    String documentType = parts[0];
                    String documentNumber = parts[1];
                    String name = parts[2];
                    String lastName = parts[3];
                    
                    String id = documentType + ":" + documentNumber;
                    Seller seller = new Seller(documentType, documentNumber, name, lastName);
                    sellers.put(id, seller);
                    salesBySeller.put(id, new ArrayList<>());
                }
            }
        }
        
        System.out.println("Vendedores cargados: " + sellers.size());
    }
    
    /**
     * Loads product information from the corresponding file
     * @throws IOException if there's an error reading the file
     */
    private void loadProducts() throws IOException {
        File productsFile = new File(INPUT_DIR + "productos.txt");
        
        if (!productsFile.exists()) {
            throw new IOException("Archivo de productos no encontrado: " + productsFile.getPath());
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(productsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 3) {
                    String id = parts[0];
                    String name = parts[1];
                    
                    // Reemplazar coma por punto para manejar el formato decimal
                    String priceStr = parts[2].replace(',', '.');
                    double price = Double.parseDouble(priceStr);
                    
                    Product product = new Product(id, name, price);
                    products.put(id, product);
                    totalProductsSold.put(id, 0);
                }
            }
        }
        
        System.out.println("Productos cargados: " + products.size());
    }
    
    /**
     * Processes all sales files in the input directory
     * @throws IOException if there's an error reading the files
     */
    private void processSalesFiles() throws IOException {
        File directory = new File(INPUT_DIR);
        File[] files = directory.listFiles((dir, name) -> 
            !name.equals("vendedores.txt") && 
            !name.equals("productos.txt"));
        
        if (files == null || files.length == 0) {
            System.out.println("ADVERTENCIA: No se encontraron archivos de ventas en " + INPUT_DIR);
            return;
        }
        
        for (File file : files) {
            try {
                processSaleFile(file);
            } catch (IOException e) {
                System.err.println("Error al procesar archivo " + file.getName() + ": " + e.getMessage());
            }
        }
        
        System.out.println("Archivos de ventas procesados: " + files.length);
    }
    
    /**
     * Processes an individual sales file
     * @param file the file to process
     * @throws IOException if there's an error reading the file
     */
    private void processSaleFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // First line: seller identification
            String firstLine = reader.readLine();
            if (firstLine == null) {
                System.out.println("Archivo vacío: " + file.getName());
                return;
            }
            
            String sellerId = firstLine;
            if (!sellers.containsKey(sellerId)) {
                System.out.println("Vendedor no encontrado: " + sellerId + " en archivo " + file.getName());
                return;
            }
            
            // Read sales
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(";")) {
                    line = line.substring(0, line.length() - 1);
                }
                
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    String productId = parts[0];
                    int quantity = Integer.parseInt(parts[1]);
                    
                    if (!products.containsKey(productId)) {
                        System.out.println("Producto no encontrado: " + productId + " en archivo " + file.getName());
                        continue;
                    }
                    
                    Product product = products.get(productId);
                    salesBySeller.get(sellerId).add(new Sale(product, quantity));
                    
                    // Update total product counter
                    totalProductsSold.put(productId, 
                        totalProductsSold.get(productId) + quantity);
                }
            }
        }
    }
    
    /**
     * Generates the seller report ordered by total sales
     * @throws IOException if there's an error writing the file
     */
    private void createSellerReport() throws IOException {
        List<SellerReport> sellerReports = new ArrayList<>();
        
        for (String sellerId : salesBySeller.keySet()) {
            Seller seller = sellers.get(sellerId);
            List<Sale> sales = salesBySeller.get(sellerId);
            
            double totalSold = 0;
            for (Sale sale : sales) {
                totalSold += sale.getProduct().getPrice() * sale.getQuantity();
            }
            
            sellerReports.add(new SellerReport(seller, totalSold));
        }
        
        // Sort by total sold (highest to lowest)
        Collections.sort(sellerReports, Comparator.comparingDouble(SellerReport::getTotalSold).reversed());
        
        // Generate CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "reporte_vendedores.csv"))) {
            // Header
            writer.write("Tipo Documento,Número Documento,Nombre,Apellido,Total Vendido");
            writer.newLine();
            
            // Data
            for (SellerReport report : sellerReports) {
                Seller seller = report.getSeller();
                // Usar punto como separador decimal para el CSV
                String formattedTotal = String.format("%.2f", report.getTotalSold()).replace('.', ',');
                writer.write(String.format("%s,%s,%s,%s,%s",
                    seller.getDocumentType(),
                    seller.getDocumentNumber(),
                    seller.getName(),
                    seller.getLastName(),
                    formattedTotal));
                writer.newLine();
            }
        }
        
        System.out.println("Reporte de vendedores generado: reporte_vendedores.csv");
    }
    
    /**
     * Generates the product report ordered by quantity sold
     * @throws IOException if there's an error writing the file
     */
    private void createProductReport() throws IOException {
        List<ProductReport> productReports = new ArrayList<>();
        
        for (String productId : totalProductsSold.keySet()) {
            Product product = products.get(productId);
            int quantitySold = totalProductsSold.get(productId);
            
            productReports.add(new ProductReport(product, quantitySold));
        }
        
        // Sort by quantity sold (highest to lowest)
        Collections.sort(productReports, Comparator.comparingInt(ProductReport::getQuantitySold).reversed());
        
        // Generate CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "reporte_productos.csv"))) {
            // Header
            writer.write("ID,Nombre,Precio,Cantidad Vendida");
            writer.newLine();
            
            // Data
            for (ProductReport report : productReports) {
                Product product = report.getProduct();
                // Usar coma como separador decimal para el CSV
                String formattedPrice = String.format("%.2f", product.getPrice()).replace('.', ',');
                writer.write(String.format("%s,%s,%s,%d",
                    product.getId(),
                    product.getName(),
                    formattedPrice,
                    report.getQuantitySold()));
                writer.newLine();
            }
        }
        
        System.out.println("Reporte de productos generado: reporte_productos.csv");
    }
    
    /**
     * Internal class to represent a seller
     */
    private static class Seller {
        private String documentType;
        private String documentNumber;
        private String name;
        private String lastName;
        
        public Seller(String documentType, String documentNumber, String name, String lastName) {
            this.documentType = documentType;
            this.documentNumber = documentNumber;
            this.name = name;
            this.lastName = lastName;
        }
        
        public String getDocumentType() {
            return documentType;
        }
        
        public String getDocumentNumber() {
            return documentNumber;
        }
        
        public String getName() {
            return name;
        }
        
        public String getLastName() {
            return lastName;
        }
    }
    
    /**
     * Internal class to represent a product
     */
    private static class Product {
        private String id;
        private String name;
        private double price;
        
        public Product(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public double getPrice() {
            return price;
        }
    }
    
    /**
     * Internal class to represent a sale
     */
    private static class Sale {
        private Product product;
        private int quantity;
        
        public Sale(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        
        public Product getProduct() {
            return product;
        }
        
        public int getQuantity() {
            return quantity;
        }
    }
    
    /**
     * Internal class for the seller report
     */
    private static class SellerReport {
        private Seller seller;
        private double totalSold;
        
        public SellerReport(Seller seller, double totalSold) {
            this.seller = seller;
            this.totalSold = totalSold;
        }
        
        public Seller getSeller() {
            return seller;
        }
        
        public double getTotalSold() {
            return totalSold;
        }
    }
    
    /**
     * Internal class for the product report
     */
    private static class ProductReport {
        private Product product;
        private int quantitySold;
        
        public ProductReport(Product product, int quantitySold) {
            this.product = product;
            this.quantitySold = quantitySold;
        }
        
        public Product getProduct() {
            return product;
        }
        
        public int getQuantitySold() {
            return quantitySold;
        }
    }
}