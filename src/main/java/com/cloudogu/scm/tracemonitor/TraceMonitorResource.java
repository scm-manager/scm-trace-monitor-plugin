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
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Getter;
import org.apache.shiro.SecurityUtils;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.trace.SpanContext;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.cloudogu.scm.tracemonitor.TraceMonitorResource.TRACE_MONITOR_PATH;

@Path(TRACE_MONITOR_PATH)
public class TraceMonitorResource {

  static final String TRACE_MONITOR_PATH = "v2/trace-monitor/";
  static final String TRACE_MONITOR_MEDIA_TYPE = VndMediaType.PREFIX + "trace-monitor" + VndMediaType.SUFFIX;

  private final TraceStore store;
  private final SpanContextMapper mapper;
  private final Provider<ScmPathInfoStore> scmPathInfo;

  @Inject
  TraceMonitorResource(TraceStore store, SpanContextMapper mapper, Provider<ScmPathInfoStore> pathInfoStore) {
    this.store = store;
    this.mapper = mapper;
    this.scmPathInfo = pathInfoStore;
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
    Collection<SpanContextDto> dtos;

    if (!Strings.isNullOrEmpty(category)) {
      dtos = mapSpanContextCollectionToTraceMonitorResultDto(store.get(category));
    } else {
      dtos = mapSpanContextCollectionToTraceMonitorResultDto(store.getAll());
    }

    if (onlyFailed) {
      dtos = filterForFailedSpans(dtos);
    }
    final String selfLink = new LinkBuilder(scmPathInfo.get().get(), TraceMonitorResource.class).method("get").parameters().href();
    return new TraceMonitorResultDto(new Links.Builder().self(selfLink).build(), dtos);
  }

  @GET
  @Produces(TRACE_MONITOR_MEDIA_TYPE)
  @Path("available-categories")
  @Operation(
    summary = "Trace monitor categories",
    description = "Returns a list of the trace monitor categories.",
    tags = "Trace Monitor",
    operationId = "trace_monitor_get_categories"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = TRACE_MONITOR_MEDIA_TYPE,
      schema = @Schema(implementation = List.class)
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
  public AvailableCategoriesDto getAvailableCategories() {
    final String selfLink = new LinkBuilder(scmPathInfo.get().get(), TraceMonitorResource.class).method("getAvailableCategories").parameters().href();
    List<String> categories = store.getAll()
      .stream()
      .map(SpanContext::getKind)
      .distinct()
      .collect(Collectors.toList());
    return new AvailableCategoriesDto(new Links.Builder().self(selfLink).build(), categories);
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

  @Getter
  @SuppressWarnings("java:S2160") // wo do not need equals and hashcode for dto
  static class AvailableCategoriesDto extends HalRepresentation {
    private final List<String> categories;

    public AvailableCategoriesDto(Links links, List<String> categories) {
      super(links);
      this.categories = categories;
    }
  }
}
