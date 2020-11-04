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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
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
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static com.cloudogu.scm.tracemonitor.config.GlobalConfigResource.MEDIA_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalConfigResourceTest {

  @Mock
  private Subject subject;

  @Mock
  private GlobalConfigStore store;

  @Mock
  private ConfigurationMapper mapper;

  @InjectMocks
  private GlobalConfigResource resource;

  private RestDispatcher dispatcher;

  @BeforeEach
  void initResource() {
    ThreadContext.bind(subject);
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldReturnForbiddenIfNotPermitted() throws URISyntaxException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:read:traceMonitor");
    MockHttpRequest request = MockHttpRequest.get("/" + GlobalConfigResource.TRACE_MONITOR_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  void shouldGetConfig() throws URISyntaxException, UnsupportedEncodingException {
    GlobalConfig globalConfig = new GlobalConfig(42);
    when(store.get()).thenReturn(globalConfig);
    when(mapper.map(globalConfig)).thenReturn(new GlobalConfigDto(42));

    MockHttpRequest request = MockHttpRequest.get("/" + GlobalConfigResource.TRACE_MONITOR_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"storeSize\":42");
  }

  @Test
  void shouldUpdateConfig() throws URISyntaxException {
    GlobalConfig globalConfig = new GlobalConfig(42);
    when(mapper.map(new GlobalConfigDto(42))).thenReturn(globalConfig);
    MockHttpRequest request = MockHttpRequest.put("/" + GlobalConfigResource.TRACE_MONITOR_CONFIG_PATH_V2)
      .contentType(MEDIA_TYPE)
      .content("{\"storeSize\":42}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);

    verify(mapper).map(any(GlobalConfigDto.class));
    verify(store).update(globalConfig);
  }

  @Test
  void shouldNotUpdateConfigOnInvalidStoreSize() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/" + GlobalConfigResource.TRACE_MONITOR_CONFIG_PATH_V2)
      .contentType(MEDIA_TYPE)
      .content("{\"storeSize\":-1}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
  }
}
