package com.parking.profile_service.repository.http_client;

import com.parking.profile_service.configuration.AuthenticationRequestInterceptor;
import com.parking.profile_service.dto.response.ApiResponse;
import com.parking.profile_service.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "Identity", url = "http://localhost:8081/identity", configuration = {AuthenticationRequestInterceptor.class})
public interface IdentityClient {
    @GetMapping(value = "/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserResponse> getUser(@PathVariable(name = "id") String id);
}
