package com.uniloop.service;

import com.uniloop.model.Request;
import com.uniloop.model.RequestGroup;
import com.uniloop.repository.RequestRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RequestService {

    private final RequestRepository requestRepository =
            new RequestRepository();

    public int getItemIdByRequestId(int requestId) {

        return requestRepository.getItemIdByRequestId(requestId);
    }

    public boolean requestItem(int itemId, int buyerId) {

        return requestRepository.createRequest(itemId, buyerId);
    }

    public List<Request> getRequestsForSeller(int sellerId) {

        return requestRepository.getRequestsForSeller(sellerId);
    }

    public List<Request> getActiveRequestsForItem(int itemId, int sellerId) {
        List<Request> itemRequests = new ArrayList<>();
        for (Request request : requestRepository.getRequestsForSeller(sellerId)) {
            if (request.getItemId() == itemId
                    && ("PENDING".equals(request.getRequestStatus())
                    || "ACCEPTED".equals(request.getRequestStatus()))) {
                itemRequests.add(request);
            }
        }
        return itemRequests;
    }

    public boolean updateRequestStatus(int requestId, String status, int sellerId) {

        if (status.equals("REJECTED")) {

            return requestRepository.updateRequestStatusForSeller(
                    requestId,
                    status,
                    sellerId
            );
        }

        if (status.equals("ACCEPTED")) {

            int itemId = requestRepository.getItemIdByRequestIdForSeller(requestId, sellerId);

            if (itemId == -1) {
                return false;
            }

            boolean accepted =
                    requestRepository.updateRequestStatusForSeller(
                            requestId,
                            "ACCEPTED",
                            sellerId
                    );

            if (!accepted) {
                return false;
            }

            boolean itemUpdated =
                    requestRepository.updateItemStatus(
                            itemId,
                            "UNAVAILABLE",
                            sellerId
                    );

            if (!itemUpdated) {
                return false;
            }

            return requestRepository.rejectOtherRequests(
                    itemId,
                    requestId,
                    sellerId
            );
        }

        return false;
    }

    public boolean cancelRequest(int requestId, int buyerId) {

        return requestRepository.cancelRequest(
                requestId,
                buyerId
        );
    }

    public String getRequestStatus(int itemId, int buyerId) {

        return requestRepository.getRequestStatus(
                itemId,
                buyerId
        );
    }

    public Request getLatestRequest(int itemId, int buyerId) {

        return requestRepository.getLatestRequest(
                itemId,
                buyerId
        );
    }

    // Get active requests grouped by item
    public List<RequestGroup> getRequestGroupsForSeller(int sellerId) {

        List<Request> allRequests =
                requestRepository.getRequestsForSeller(sellerId);

        // Keep only requests that should appear on the main Requests page
        List<Request> activeRequests = new ArrayList<>();

        for (Request request : allRequests) {

            if (request.getRequestStatus().equals("PENDING")
                    || request.getRequestStatus().equals("ACCEPTED")) {

                activeRequests.add(request);
            }
        }

        // Group requests by item
        Map<Integer, RequestGroup> groupedRequests =
                new LinkedHashMap<>();

        for (Request request : activeRequests) {

            int itemId = request.getItemId();

            if (!groupedRequests.containsKey(itemId)) {

                RequestGroup group = new RequestGroup();

                group.setItemId(itemId);
                group.setItemName(request.getItemName());
                group.setImagePath(request.getImagePath());
                group.setRequests(new ArrayList<>());

                groupedRequests.put(itemId, group);
            }

            groupedRequests
                    .get(itemId)
                    .getRequests()
                    .add(request);
        }

        // Sort requests inside each item:
        // newest request first
        for (RequestGroup group : groupedRequests.values()) {

            group.getRequests().sort(
                    Comparator.comparing(
                            Request::getRequestDate
                    ).reversed()
            );
        }

        // Sort item groups by their newest request
        List<RequestGroup> groups =
                new ArrayList<>(groupedRequests.values());

        groups.sort(
                Comparator.comparing(
                        (RequestGroup group) -> group.getRequests()
                                .get(0)
                                .getRequestDate()
                ).reversed()
        );

        return groups;
    }

    public List<RequestGroup> getRequestHistoryGroupsForSeller(int sellerId) {

        List<Request> allRequests =
                requestRepository.getRequestsForSeller(sellerId);

        // Keep only rejected and cancelled requests
        List<Request> historyRequests = new ArrayList<>();

        for (Request request : allRequests) {

            if (request.getRequestStatus().equals("REJECTED")
                    || request.getRequestStatus().equals("CANCELLED")) {

                historyRequests.add(request);
            }
        }

        // Group requests by item
        Map<Integer, RequestGroup> groupedRequests =
                new LinkedHashMap<>();

        for (Request request : historyRequests) {

            int itemId = request.getItemId();

            if (!groupedRequests.containsKey(itemId)) {

                RequestGroup group = new RequestGroup();

                group.setItemId(itemId);
                group.setItemName(request.getItemName());
                group.setImagePath(request.getImagePath());
                group.setRequests(new ArrayList<>());

                groupedRequests.put(itemId, group);
            }

            groupedRequests
                    .get(itemId)
                    .getRequests()
                    .add(request);
        }

        // Newest request first inside each item
        for (RequestGroup group : groupedRequests.values()) {

            group.getRequests().sort(
                    Comparator.comparing(
                            Request::getRequestDate
                    ).reversed()
            );
        }

        // Items with newest activity first
        List<RequestGroup> groups =
                new ArrayList<>(groupedRequests.values());

        groups.sort(
                Comparator.comparing(
                        (RequestGroup group) -> group.getRequests()
                                .get(0)
                                .getRequestDate()
                ).reversed()
        );

        return groups;
    }

    public List<Request> getRequestsForBuyer(int buyerId) {

        return requestRepository.getRequestsForBuyer(buyerId);
    }

    // Get buyer request history grouped by item
    public List<RequestGroup> getRequestHistoryGroupsForBuyer(int buyerId) {

        List<Request> allRequests =
                requestRepository.getRequestsForBuyer(buyerId);

        // Keep only rejected and cancelled requests
        List<Request> historyRequests = new ArrayList<>();

        for (Request request : allRequests) {

            if (request.getRequestStatus().equals("REJECTED")
                    || request.getRequestStatus().equals("CANCELLED")) {

                historyRequests.add(request);
            }
        }

        // Group requests by item
        Map<Integer, RequestGroup> groupedRequests =
                new LinkedHashMap<>();

        for (Request request : historyRequests) {

            int itemId = request.getItemId();

            if (!groupedRequests.containsKey(itemId)) {

                RequestGroup group = new RequestGroup();

                group.setItemId(itemId);
                group.setItemName(request.getItemName());
                group.setImagePath(request.getImagePath());
                group.setRequests(new ArrayList<>());

                groupedRequests.put(itemId, group);
            }

            groupedRequests
                    .get(itemId)
                    .getRequests()
                    .add(request);
        }

        // Newest request first inside each item
        for (RequestGroup group : groupedRequests.values()) {

            group.getRequests().sort(
                    Comparator.comparing(
                            Request::getRequestDate
                    ).reversed()
            );
        }

        // Items with newest activity first
        List<RequestGroup> groups =
                new ArrayList<>(groupedRequests.values());

        groups.sort(
                Comparator.comparing(
                        (RequestGroup group) -> group.getRequests()
                                .get(0)
                                .getRequestDate()
                ).reversed()
        );

        return groups;
    }
}
