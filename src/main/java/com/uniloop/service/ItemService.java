package com.uniloop.service;

import com.uniloop.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ItemService {

    public boolean addItem(
            int sellerId,
            int categoryId,
            String itemName,
            String description,
            double price,
            String imagePath,
            String condition,
            String listingType) {

        String sql = """
                INSERT INTO items
                (seller_id, category_id, item_name, description, price,
                 image_path, item_condition, listing_type, item_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'AVAILABLE')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, sellerId);
            statement.setInt(2, categoryId);
            statement.setString(3, itemName);
            statement.setString(4, description);
            statement.setDouble(5, price);
            statement.setString(6, imagePath);
            statement.setString(7, condition);
            statement.setString(8, listingType);

            statement.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}