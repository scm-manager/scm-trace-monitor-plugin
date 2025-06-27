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

import com.cloudogu.scm.tracemonitor.Cleanup;
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

import jakarta.servlet.http.HttpServletResponse;
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

  @Mock
  private Cleanup cleanup;

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
    GlobalConfig globalConfig = new GlobalConfig(42, null);
    when(store.get()).thenReturn(globalConfig);
    when(mapper.map(globalConfig)).thenReturn(new GlobalConfigDto(42, "0 0 2 * * ?"));

    MockHttpRequest request = MockHttpRequest.get("/" + GlobalConfigResource.TRACE_MONITOR_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"storeSize\":42");
  }

  @Test
  void shouldUpdateConfig() throws URISyntaxException {
    GlobalConfig globalConfig = new GlobalConfig(42, "0 0 2 * * ?");
    when(mapper.map(new GlobalConfigDto(42, "0 0 2 * * ?"))).thenReturn(globalConfig);
    MockHttpRequest request = MockHttpRequest.put("/" + GlobalConfigResource.TRACE_MONITOR_CONFIG_PATH_V2)
      .contentType(MEDIA_TYPE)
      .content("{\"storeSize\":42}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);

    verify(mapper).map(any(GlobalConfigDto.class));
    verify(store).update(globalConfig);
    verify(cleanup).reSchedule();
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
