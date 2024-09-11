/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.tracemonitor.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static com.cloudogu.scm.tracemonitor.config.GlobalConfigResource.TRACE_MONITOR_CONFIG_PATH_V2;

@OpenAPIDefinition(tags = {
  @Tag(name = "Trace Monitor Plugin", description = "Trace Monitor plugin provided endpoints")
})
@Path(TRACE_MONITOR_CONFIG_PATH_V2)
public class GlobalConfigResource {

  static final String TRACE_MONITOR_CONFIG_PATH_V2 = "v2/config/trace-monitor";
  static final String MEDIA_TYPE = VndMediaType.PREFIX + "trace-monitor-config" + VndMediaType.SUFFIX;

  private final ConfigurationMapper mapper;
  private final GlobalConfigStore store;

  @Inject
  public GlobalConfigResource(ConfigurationMapper mapper, GlobalConfigStore store) {
    this.mapper = mapper;
    this.store = store;
  }

  @GET
  @Path("/")
  @Produces(MEDIA_TYPE)
  @Operation(
    summary = "Get global trace monitor configuration",
    description = "Returns the global trace monitor configuration.",
    tags = "Trace Monitor Plugin",
    operationId = "trace_monitor_get_global_config"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = GlobalConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the configuration")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response get() {
    ConfigurationPermissions.read("traceMonitor").check();
    return Response.ok(mapper.map(store.get())).build();
  }

  @PUT
  @Path("/")
  @Consumes(MEDIA_TYPE)
  @Operation(
    summary = "Update global trace monitor configuration",
    description = "Modifies the global trace monitor configuration.",
    tags = "Trace Monitor Plugin",
    operationId = "trace_monitor_put_global_config"
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege to change the configuration")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response update(@Valid GlobalConfigDto updatedConfig) {
    store.update(mapper.map(updatedConfig));
    return Response.noContent().build();
  }
}
