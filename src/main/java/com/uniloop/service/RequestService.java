package com.uniloop.service;

import com.uniloop.model.Request;
import com.uniloop.repository.RequestRepository;

import java.util.List;

public class RequestService {

    private final RequestRepository requestRepository =
            new RequestRepository();

    public boolean requestItem(int itemId, int buyerId) {

        return requestRepository.createRequest(itemId, buyerId);
    }

    public List<Request> getRequestsForSeller(int sellerId) {

        return requestRepository.getRequestsForSeller(sellerId);
    }

    public boolean updateRequestStatus(int requestId, String status) {

        return requestRepository.updateRequestStatus(
                requestId,
                status
        );
    }
}