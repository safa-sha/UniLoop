package com.uniloop.service;

import com.uniloop.model.Request;
import com.uniloop.repository.RequestRepository;

import java.util.List;

public class RequestService {

    private final RequestRepository requestRepository =
            new RequestRepository();


    public boolean requestItem(int itemId, int buyerId) {

        return requestRepository.createRequest(
                itemId,
                buyerId
        );
    }


    public List<Request> getRequestsForSeller(int sellerId) {

        return requestRepository.getRequestsForSeller(
                sellerId
        );
    }


    public boolean updateRequestStatus(
            int requestId,
            String status) {

        // Normal rejection
        if (status.equals("REJECTED")) {

            return requestRepository.updateRequestStatus(
                    requestId,
                    status
            );
        }


        // Accept request
        if (status.equals("ACCEPTED")) {

            // Find which item this request belongs to
            int itemId =
                    requestRepository.getItemIdByRequestId(
                            requestId
                    );

            // Request does not exist
            if (itemId == -1) {
                return false;
            }


            // Accept this request
            boolean accepted =
                    requestRepository.updateRequestStatus(
                            requestId,
                            "ACCEPTED"
                    );

            if (!accepted) {
                return false;
            }


            // Make the item unavailable
            boolean itemUpdated =
                    requestRepository.updateItemStatus(
                            itemId,
                            "UNAVAILABLE"
                    );

            if (!itemUpdated) {
                return false;
            }


            // Reject all other pending requests
            return requestRepository.rejectOtherRequests(
                    itemId,
                    requestId
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
}