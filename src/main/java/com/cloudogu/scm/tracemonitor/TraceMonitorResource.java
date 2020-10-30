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

package com.cloudogu.scm.tracemonitor;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.shiro.SecurityUtils;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.trace.SpanContext;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Path("v2/trace-monitor/")
public class TraceMonitorResource {

  static final String TRACE_MONITOR_MEDIA_TYPE = VndMediaType.PREFIX + "trace-monitor" + VndMediaType.SUFFIX;

  private final TraceStore store;
  private final SpanContextMapper mapper;

  @Inject
  TraceMonitorResource(TraceStore store, SpanContextMapper mapper) {
    this.store = store;
    this.mapper = mapper;
  }

  @GET
  @Produces(TRACE_MONITOR_MEDIA_TYPE)
  @Path("")
  @Operation(
    summary = "Trace monitor result",
    description = "Returns the trace monitor result.",
    tags = "Trace Monitor",
    operationId = "trace_monitor_get_result"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = TRACE_MONITOR_MEDIA_TYPE,
      schema = @Schema(implementation = TraceMonitorResultDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"traceMonitor:read\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public TraceMonitorResultDto get(
    @DefaultValue("") @QueryParam("category") String category,
    @QueryParam("onlyFailed") boolean onlyFailed
  ) {
    //TODO Remove -- just for testing
    SpanContext spanContext = SpanContext.create("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.now(), Instant.now().plusMillis(200L), true);
    SpanContext spanContext1 = SpanContext.create("Redmine", ImmutableMap.of("url", "hitchhiker.org/redmine"), Instant.now(), Instant.now().plusMillis(400L), false);
    store.add(spanContext);
    store.add(spanContext1);
    //TODO Remove -- just for testing

    SecurityUtils.getSubject().checkPermission("traceMonitor:read");
    Collection<SpanContextDto> dtos;

    if (!Strings.isNullOrEmpty(category)) {
      dtos = mapSpanContextCollectionToTraceMonitorResultDto(store.get(category));
    } else {
      dtos = mapSpanContextCollectionToTraceMonitorResultDto(store.getAll());
    }

    if (onlyFailed) {
      dtos = filterForFailedSpans(dtos);
    }

    return new TraceMonitorResultDto(dtos);
  }

  private List<SpanContextDto> filterForFailedSpans(Collection<SpanContextDto> spanContextDtos) {
    return spanContextDtos.stream()
      .filter(SpanContextDto::isFailed)
      .collect(Collectors.toList());
  }

  private List<SpanContextDto> mapSpanContextCollectionToTraceMonitorResultDto(Collection<SpanContext> spans) {
    return spans.stream()
      .map(mapper::map)
      .collect(Collectors.toList());
  }
}
