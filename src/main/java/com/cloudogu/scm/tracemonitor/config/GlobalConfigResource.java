/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
