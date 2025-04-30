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

 //Clase Main como se indico para esta entrega
public class main {
    
    // Directorios de entrada y salida
    private static final String INPUT_DIR = "data/";
    private static final String OUTPUT_DIR = "reports/";
    
    private static final Map<String, Seller> sellers = new HashMap<>();
    private static final Map<String, Product> products = new HashMap<>();
    private static final Map<String, List<Sale>> salesBySeller = new HashMap<>();
    private static final Map<String, Integer> totalProductsSold = new HashMap<>();
        /**
     * Método principal que ejecuta el procesamiento de archivos y generación de reportes
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        System.out.println("Iniciando procesamiento de archivos de ventas...");
        
        try {
            // Crear directorio de salida si no existe
            File directory = new File(OUTPUT_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Cargar información de vendedores y productos
            loadSellers();
            loadProducts();
            
            // Procesar archivos de ventas
            processSalesFiles();
            
            // Generar reportes
            createSellerReport();
            createProductReport();
            
            System.out.println("Procesamiento completado exitosamente. Reportes generados en: " + OUTPUT_DIR);
        } catch (IOException e) {
            System.err.println("Error durante el procesamiento: " + e.getMessage());
            
        }
    }
    
    /**
     * Carga la información de vendedores desde el archivo correspondiente
     * @throws IOException si hay un error al leer el archivo
     */
    private static void loadSellers() throws IOException {
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
     * Carga la información de productos desde el archivo correspondiente
     * @throws IOException si hay un error al leer el archivo
     */
    private static void loadProducts() throws IOException {
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
     * Procesa todos los archivos de ventas en el directorio de entrada
     * @throws IOException si hay un error al leer los archivos
     */
    private static void processSalesFiles() throws IOException {
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
     * Procesa un archivo individual de ventas
     * @param file el archivo a procesar
     * @throws IOException si hay un error al leer el archivo
     */
    private static void processSaleFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Primera línea: identificación del vendedor
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
            
            // Leer ventas
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
                    
                    // Actualizar contador total de productos
                    totalProductsSold.put(productId, 
                        totalProductsSold.get(productId) + quantity);
                }
            }
        }
    }
    
    /**
     * Genera el reporte de vendedores ordenado por total de ventas
     * @throws IOException si hay un error al escribir el archivo
     */
    private static void createSellerReport() throws IOException {
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
        
        // Ordenar por total vendido (de mayor a menor)
        Collections.sort(sellerReports, Comparator.comparingDouble(SellerReport::getTotalSold).reversed());
        
        // Generar archivo CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "reporte_vendedores.csv"))) {
            // Encabezado
            writer.write("Tipo Documento,Número Documento,Nombre,Apellido,Total Vendido");
            writer.newLine();
            
            // Datos
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
     * Genera el reporte de productos ordenado por cantidad vendida
     * @throws IOException si hay un error al escribir el archivo
     */
    private static void createProductReport() throws IOException {
        List<ProductReport> productReports = new ArrayList<>();
        
        for (String productId : totalProductsSold.keySet()) {
            Product product = products.get(productId);
            int quantitySold = totalProductsSold.get(productId);
            
            productReports.add(new ProductReport(product, quantitySold));
        }
        
        // Ordenar por cantidad vendida (de mayor a menor)
        Collections.sort(productReports, Comparator.comparingInt(ProductReport::getQuantitySold).reversed());
        
        // Generar archivo CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "reporte_productos.csv"))) {
            // Encabezado
            writer.write("ID,Nombre,Precio,Cantidad Vendida");
            writer.newLine();
            
            // Datos
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
     * Clase interna para representar a un vendedor
     */
    private static class Seller {
        private final String documentType;
        private final String documentNumber;
        private final String name;
        private final String lastName;
        
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
     * Clase interna para representar un producto
     */
    private static class Product {
        private final String id;
        private final String name;
        private final double price;
        
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
     * Clase interna para representar una venta
     */
    private static class Sale {
        private final Product product;
        private final int quantity;
        
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
     * Clase interna para el reporte de vendedores
     */
    private static class SellerReport {
        private final Seller seller;
        private final double totalSold;
        
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
     * Clase interna para el reporte de productos
     */
    private static class ProductReport {
        private final Product product;
        private final int quantitySold;
        
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