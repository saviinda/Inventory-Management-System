package com.inventory.controller;



import com.inventory.model.Product;
import com.inventory.view.DatabaseManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, String> supplierColumn;

    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private TextField supplierField;
    @FXML private TextField searchField;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    private DatabaseManager databaseManager;
    private ObservableList<Product> productList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseManager = new DatabaseManager();

        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));

        // Load data
        loadProductData();

        // Set up table selection listener
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateFields(newValue);
                    }
                }
        );

        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts(newValue);
        });
    }

    private void loadProductData() {
        productList = databaseManager.getAllProducts();
        productTable.setItems(productList);
    }

    private void populateFields(Product product) {
        nameField.setText(product.getName());
        categoryField.setText(product.getCategory());
        quantityField.setText(String.valueOf(product.getQuantity()));
        priceField.setText(String.valueOf(product.getPrice()));
        supplierField.setText(product.getSupplier());
    }

    @FXML
    private void handleAddProduct() {
        if (validateInput()) {
            Product product = createProductFromInput();
            if (databaseManager.addProduct(product)) {
                loadProductData();
                clearFields();
                showAlert("Success", "Product added successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to add product!", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleUpdateProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null && validateInput()) {
            selectedProduct.setName(nameField.getText());
            selectedProduct.setCategory(categoryField.getText());
            selectedProduct.setQuantity(Integer.parseInt(quantityField.getText()));
            selectedProduct.setPrice(Double.parseDouble(priceField.getText()));
            selectedProduct.setSupplier(supplierField.getText());

            if (databaseManager.updateProduct(selectedProduct)) {
                loadProductData();
                clearFields();
                showAlert("Success", "Product updated successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to update product!", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Warning", "Please select a product to update!", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Product");
            confirmAlert.setContentText("Are you sure you want to delete this product?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (databaseManager.deleteProduct(selectedProduct.getId())) {
                    loadProductData();
                    clearFields();
                    showAlert("Success", "Product deleted successfully!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to delete product!", Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Warning", "Please select a product to delete!", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleClearFields() {
        clearFields();
        productTable.getSelectionModel().clearSelection();
    }

    private void clearFields() {
        nameField.clear();
        categoryField.clear();
        quantityField.clear();
        priceField.clear();
        supplierField.clear();
    }

    private Product createProductFromInput() {
        return new Product(
                0, // ID will be auto-generated
                nameField.getText(),
                categoryField.getText(),
                Integer.parseInt(quantityField.getText()),
                Double.parseDouble(priceField.getText()),
                supplierField.getText()
        );
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty() ||
                categoryField.getText().trim().isEmpty() ||
                quantityField.getText().trim().isEmpty() ||
                priceField.getText().trim().isEmpty() ||
                supplierField.getText().trim().isEmpty()) {

            showAlert("Validation Error", "Please fill in all fields!", Alert.AlertType.WARNING);
            return false;
        }

        try {
            Integer.parseInt(quantityField.getText());
            Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numbers for quantity and price!", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            productTable.setItems(productList);
        } else {
            ObservableList<Product> filteredList = productList.filtered(product ->
                    product.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                            product.getCategory().toLowerCase().contains(searchText.toLowerCase()) ||
                            product.getSupplier().toLowerCase().contains(searchText.toLowerCase())
            );
            productTable.setItems(filteredList);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}