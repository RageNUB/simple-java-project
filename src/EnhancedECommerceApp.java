// EnhancedECommerceApp.java (Extended with Modern UI, Product Images, Cart Selection, and Payment + Admin Features + Persistence)

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class EnhancedECommerceApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}

class Product {
    String name;
    String category;
    double price;
    String imagePath;

    Product(String name, String category, double price, String imagePath) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.imagePath = imagePath;
    }

    public String toString() {
        return name + " - $" + price;
    }
}

class LoginFrame extends JFrame {
    JTextField usernameField;
    JPasswordField passwordField;
    JButton loginButton, registerButton;
    static Map<String, String> userCredentials = new HashMap<>();
    static Map<String, List<Product>> userOrderHistory = new HashMap<>();
    static Map<String, List<Product>> userCartHistory = new HashMap<>();

    LoginFrame() {
        loadCredentials();
        loadOrderData();
        setTitle("Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 200);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        add(loginButton);
        add(registerButton);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            if (user.equals("admin") && pass.equals("admin")) {
                new AdminPanel();
                dispose();
            } else if (userCredentials.containsKey(user) && userCredentials.get(user).equals(pass)) {
                new StoreFrame(user);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        });

        registerButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            if (!userCredentials.containsKey(user)) {
                userCredentials.put(user, pass);
                userOrderHistory.put(user, new ArrayList<>());
                userCartHistory.put(user, new ArrayList<>());
                saveCredentials();
                saveOrderData();
                JOptionPane.showMessageDialog(this, "Registration successful!");
            } else {
                JOptionPane.showMessageDialog(this, "User already exists.");
            }
        });

        setVisible(true);
    }

    void loadCredentials() {
        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) userCredentials.put(parts[0], parts[1]);
            }
        } catch (IOException ignored) {}
    }

    void saveCredentials() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("users.txt"))) {
            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                pw.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException ignored) {}
    }

    void loadOrderData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("orders.dat"))) {
            userOrderHistory = (Map<String, List<Product>>) ois.readObject();
        } catch (Exception ignored) {}
    }

    static void saveOrderData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("orders.dat"))) {
            oos.writeObject(userOrderHistory);
        } catch (IOException ignored) {}
    }
}

class StoreFrame extends JFrame {
    DefaultListModel<Product> productListModel;
    JList<Product> productList;
    JComboBox<String> categoryFilter;
    JTextField searchField;
    JButton addToCartButton, viewCartButton, viewHistoryButton, payButton, logoutButton;
    JLabel imageLabel;
    String currentUser;

    static List<Product> allProducts = new ArrayList<>(Arrays.asList(
            new Product("Phone", "Electronics", 699.99, "src/phone.jpg"),
            new Product("Laptop", "Electronics", 1099.99, "images/laptop.png"),
            new Product("Headphones", "Electronics", 199.99, "images/headphones.png"),
            new Product("Smart Watch", "Electronics", 299.99, "images/watch.png"),
            new Product("Shirt", "Clothing", 29.99, "images/shirt.png"),
            new Product("Pants", "Clothing", 49.99, "images/pants.png"),
            new Product("Shoes", "Clothing", 89.99, "images/shoes.png"),
            new Product("Hat", "Clothing", 19.99, "images/hat.png"),
            new Product("Java Book", "Books", 39.99, "images/book.png"),
            new Product("Design Patterns", "Books", 59.99, "images/patterns.png"),
            new Product("DSA Handbook", "Books", 44.99, "images/dsa.png")
    ));

    List<Product> cart = new ArrayList<>();

    StoreFrame(String user) {
        currentUser = user;
        setTitle("E-Commerce Store - Welcome " + user);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 550);
        setLayout(new BorderLayout(10, 10));

        categoryFilter = new JComboBox<>(new String[]{"All", "Electronics", "Clothing", "Books"});
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        logoutButton = new JButton("Logout");

        JPanel topPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Filter & Search"));
        topPanel.add(categoryFilter);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(logoutButton);

        productListModel = new DefaultListModel<>();
        productList = new JList<>(productListModel);
        productList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        productList.setCellRenderer(new ProductRenderer());

        JScrollPane scrollPane = new JScrollPane(productList);

        addToCartButton = new JButton("Add Selected to Cart");
        viewCartButton = new JButton("View Cart");
        viewHistoryButton = new JButton("Order History");
        payButton = new JButton("Pay Now");

