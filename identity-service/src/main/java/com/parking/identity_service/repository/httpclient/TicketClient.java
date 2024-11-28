package com.parking.identity_service.repository.httpclient;

import com.parking.identity_service.configuration.AuthenticationRequestInterceptor;
import com.parking.identity_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ticket-service", url = "http://localhost:8085/ticket", configuration = {AuthenticationRequestInterceptor.class})
public interface TicketClient {

    @GetMapping(value = "/count/purchased", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Integer> countTicketPurchased(@RequestParam("uid") String uid);
}
