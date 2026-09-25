package com.uniloop.repository;

import com.uniloop.DatabaseConnection;
import com.uniloop.model.Request;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RequestRepository {

    public boolean createRequest(int itemId, int buyerId) {

        String sql = """
                INSERT INTO requests
                (item_id, buyer_id, request_status)
                VALUES (?, ?, 'PENDING')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);
            statement.setInt(2, buyerId);

            statement.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Request> getRequestsForSeller(int sellerId) {

        List<Request> requests = new ArrayList<>();

        String sql = """
                SELECT
                    r.request_id,
                    r.item_id,
                    r.buyer_id,
                    r.request_status,
                    r.request_date
                FROM requests r
                JOIN items i
                    ON r.item_id = i.item_id
                WHERE i.seller_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, sellerId);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Request request = new Request();

                request.setRequestId(
                        resultSet.getInt("request_id")
                );

                request.setItemId(
                        resultSet.getInt("item_id")
                );

                request.setBuyerId(
                        resultSet.getInt("buyer_id")
                );

                request.setRequestStatus(
                        resultSet.getString("request_status")
                );

                request.setRequestDate(
                        resultSet.getString("request_date")
                );

                requests.add(request);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return requests;
    }


    public boolean updateRequestStatus(int requestId, String status) {

        String sql = """
                UPDATE requests
                SET request_status = ?
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, requestId);

            statement.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}