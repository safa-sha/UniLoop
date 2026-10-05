package com.uniloop.repository;

import com.uniloop.DatabaseConnection;
import com.uniloop.model.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ItemRepository {

    // Get all AVAILABLE items for Browse
    public List<Item> getAllItems() {

        List<Item> items = new ArrayList<>();

        String sql = """
                SELECT *
                FROM items
                WHERE item_status = 'AVAILABLE'
                ORDER BY item_id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Item item = new Item();

                item.setItemId(
                        resultSet.getInt("item_id")
                );

                item.setSellerId(
                        resultSet.getInt("seller_id")
                );

                item.setCategoryId(
                        resultSet.getInt("category_id")
                );

                item.setItemName(
                        resultSet.getString("item_name")
                );

                item.setDescription(
                        resultSet.getString("description")
                );

                item.setPrice(
                        resultSet.getDouble("price")
                );

                item.setImagePath(
                        resultSet.getString("image_path")
                );

                item.setCondition(
                        resultSet.getString("item_condition")
                );

                item.setListingType(
                        resultSet.getString("listing_type")
                );

                item.setItemStatus(
                        resultSet.getString("item_status")
                );

                item.setDatePosted(
                        resultSet.getString("date_posted")
                );

                items.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }


    // Get latest AVAILABLE items for homepage
    public List<Item> getLatestItems() {

        List<Item> items = new ArrayList<>();

        String sql = """
                SELECT *
                FROM items
                WHERE item_status = 'AVAILABLE'
                ORDER BY item_id DESC
                LIMIT 3
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                Item item = new Item();

                item.setItemId(
                        resultSet.getInt("item_id")
                );

                item.setSellerId(
                        resultSet.getInt("seller_id")
                );

                item.setCategoryId(
                        resultSet.getInt("category_id")
                );

                item.setItemName(
                        resultSet.getString("item_name")
                );

                item.setDescription(
                        resultSet.getString("description")
                );

                item.setPrice(
                        resultSet.getDouble("price")
                );

                item.setImagePath(
                        resultSet.getString("image_path")
                );

                item.setCondition(
                        resultSet.getString("item_condition")
                );

                item.setListingType(
                        resultSet.getString("listing_type")
                );

                item.setItemStatus(
                        resultSet.getString("item_status")
                );

                item.setDatePosted(
                        resultSet.getString("date_posted")
                );

                items.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }


    // Get AVAILABLE items by category for Browse
    public List<Item> getItemsByCategory(int categoryId) {

        List<Item> items = new ArrayList<>();

        String sql = """
                SELECT *
                FROM items
                WHERE category_id = ?
                AND item_status = 'AVAILABLE'
                ORDER BY item_id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    Item item = new Item();

                    item.setItemId(
                            resultSet.getInt("item_id")
                    );

                    item.setSellerId(
                            resultSet.getInt("seller_id")
                    );

                    item.setCategoryId(
                            resultSet.getInt("category_id")
                    );

                    item.setItemName(
                            resultSet.getString("item_name")
                    );

                    item.setDescription(
                            resultSet.getString("description")
                    );

                    item.setPrice(
                            resultSet.getDouble("price")
                    );

                    item.setImagePath(
                            resultSet.getString("image_path")
                    );

                    item.setCondition(
                            resultSet.getString("item_condition")
                    );

                    item.setListingType(
                            resultSet.getString("listing_type")
                    );

                    item.setItemStatus(
                            resultSet.getString("item_status")
                    );

                    item.setDatePosted(
                            resultSet.getString("date_posted")
                    );

                    items.add(item);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }


    // Get seller's AVAILABLE items
    public List<Item> getAvailableItemsBySeller(int sellerId) {

        List<Item> items = new ArrayList<>();

        String sql = """
                SELECT *
                FROM items
                WHERE seller_id = ?
                AND item_status = 'AVAILABLE'
                ORDER BY item_id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, sellerId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    Item item = new Item();

                    item.setItemId(
                            resultSet.getInt("item_id")
                    );

                    item.setSellerId(
                            resultSet.getInt("seller_id")
                    );

                    item.setCategoryId(
                            resultSet.getInt("category_id")
                    );

                    item.setItemName(
                            resultSet.getString("item_name")
                    );

                    item.setDescription(
                            resultSet.getString("description")
                    );

                    item.setPrice(
                            resultSet.getDouble("price")
                    );

                    item.setImagePath(
                            resultSet.getString("image_path")
                    );

                    item.setCondition(
                            resultSet.getString("item_condition")
                    );

                    item.setListingType(
                            resultSet.getString("listing_type")
                    );

                    item.setItemStatus(
                            resultSet.getString("item_status")
                    );

                    item.setDatePosted(
                            resultSet.getString("date_posted")
                    );

                    items.add(item);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }


    // Get seller's UNAVAILABLE items for Listing History
    public List<Item> getUnavailableItemsBySeller(int sellerId) {

        List<Item> items = new ArrayList<>();

        String sql = """
                SELECT *
                FROM items
                WHERE seller_id = ?
                AND item_status = 'UNAVAILABLE'
                ORDER BY item_id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, sellerId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    Item item = new Item();

                    item.setItemId(
                            resultSet.getInt("item_id")
                    );

                    item.setSellerId(
                            resultSet.getInt("seller_id")
                    );

                    item.setCategoryId(
                            resultSet.getInt("category_id")
                    );

                    item.setItemName(
                            resultSet.getString("item_name")
                    );

                    item.setDescription(
                            resultSet.getString("description")
                    );

                    item.setPrice(
                            resultSet.getDouble("price")
                    );

                    item.setImagePath(
                            resultSet.getString("image_path")
                    );

                    item.setCondition(
                            resultSet.getString("item_condition")
                    );

                    item.setListingType(
                            resultSet.getString("listing_type")
                    );

                    item.setItemStatus(
                            resultSet.getString("item_status")
                    );

                    item.setDatePosted(
                            resultSet.getString("date_posted")
                    );

                    items.add(item);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }


    // Get an item by ID regardless of its status
    public Item getItemById(int itemId) {

        String sql = """
                SELECT i.*, c.category_name, u.name AS seller_name
                FROM items i
                LEFT JOIN categories c ON c.category_id = i.category_id
                LEFT JOIN users u ON u.user_id = i.seller_id
                WHERE i.item_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    Item item = new Item();

                    item.setItemId(
                            resultSet.getInt("item_id")
                    );

                    item.setSellerId(
                            resultSet.getInt("seller_id")
                    );

                    item.setCategoryId(
                            resultSet.getInt("category_id")
                    );

                    item.setItemName(
                            resultSet.getString("item_name")
                    );

                    item.setDescription(
                            resultSet.getString("description")
                    );

                    item.setPrice(
                            resultSet.getDouble("price")
                    );

                    item.setImagePath(
                            resultSet.getString("image_path")
                    );

                    item.setCondition(
                            resultSet.getString("item_condition")
                    );

                    item.setListingType(
                            resultSet.getString("listing_type")
                    );

                    item.setItemStatus(
                            resultSet.getString("item_status")
                    );

                    item.setDatePosted(
                            resultSet.getString("date_posted")
                    );
                    item.setCategoryName(resultSet.getString("category_name"));
                    item.setSellerName(resultSet.getString("seller_name"));

                    return item;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public boolean updateItem(int itemId, int sellerId, int categoryId,
                              String itemName, String description, double price,
                              String imagePath, String condition, String listingType) {
        String sql = "UPDATE items SET category_id=?, item_name=?, description=?, price=?, image_path=COALESCE(?, image_path), item_condition=?, listing_type=? WHERE item_id=? AND seller_id=?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            statement.setString(2, itemName);
            statement.setString(3, description);
            statement.setDouble(4, price);
            statement.setString(5, imagePath);
            statement.setString(6, condition);
            statement.setString(7, listingType);
            statement.setInt(8, itemId);
            statement.setInt(9, sellerId);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void markItemUnavailable(int itemId) {

    String sql =
            "UPDATE items SET item_status = 'UNAVAILABLE' WHERE item_id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, itemId);
        statement.executeUpdate();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
