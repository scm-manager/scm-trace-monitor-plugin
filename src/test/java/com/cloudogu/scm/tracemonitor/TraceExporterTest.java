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

import com.cloudogu.scm.tracemonitor.config.GlobalConfig;
import com.cloudogu.scm.tracemonitor.config.GlobalConfigStore;
import com.google.common.collect.ImmutableMap;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.trace.SpanContext;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceExporterTest {

  @Mock
  private Subject subject;
  @Mock
  private GlobalConfigStore globalConfigStore;
  @Mock
  private ScmEventBus eventBus;
  private TraceStore store;
  private TraceExporter traceExporter;

  @BeforeEach
  void initStore() {
    ThreadContext.bind(subject);
    InMemoryConfigurationEntryStoreFactory factory = new InMemoryConfigurationEntryStoreFactory();
    store = new TraceStore(factory, globalConfigStore);
    traceExporter = new TraceExporter(store, eventBus);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldAddSpanToStore() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(42));

    traceExporter.export(createSpanContext(false));

    assertThat(store.getAll()).isNotEmpty();
    SpanContext storedSpanContext = store.getAll().iterator().next();
    assertThat(storedSpanContext.getKind()).isEqualTo("Jenkins");
    assertThat(storedSpanContext.getClosed()).isEqualTo(Instant.ofEpochMilli(200L));
    assertThat(storedSpanContext.getOpened()).isEqualTo(Instant.ofEpochMilli(0L));
    assertThat(storedSpanContext.isFailed()).isFalse();
    assertThat(storedSpanContext.getLabels().values().iterator().next()).isEqualTo("hitchhiker.org/scm");
  }

  private SpanContext createSpanContext(boolean failed) {
    return SpanContext.create("Jenkins", ImmutableMap.of("url", "hitchhiker.org/scm"), Instant.ofEpochMilli(0L), Instant.ofEpochMilli(200L), failed);
  }

  @Test
  void shouldFireRequestFailedEvent() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(42));

    traceExporter.export(createSpanContext(true));

    verify(eventBus).post(any(RequestFailedEvent.class));
  }

  @Test
  void shouldNotFireRequestFailedEvent() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(42));

    traceExporter.export(createSpanContext(false));

    verify(eventBus, never()).post(any(RequestFailedEvent.class));
  }

}
