package com.example.client.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder(toBuilder = true)
@Schema(description = "The find users request DTO")
public record FindUsersRequest(
    @Nullable @Schema(description = "Search string", example = "john") String search,
    @Nullable @Schema(description = "Enabled users filter", example = "true") Boolean enabled,
    @Nullable @Schema(description = "Deleted users filter", example = "true") Boolean deleted,
    @Nullable
        @Schema(
            description = "Sort order. Fields: id, email, firstName, lastName",
            example = "[\"email:asc\",  \"lastName:desc\"]")
        List<String> sort,
    @Nullable
        @Min(0)
        @Schema(
            description = "Offset of the first returned object",
            defaultValue = "0",
            example = "true")
        Long offset,
    @Nullable
        @Min(0)
        @Max(1000)
        @Schema(
            description = "Maximum number of objects to return",
            defaultValue = "20",
            example = "100")
        Integer limit)
    implements FindUsersFilter {}
