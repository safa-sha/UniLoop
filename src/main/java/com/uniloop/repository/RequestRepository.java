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

        // Check whether the item is still available
        String itemSql = """
                SELECT item_status
                FROM items
                WHERE item_id = ?
                """;

        // Check whether this buyer already requested the item
        String requestSql = """
                SELECT request_id
                FROM requests
                WHERE item_id = ?
                  AND buyer_id = ?
                  AND request_status IN ('PENDING', 'ACCEPTED')
                """;

        // Create the request
        String insertSql = """
                INSERT INTO requests
                (item_id, buyer_id, request_status)
                VALUES (?, ?, 'PENDING')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement itemStatement =
                     connection.prepareStatement(itemSql)) {

            // Check item status
            itemStatement.setInt(1, itemId);

            ResultSet itemResult =
                    itemStatement.executeQuery();

            if (!itemResult.next()) {
                return false;
            }

            String itemStatus =
                    itemResult.getString("item_status");

            // Don't allow requests for unavailable items
            if (!"AVAILABLE".equals(itemStatus)) {
                return false;
            }

            // Check for an existing request
            try (PreparedStatement requestStatement =
                         connection.prepareStatement(requestSql)) {

                requestStatement.setInt(1, itemId);
                requestStatement.setInt(2, buyerId);

                ResultSet requestResult =
                        requestStatement.executeQuery();

                if (requestResult.next()) {
                    return false;
                }
            }

            // Create new request
            try (PreparedStatement insertStatement =
                         connection.prepareStatement(insertSql)) {

                insertStatement.setInt(1, itemId);
                insertStatement.setInt(2, buyerId);

                insertStatement.executeUpdate();

                return true;
            }

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
                    i.item_name,
                    u.name AS buyer_name,
                    r.request_status,
                    r.request_date
                FROM requests r
                JOIN items i
                    ON r.item_id = i.item_id
                JOIN users u
                    ON r.buyer_id = u.user_id
                WHERE i.seller_id = ?
                ORDER BY r.request_id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, sellerId);

            ResultSet resultSet =
                    statement.executeQuery();

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

                request.setItemName(
                        resultSet.getString("item_name")
                );

                request.setBuyerName(
                        resultSet.getString("buyer_name")
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


    public boolean updateRequestStatus(
            int requestId,
            String status) {

        String sql = """
                UPDATE requests
                SET request_status = ?
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, requestId);

            statement.executeUpdate();

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    public int getItemIdByRequestId(int requestId) {

        String sql = """
                SELECT item_id
                FROM requests
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, requestId);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                return resultSet.getInt("item_id");
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return -1;
    }


    public boolean updateItemStatus(
            int itemId,
            String status) {

        String sql = """
                UPDATE items
                SET item_status = ?
                WHERE item_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, itemId);

            statement.executeUpdate();

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    public boolean rejectOtherRequests(
            int itemId,
            int acceptedRequestId) {

        String sql = """
                UPDATE requests
                SET request_status = 'REJECTED'
                WHERE item_id = ?
                  AND request_id <> ?
                  AND request_status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);
            statement.setInt(2, acceptedRequestId);

            statement.executeUpdate();

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    public boolean cancelRequest(int requestId, int buyerId) {

    String sql = """
            UPDATE requests
            SET request_status = 'CANCELLED'
            WHERE request_id = ?
              AND buyer_id = ?
              AND request_status = 'PENDING'
            """;

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, requestId);
        statement.setInt(2, buyerId);

        int rowsUpdated = statement.executeUpdate();

        return rowsUpdated > 0;

    } catch (Exception e) {

        e.printStackTrace();

        return false;
    }
}

public String getRequestStatus(int itemId, int buyerId) {

    String sql = """
            SELECT request_status
            FROM requests
            WHERE item_id = ?
              AND buyer_id = ?
            ORDER BY request_id DESC
            LIMIT 1
            """;

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, itemId);
        statement.setInt(2, buyerId);

        ResultSet resultSet =
                statement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getString("request_status");
        }

    } catch (Exception e) {

        e.printStackTrace();
    }

    return null;
}

public Request getLatestRequest(int itemId, int buyerId) {

    String sql = """
            SELECT
                request_id,
                item_id,
                buyer_id,
                request_status,
                request_date
            FROM requests
            WHERE item_id = ?
              AND buyer_id = ?
            ORDER BY request_id DESC
            LIMIT 1
            """;

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, itemId);
        statement.setInt(2, buyerId);

        ResultSet resultSet =
                statement.executeQuery();

        if (resultSet.next()) {

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

            return request;
        }

    } catch (Exception e) {

        e.printStackTrace();
    }

    return null;
}
}