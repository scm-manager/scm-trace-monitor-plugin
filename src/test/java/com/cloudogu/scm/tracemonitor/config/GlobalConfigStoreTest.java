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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class GlobalConfigStoreTest {

  @Mock
  private Subject subject;
  private GlobalConfigStore store;

  @BeforeEach
  void initStore() {
    ThreadContext.bind(subject);
    InMemoryConfigurationStoreFactory factory = new InMemoryConfigurationStoreFactory();
    store = new GlobalConfigStore(factory);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldGetConfig() {
    GlobalConfig globalConfig = store.get();
    assertThat(globalConfig.getStoreSize()).isEqualTo(100);
  }

  @Test
  void shouldUpdateConfig() {
    store.update(new GlobalConfig(1337));

    GlobalConfig globalConfig = store.get();
    assertThat(globalConfig.getStoreSize()).isEqualTo(1337);
  }

  @Test
  void shouldNotUpdateConfigWithoutPermission() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:write:traceMonitor");

    GlobalConfig config = new GlobalConfig(1337);
    assertThrows(AuthorizationException.class, () -> store.update(config));
  }
}
