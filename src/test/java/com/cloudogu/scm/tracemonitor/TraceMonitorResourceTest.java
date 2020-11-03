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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.trace.SpanContext;
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

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
    assertThat(response.getContentAsString()).contains("{\"spans\":[],\"_links\":{\"self\":{\"href\":\"hitchhiker.org/scm/v2/trace-monitor/\"}}}");
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


  private void mockSpans(Optional<String> category) {
    SpanContext span1 = SpanContext.create("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.now(), Instant.now().plusMillis(200L), true);
    SpanContext span2 = SpanContext.create("Redmine", ImmutableMap.of("url", "hitchhiker.org/redmine"), Instant.now(), Instant.now().plusMillis(400L), false);
    lenient().when(store.getAll()).thenReturn(ImmutableList.of(span1, span2));
    lenient().when(mapper.map(span1)).thenReturn(new SpanContextDto("Jenkins", ImmutableMap.of("url", "hitchhiker.org/jenkins"), Instant.now(), Instant.now().plusMillis(200L), 200, true));
    lenient().when(mapper.map(span2)).thenReturn(new SpanContextDto("Redmine", ImmutableMap.of("url", "hitchhiker.org/redmine"), Instant.now(), Instant.now().plusMillis(400L), 400, false));
    category.ifPresent(value -> lenient().when(store.get(value)).thenReturn(ImmutableList.of(span1, span2).stream().filter(s -> s.getKind().equalsIgnoreCase(value)).collect(Collectors.toList())));
  }
}
