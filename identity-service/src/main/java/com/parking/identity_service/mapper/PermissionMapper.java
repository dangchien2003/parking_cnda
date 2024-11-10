package com.parking.identity_service.mapper;

import com.parking.identity_service.dto.request.PermissionCreationRequest;
import com.parking.identity_service.dto.response.PermissionResponse;
import com.parking.identity_service.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionCreationRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
