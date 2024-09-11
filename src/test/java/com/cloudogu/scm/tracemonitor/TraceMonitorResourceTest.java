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

package com.cloudogu.scm.tracemonitor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.trace.SpanContext;
import sonia.scm.web.RestDispatcher;

import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceMonitorResourceTest {

  @Mock
  Provider<ScmPathInfoStore> pathInfoStoreProvider;

  @Mock
  ScmPathInfoStore scmPathInfoStore;

  @Mock
  private TraceStore store;

  @Mock
  private SpanContextMapper mapper;

  @InjectMocks
  private TraceMonitorResource resource;

  private RestDispatcher dispatcher;

  @BeforeEach
  void initResource() {
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
    lenient().when(pathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);
    lenient().when(scmPathInfoStore.get()).thenReturn(() -> URI.create("hitchhiker.org/scm/"));
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldGetAllSpans() throws UnsupportedEncodingException, URISyntaxException {
    mockSpans(Optional.empty());

    MockHttpRequest request = MockHttpRequest.get("/" + TraceMonitorResource.TRACE_MONITOR_PATH);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"kind\":\"Jenkins\"");
    assertThat(response.getContentAsString()).contains("\"kind\":\"Redmine\"");
  }

  @Test
  void shouldOnlyGetFailedSpans() throws UnsupportedEncodingException, URISyntaxException {
    mockSpans(Optional.empty());

    MockHttpRequest request = MockHttpRequest.get("/" + TraceMonitorResource.TRACE_MONITOR_PATH + "?onlyFailed=true");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"kind\":\"Jenkins\"");
    assertThat(response.getContentAsString()).doesNotContain("\"kind\":\"Redmine\"");
  }

  @Test
  void shouldOnlyGetFilteredSpans() throws UnsupportedEncodingException, URISyntaxException {
    mockSpans(Optional.empty());

    MockHttpRequest request = MockHttpRequest.get("/" + TraceMonitorResource.TRACE_MONITOR_PATH + "?labelFilter=redmi");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).doesNotContain("\"kind\":\"Jenkins\"");
    assertThat(response.getContentAsString()).contains("\"kind\":\"Redmine\"");
  }

  @Test
  void shouldOnlyGetSpansForCategory() throws UnsupportedEncodingException, URISyntaxException {
    mockSpans(Optional.of("Redmine"));

    MockHttpRequest request = MockHttpRequest.get("/" + TraceMonitorResource.TRACE_MONITOR_PATH + "?category=Redmine");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).doesNotContain("\"kind\":\"Jenkins\"");
    assertThat(response.getContentAsString()).contains("\"kind\":\"Redmine\"");
  }

  @Test
  void shouldGetEmptyCollection() throws UnsupportedEncodingException, URISyntaxException {
    mockSpans(Optional.of("Redmine"));

    MockHttpRequest request = MockHttpRequest.get("/" + TraceMonitorResource.TRACE_MONITOR_PATH + "?category=Redmine&onlyFailed=true");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"spans\":[]");
    assertThat(response.getContentAsString()).contains("\"page\":0");
    assertThat(response.getContentAsString()).contains("\"pageSize\":100");
    assertThat(response.getContentAsString()).contains("\"pageTotal\":0");
    assertThat(response.getContentAsString()).contains("\"_links\":{\"self\":{\"href\":\"hitchhiker.org/scm/v2/trace-monitor/?page=1\"},\"first\":{\"href\":\"hitchhiker.org/scm/v2/trace-monitor/?page=1\"},\"last\":{\"href\":\"hitchhiker.org/scm/v2/trace-monitor/?page=1\"}}");
  }

  @Test
  void shouldGetAvailableCategories() throws UnsupportedEncodingException, URISyntaxException {
    mockSpans(Optional.empty());
    MockHttpRequest request = MockHttpRequest.get("/" + TraceMonitorResource.TRACE_MONITOR_PATH + "available-categories");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("{\"categories\":[\"Jenkins\",\"Redmine\"],\"_links\":{\"self\":{\"href\":\"hitchhiker.org/scm/v2/trace-monitor/available-categories\"}}}");
  }

  @Test
  void shouldGetSortedAndLimitedSpans() throws URISyntaxException, UnsupportedEncodingException {
    List<SpanContext> contexts = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      SpanContext span = new SpanContext("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.ofEpochMilli(i), Instant.ofEpochMilli(i).plusMillis(i), true);
      contexts.add(span);
      lenient().when(mapper.map(span)).thenReturn(new SpanContextDto("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.ofEpochMilli(i), Instant.ofEpochMilli(i).plusMillis(i), i, true));
    }
    when(store.getAll()).thenReturn(contexts);

    MockHttpRequest request = MockHttpRequest.get("/" + TraceMonitorResource.TRACE_MONITOR_PATH + "?limit=10");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"kind\":\"Jenkins\"");
    assertThat(response.getContentAsString()).contains("\"durationInMillis\":99");
    assertThat(response.getContentAsString()).contains("\"durationInMillis\":90");
    assertThat(response.getContentAsString()).doesNotContain("\"durationInMillis\":89");
  }

  @Test
  void shouldGetSortedFailedLimitedSpans() throws URISyntaxException, UnsupportedEncodingException {
    List<SpanContext> contexts = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      SpanContext span = new SpanContext("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.ofEpochMilli(i), Instant.ofEpochMilli(i).plusMillis(i), i > 50);
      contexts.add(span);
      lenient().when(mapper.map(span)).thenReturn(new SpanContextDto("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.ofEpochMilli(i), Instant.ofEpochMilli(i).plusMillis(i), i, i > 50));
    }
    when(store.getAll()).thenReturn(contexts);

    MockHttpRequest request = MockHttpRequest.get("/" + TraceMonitorResource.TRACE_MONITOR_PATH + "?limit=10&onlyFailed=true");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"kind\":\"Jenkins\"");
    assertThat(response.getContentAsString()).contains("\"durationInMillis\":99");
    assertThat(response.getContentAsString()).contains("\"durationInMillis\":90");
    assertThat(response.getContentAsString()).contains("\"failed\":true");
    assertThat(response.getContentAsString()).doesNotContain("\"durationInMillis\":89");
    assertThat(response.getContentAsString()).doesNotContain("\"failed\":false");
  }

  @Test
  void shouldGetSpansOnSpecificPage() throws UnsupportedEncodingException, URISyntaxException {
    List<SpanContext> contexts = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      SpanContext span = new SpanContext("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.ofEpochMilli(i), Instant.ofEpochMilli(i).plusMillis(i), true);
      contexts.add(span);
      lenient().when(mapper.map(span)).thenReturn(new SpanContextDto("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.ofEpochMilli(i), Instant.ofEpochMilli(i).plusMillis(i), i, true));
    }
    when(store.getAll()).thenReturn(contexts);

    MockHttpRequest request = MockHttpRequest.get("/" + TraceMonitorResource.TRACE_MONITOR_PATH + "?limit=10&page=3");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"kind\":\"Jenkins\"");
    assertThat(response.getContentAsString()).contains("\"durationInMillis\":79");
    assertThat(response.getContentAsString()).contains("\"durationInMillis\":70");
    assertThat(response.getContentAsString()).doesNotContain("\"durationInMillis\":80");
    assertThat(response.getContentAsString()).doesNotContain("\"durationInMillis\":69");
  }


  private void mockSpans(Optional<String> category) {
    SpanContext span1 = new SpanContext("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.now(), Instant.now().plusMillis(200L), true);
    SpanContext span2 = new SpanContext("Redmine", ImmutableMap.of("url", "hitchhiker.org/redmine"), Instant.now(), Instant.now().plusMillis(400L), false);
    lenient().when(store.getAll()).thenReturn(ImmutableList.of(span1, span2));
    lenient().when(mapper.map(span1)).thenReturn(new SpanContextDto("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.now(), Instant.now().plusMillis(200L), 200, true));
    lenient().when(mapper.map(span2)).thenReturn(new SpanContextDto("Redmine", ImmutableMap.of("url", "hitchhiker.org/redmine"), Instant.now(), Instant.now().plusMillis(400L), 400, false));
    category.ifPresent(value -> lenient().when(store.get(value)).thenReturn(ImmutableList.of(span1, span2).stream().filter(s -> s.getKind().equalsIgnoreCase(value)).collect(Collectors.toList())));
  }
}
