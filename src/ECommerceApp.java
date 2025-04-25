import javax.swing.*;
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

    void showLoginScreen() {
        JPanel panel = new JPanel(new GridLayout(4, 4));
        setSize(400, 300);
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginBtn);
        panel.add(registerBtn);

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

    void showUserScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel();
        JButton cartBtn = new JButton("Cart");
        JButton orderHistoryBtn = new JButton("Orders");
        JButton logoutBtn = new JButton("Logout");

        top.add(cartBtn);
        top.add(orderHistoryBtn);
        top.add(logoutBtn);

        panel.add(top, BorderLayout.NORTH);

        JPanel productsPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        for (Product p : allProducts) {
            JPanel card = new JPanel(new BorderLayout());
            JLabel imgLabel = new JLabel();
            if (!p.imagePath.isEmpty()) imgLabel.setIcon(new ImageIcon(p.imagePath));
            JLabel nameLabel = new JLabel(p.name);
            JLabel priceLabel = new JLabel("$" + p.price);
            JButton addBtn = new JButton("Add to Cart");
            addBtn.addActionListener(e -> {
                cart.add(p);
                JOptionPane.showMessageDialog(this, p.name + " added to cart");
            });
            card.add(imgLabel, BorderLayout.CENTER);
            card.add(nameLabel, BorderLayout.NORTH);
            card.add(priceLabel, BorderLayout.SOUTH);
            card.add(addBtn, BorderLayout.EAST);
            productsPanel.add(card);
        }

        panel.add(new JScrollPane(productsPanel), BorderLayout.CENTER);
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

        top.add(addProductBtn);
        top.add(viewAllOrdersBtn);
        top.add(logoutBtn);

        panel.add(top, BorderLayout.NORTH);

        JPanel productsPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        for (Product p : allProducts) {
            JPanel card = new JPanel(new BorderLayout());
            JLabel imgLabel = new JLabel();
            if (!p.imagePath.isEmpty()) imgLabel.setIcon(new ImageIcon(p.imagePath));
            JLabel nameLabel = new JLabel(p.name);
            JLabel priceLabel = new JLabel("$" + p.price);
            card.add(imgLabel, BorderLayout.CENTER);
            card.add(nameLabel, BorderLayout.NORTH);
            card.add(priceLabel, BorderLayout.SOUTH);
            productsPanel.add(card);
        }

        panel.add(new JScrollPane(productsPanel), BorderLayout.CENTER);

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
