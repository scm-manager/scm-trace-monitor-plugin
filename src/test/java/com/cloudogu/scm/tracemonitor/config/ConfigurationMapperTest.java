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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationMapperTest {

  @Mock
  private Subject subject;

  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  private ConfigurationMapperImpl mapper;

  @BeforeEach
  void initSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapToDto() {
    when(subject.isPermitted("configuration:write:traceMonitor")).thenReturn(false);
    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/scm"));
    GlobalConfigDto dto = mapper.map(new GlobalConfig(1000));

    assertThat(dto.getStoreSize()).isEqualTo(1000);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/v2/config/trace-monitor/");
    assertThat(dto.getLinks().getLinkBy("update")).isNotPresent();
  }

  @Test
  void shouldMapToDtoWithUpdateLink() {
    when(subject.isPermitted("configuration:write:traceMonitor")).thenReturn(true);
    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/scm"));
    GlobalConfigDto dto = mapper.map(new GlobalConfig(1000));

    assertThat(dto.getStoreSize()).isEqualTo(1000);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/v2/config/trace-monitor/");
    assertThat(dto.getLinks().getLinkBy("update").get().getHref()).isEqualTo("/v2/config/trace-monitor/");
  }

}
