package com.example.client.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder(toBuilder = true)
@Schema(description = "The count users request DTO")
public record CountUsersRequest(
    @Nullable @Schema(description = "Search string", example = "john") String search,
    @Nullable @Schema(description = "Enabled users filter", example = "true") Boolean enabled,
    @Nullable @Schema(description = "Deleted users filter", example = "true") Boolean deleted)
    implements FindUsersFilter {}
