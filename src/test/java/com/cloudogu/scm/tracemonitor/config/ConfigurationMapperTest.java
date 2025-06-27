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
    GlobalConfigDto dto = mapper.map(new GlobalConfig(1000, null));

    assertThat(dto.getStoreSize()).isEqualTo(1000);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/v2/config/trace-monitor/");
    assertThat(dto.getLinks().getLinkBy("update")).isNotPresent();
  }

  @Test
  void shouldMapToDtoWithUpdateLink() {
    when(subject.isPermitted("configuration:write:traceMonitor")).thenReturn(true);
    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/scm"));
    GlobalConfigDto dto = mapper.map(new GlobalConfig(1000, null));

    assertThat(dto.getStoreSize()).isEqualTo(1000);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/v2/config/trace-monitor/");
    assertThat(dto.getLinks().getLinkBy("update").get().getHref()).isEqualTo("/v2/config/trace-monitor/");
  }

}
