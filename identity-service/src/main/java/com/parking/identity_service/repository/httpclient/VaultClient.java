package com.parking.identity_service.repository.httpclient;

import com.parking.identity_service.configuration.AuthenticationRequestInterceptor;
import com.parking.identity_service.dto.request.OwnerCreationRequest;
import com.parking.identity_service.dto.response.ApiResponse;
import com.parking.identity_service.dto.response.BalanceResponse;
import com.parking.identity_service.dto.response.OwnerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "vault-service", url = "${feign.client.config.vault-service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface VaultClient {
    @PostMapping(value = "/internal/owner/registration", produces = MediaType.APPLICATION_JSON_VALUE)
    OwnerResponse createOwner(@RequestBody OwnerCreationRequest request);

    @GetMapping(value = "/owners/internal/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<BalanceResponse> getBalance(@RequestParam("uid") String uid);


    @GetMapping(value = "/fluctuation/use-in-month", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Integer> useinmonth(@RequestParam("uid") String uid);
}
