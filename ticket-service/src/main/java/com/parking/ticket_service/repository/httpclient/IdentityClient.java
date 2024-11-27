package com.parking.ticket_service.repository.httpclient;

import com.parking.ticket_service.dto.response.ApiResponse;
import com.parking.ticket_service.dto.response.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "identity-client", url = "http://localhost:8081/identity")
public interface IdentityClient {
    @GetMapping(value = "/users/internal/get-all-by-emails")
    ApiResponse<List<User>> getAllUser(@RequestParam(name = "emails") List<String> emails);
}
