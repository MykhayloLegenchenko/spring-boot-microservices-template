package com.example.client.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder(toBuilder = true)
@Schema(description = "Count users request payload")
public record CountUsersRequest(
    @Nullable @Schema(description = "Search string", example = "john") String search,
    @Nullable @Schema(description = "Enabled users filter", example = "true") Boolean enabled,
    @Nullable @Schema(description = "Deleted users filter", example = "true") Boolean deleted)
    implements FindUsersFilter {}