        JPanel bottomPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        bottomPanel.add(addToCartButton);
        bottomPanel.add(viewCartButton);
        bottomPanel.add(viewHistoryButton);
        bottomPanel.add(payButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        updateProductList();

        categoryFilter.addActionListener(e -> updateProductList());

        searchButton.addActionListener(e -> {
            String query = searchField.getText().toLowerCase();
            productListModel.clear();
            for (Product p : allProducts) {
                if (p.name.toLowerCase().contains(query)) {
                    productListModel.addElement(p);
                }
            }
        });

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                LoginFrame.saveOrderData();
                dispose();
                new LoginFrame();
            }
        });

        addToCartButton.addActionListener(e -> {
            List<Product> selected = productList.getSelectedValuesList();
            if (!selected.isEmpty()) {
                cart.addAll(selected);
                JOptionPane.showMessageDialog(this, selected.size() + " item(s) added to cart.");
            }
        });

        viewCartButton.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Your cart is empty.");
                return;
            }
            StringBuilder cartList = new StringBuilder("Cart Items:\n");
            double total = 0;
            for (Product item : cart) {
                cartList.append("- ").append(item.name).append(" ($").append(item.price).append(")\n");
                total += item.price;
            }
            cartList.append("Total: $").append(String.format("%.2f", total));
            JOptionPane.showMessageDialog(this, cartList.toString());
        });

        payButton.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Your cart is empty.");
                return;
            }
            double total = 0;
            for (Product p : cart) total += p.price;
            int confirm = JOptionPane.showConfirmDialog(this, "Total: $" + total + "\nProceed to payment?", "Payment", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                LoginFrame.userOrderHistory.get(currentUser).addAll(cart);
                cart.clear();
                JOptionPane.showMessageDialog(this, "Payment Successful! Order placed.");
            }
        });

        viewHistoryButton.addActionListener(e -> {
            List<Product> orders = LoginFrame.userOrderHistory.get(currentUser);
            if (orders.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No previous orders.");
            } else {
                StringBuilder history = new StringBuilder("Your Orders:\n");
                for (Product p : orders) {
                    history.append("- ").append(p.name).append(" ($").append(p.price).append(")\n");
                }
                JOptionPane.showMessageDialog(this, history.toString());
            }
        });

        setVisible(true);
    }

    void updateProductList() {
        productListModel.clear();
        String category = (String) categoryFilter.getSelectedItem();
        for (Product p : allProducts) {
            if ("All".equals(category) || p.category.equals(category)) {
                productListModel.addElement(p);
            }
        }
    }

    static class ProductRenderer extends JPanel implements ListCellRenderer<Product> {
        JLabel nameLabel = new JLabel();
        JLabel priceLabel = new JLabel();
        JLabel image = new JLabel();

        ProductRenderer() {
            setLayout(new BorderLayout(5, 5));
            add(image, BorderLayout.WEST);

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.add(nameLabel);
            textPanel.add(priceLabel);
            add(textPanel, BorderLayout.CENTER);
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }

        public Component getListCellRendererComponent(JList<? extends Product> list, Product value, int index, boolean isSelected, boolean cellHasFocus) {
            nameLabel.setText(value.name);
            priceLabel.setText("$" + value.price);
            image.setIcon(new ImageIcon(new ImageIcon(value.imagePath).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
            setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
            return this;
        }
    }
}

class AdminPanel extends JFrame {
    JTextArea orderSummary;
    JButton addProductButton, logoutButton;

    AdminPanel() {
        setTitle("Admin - All Orders");
        setSize(600, 450);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        orderSummary = new JTextArea();
        orderSummary.setEditable(false);
        updateOrderSummary();

        addProductButton = new JButton("Add New Product");
        logoutButton = new JButton("Logout");

        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.add(addProductButton);
        topPanel.add(logoutButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(orderSummary), BorderLayout.CENTER);

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Logout admin?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame();
            }
        });

        addProductButton.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField categoryField = new JTextField();
            JTextField priceField = new JTextField();
            JTextField imageField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(4, 2));
            panel.add(new JLabel("Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Category:"));
            panel.add(categoryField);
            panel.add(new JLabel("Price:"));
            panel.add(priceField);
            panel.add(new JLabel("Image Path:"));
            panel.add(imageField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Add New Product", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText();
                    String category = categoryField.getText();
                    double price = Double.parseDouble(priceField.getText());
                    String image = imageField.getText();
                    StoreFrame.allProducts.add(new Product(name, category, price, image));
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid price format.");
                }
            }
        });

        setVisible(true);
    }

    void updateOrderSummary() {
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<String, List<Product>> entry : LoginFrame.userOrderHistory.entrySet()) {
            summary.append("User: ").append(entry.getKey()).append("\n");
            for (Product item : entry.getValue()) {
                summary.append("  - ").append(item.name).append(" ($").append(item.price).append(")\n");
            }
            summary.append("\n");
        }
        orderSummary.setText(summary.toString());
    }
}
