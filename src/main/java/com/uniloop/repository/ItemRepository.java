package com.uniloop.repository;

import com.uniloop.DatabaseConnection;
import com.uniloop.model.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ItemRepository {

    public List<Item> getAllItems() {

        List<Item> items = new ArrayList<>();

        String sql = "SELECT * FROM items ORDER BY item_id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Item item = new Item();

                item.setItemId(resultSet.getInt("item_id"));
                item.setSellerId(resultSet.getInt("seller_id"));
                item.setCategoryId(resultSet.getInt("category_id"));
                item.setItemName(resultSet.getString("item_name"));
                item.setDescription(resultSet.getString("description"));
                item.setPrice(resultSet.getDouble("price"));
                item.setImagePath(resultSet.getString("image_path"));
                item.setCondition(resultSet.getString("item_condition"));
                item.setListingType(resultSet.getString("listing_type"));
                item.setItemStatus(resultSet.getString("item_status"));
                item.setDatePosted(resultSet.getString("date_posted"));

                items.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }
    public List<Item> getLatestItems() {

    List<Item> items = new ArrayList<>();

    String sql = """
            SELECT *
            FROM items
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
public List<Item> getItemsByCategory(int categoryId) {

    List<Item> items = new ArrayList<>();

    String sql = """
            SELECT *
            FROM items
            WHERE category_id = ?
            ORDER BY item_id DESC
            """;

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, categoryId);

        try (ResultSet resultSet = statement.executeQuery()) {

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
}