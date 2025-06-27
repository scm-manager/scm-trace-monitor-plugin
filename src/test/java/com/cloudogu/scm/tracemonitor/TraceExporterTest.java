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
import sonia.scm.store.QueryableStoreExtension;
import sonia.scm.trace.SpanContext;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, QueryableStoreExtension.class})
@QueryableStoreExtension.QueryableTypes(SpanContextStoreWrapper.class)
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
  void initStore(SpanContextStoreWrapperStoreFactory queryableStoreFactory) {
    ThreadContext.bind(subject);
    store = new TraceStore(queryableStoreFactory, globalConfigStore);
    traceExporter = new TraceExporter(store, eventBus);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldAddSpanToStore() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(42, null));

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
    return new SpanContext("Jenkins", ImmutableMap.of("url", "hitchhiker.org/scm"), Instant.ofEpochMilli(0L), Instant.ofEpochMilli(200L), failed);
  }

  @Test
  void shouldFireRequestFailedEvent() {
    traceExporter.export(createSpanContext(true));

    verify(eventBus).post(any(RequestFailedEvent.class));
  }

  @Test
  void shouldNotFireRequestFailedEvent() {
    traceExporter.export(createSpanContext(false));

    verify(eventBus, never()).post(any(RequestFailedEvent.class));
  }

}
