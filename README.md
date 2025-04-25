# E-Commerce Desktop App (Java Swing)

This is a desktop-based e-commerce application built with **Java Swing**. The app supports both **Admin** and **User** roles, providing core e-commerce functionalities such as product listing, shopping cart, order placement, and admin order management.

---

## Features

### User Features:
- **Register/Login:** Secure authentication with role detection (admin/user).
- **View Products:** Browse product categories with product cards showing image, name, and price.
- **Cart System:** Add items to a cart and remove or modify quantities.
- **Place Orders:** Checkout and simulate order placement.
- **View Order History:** Track past orders in a simple UI.

### Admin Features:
- **Admin Login:** Access with predefined credentials.
- **View All Orders:** Monitor user orders in a tabular format.
- **Product Management:** (Optional future feature) Add/Edit/Remove products.

### Other Features:
- **Data Persistence:** Uses `.txt` and `.dat` files for storing users and orders without using a database.
- **Product Images:** Each product card displays an image loaded from the local file system.
- **Responsive UI:** Scrollable and organized panels using `JPanel`, `CardLayout`, and `GridLayout`.


---

## How to Run the Project

### Requirements:
- Java 8 or later
- Any Java IDE (like IntelliJ IDEA, Eclipse) or command line

### Steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/RageNUB/simple-java-project.git
   cd simple-java-project
   ```

2. **Open in IDE:**
   - Open the project folder in IntelliJ or Eclipse.
   - Make sure the `src/` folder is marked as the source root.

3. **Run the app:**
   - Run the `Main.java` class.
   - The login screen will appear.

4. **Test Admin Login:**
   - **Username:** `admin`
   - **Password:** `admin`

5. **Add product images:**
   - Ensure your images are placed in the correct path, e.g., `src/images/phone.jpg`, and update the product image path accordingly in the code.

---

## Notes

- Product and user data is stored locally, so no database setup is needed.
- All images are loaded from local `src/images` folder; update the path if running from a JAR.

---

## License

This project is for educational purposes. You can modify or reuse it for your academic or personal use.

---

## Author

Developed by Jubaidul Islam Rimon  
Department of Software Engineering | Daffodil International University