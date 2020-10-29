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
  void shouldNotGetConfigWithoutPermission() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:read:traceMonitor");
    assertThrows(AuthorizationException.class, () -> store.get());
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

    assertThrows(AuthorizationException.class, () -> store.update(new GlobalConfig(1337)));
  }
}
