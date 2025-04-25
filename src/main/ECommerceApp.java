package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ECommerceApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StoreFrame().setVisible(true));
    }
}

class StoreFrame extends JFrame {
    static List<Product> allProducts = new ArrayList<>();
    static List<Order> allOrders = new ArrayList<>();
    static List<User> allUsers = new ArrayList<>();

    static User currentUser = null;
    static List<Product> cart = new ArrayList<>();

    static {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("products.dat"))) {
            allProducts = (List<Product>) ois.readObject();
        } catch (Exception ignored) {}

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("orders.dat"))) {
            allOrders = (List<Order>) ois.readObject();
        } catch (Exception ignored) {}

        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3)
                    allUsers.add(new User(parts[0], parts[1], Boolean.parseBoolean(parts[2])));
            }
        } catch (IOException ignored) {}
    }

    static void saveProducts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("products.dat"))) {
            oos.writeObject(allProducts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveOrders() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("orders.dat"))) {
            oos.writeObject(allOrders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StoreFrame() {
        setTitle("E-Commerce App");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        showLoginScreen();
    }

    void styleButton(JButton button) {
        button.setBackground(new Color(59, 89, 182));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Tahoma", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        button.setArc
    }

    void showLoginScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        styleButton(loginBtn);
        styleButton(registerBtn);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(loginBtn, gbc);

        gbc.gridx = 1;
        panel.add(registerBtn, gbc);

        setContentPane(panel);
        revalidate();

        loginBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            for (User u : allUsers) {
                if(u.username.equals("admin") && u.password.equals("admin")){
                    u.isAdmin = true;
                }
                if (u.username.equals(user) && u.password.equals(pass)) {
                    currentUser = u;
                    if (currentUser.isAdmin) {
                        showAdminScreen();
                    } else {
                        showUserScreen();
                    }
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid login");
        });

        registerBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            boolean exists = allUsers.stream().anyMatch(u -> u.username.equals(user));
            if (!exists) {
                User u = new User(user, pass, false);
                allUsers.add(u);
                try (BufferedWriter bw = new BufferedWriter(new FileWriter("users.txt", true))) {
                    bw.write(user + "," + pass + ",false\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "Registered successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Username exists");
            }
        });
    }

    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(200, 250));

        JLabel nameLabel = new JLabel(product.name, SwingConstants.CENTER);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        ImageIcon icon = new ImageIcon(product.imagePath);
        Image scaledImage = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        JLabel priceLabel = new JLabel("Price: $" + product.price, SwingConstants.CENTER);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(imageLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(priceLabel);

        if(!currentUser.isAdmin) {
        JButton addToCartButton = new JButton("Add to Cart");
            addToCartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            styleButton(addToCartButton);
            addToCartButton.addActionListener(e -> {
                cart.add(product);
                JOptionPane.showMessageDialog(this, product.name + " Added to cart!");
            });
            card.add(Box.createVerticalStrut(10));
            card.add(addToCartButton);
        }

        return card;
    }

    void showUserScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel();
        JButton cartBtn = new JButton("Cart");
        JButton orderHistoryBtn = new JButton("Orders");
        JButton logoutBtn = new JButton("Logout");
        styleButton(cartBtn);
        styleButton(orderHistoryBtn);
        styleButton(logoutBtn);

        top.add(cartBtn);
        top.add(orderHistoryBtn);
        top.add(logoutBtn);

        panel.add(top, BorderLayout.NORTH);

        JPanel productsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        int cols = 5;
        for (int i = 0; i < allProducts.size(); i++) {
            JPanel card = createProductCard(allProducts.get(i));

            gbc.gridx = i % cols;
            gbc.gridy = i / cols;
            gbc.weightx = 1.0 / cols;
            gbc.gridwidth = 1;

            productsPanel.add(card, gbc);
        }
        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);


        cartBtn.addActionListener(e -> showCartDialog());
        orderHistoryBtn.addActionListener(e -> showUserOrdersDialog());
        logoutBtn.addActionListener(e -> {
            currentUser = null;
            cart.clear();
            showLoginScreen();
        });

        setContentPane(panel);
        revalidate();
    }

    void showAdminScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel();
        JButton addProductBtn = new JButton("Add Product");
        JButton viewAllOrdersBtn = new JButton("View All Orders");
        JButton logoutBtn = new JButton("Logout");
        styleButton(addProductBtn);
        styleButton(viewAllOrdersBtn);
        styleButton(logoutBtn);

        top.add(addProductBtn);
        top.add(viewAllOrdersBtn);
        top.add(logoutBtn);

        panel.add(top, BorderLayout.NORTH);

        JPanel productsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        int cols = 5;
        for (int i = 0; i < allProducts.size(); i++) {
            JPanel card = createProductCard(allProducts.get(i));

            gbc.gridx = i % cols;
            gbc.gridy = i / cols;
            gbc.weightx = 1.0 / cols;
            gbc.gridwidth = 1;

            productsPanel.add(card, gbc);
        }
        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        addProductBtn.addActionListener(e -> showAddProductDialog());
        viewAllOrdersBtn.addActionListener(e -> showAllOrdersDialog());
        logoutBtn.addActionListener(e -> {
            currentUser = null;
            cart.clear();
            showLoginScreen();
        });

        setContentPane(panel);
        revalidate();
    }

    void showCartDialog() {
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (Product p : cart) {
            sb.append(p.name).append(" - $").append(p.price).append("\n");
            total += p.price;
        }
        sb.append("\nTotal: $").append(total);

        int confirm = JOptionPane.showConfirmDialog(this, sb.toString(), "Cart", JOptionPane.OK_CANCEL_OPTION);
        if (confirm == JOptionPane.OK_OPTION && !cart.isEmpty()) {
            Order order = new Order(currentUser.username, new ArrayList<>(cart), total);
            allOrders.add(order);
            saveOrders();
            cart.clear();
            JOptionPane.showMessageDialog(this, "Order placed successfully");
        }
    }

    void showUserOrdersDialog() {
        StringBuilder sb = new StringBuilder("Your Orders:\n\n");
        for (Order o : allOrders) {
            if (o.username.equals(currentUser.username)) {
                for (Product p : o.items) {
                    sb.append("Product: ").append(p.name).append(", Price: $").append(p.price).append("\n");
                }
                sb.append("Total: $").append(o.total).append("\n---\n");
            }
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    void showAllOrdersDialog() {
        StringBuilder sb = new StringBuilder("All Orders:\n\n");
        for (Order o : allOrders) {
            sb.append("User: ").append(o.username).append("\n");
            for (Product p : o.items) {
                sb.append("  Product: ").append(p.name).append(", Price: $").append(p.price).append("\n");
            }
            sb.append("Total: $").append(o.total).append("\n---\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    void showAddProductDialog() {
        JTextField nameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField imagePathField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Image Path:"));
        panel.add(imagePathField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Product", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String category = categoryField.getText();
                double price = Double.parseDouble(priceField.getText());
                String imagePath = imagePathField.getText();
                Product product = new Product(name, category, price, imagePath);
                allProducts.add(product);
                saveProducts();
                showAdminScreen();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input");
            }
        }
    }
}

class Product implements Serializable {
    String name, category, imagePath;
    double price;
    public Product(String name, String category, double price, String imagePath) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.imagePath = imagePath;
    }
}

class Order implements Serializable {
    String username;
    List<Product> items;
    double total;
    public Order(String username, List<Product> items, double total) {
        this.username = username;
        this.items = items;
        this.total = total;
    }
}

class User implements Serializable {
    String username, password;
    boolean isAdmin;
    public User(String username, String password, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }
}