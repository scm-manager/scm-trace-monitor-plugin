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
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStoreExtension;
import sonia.scm.trace.SpanContext;

import java.time.Instant;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, QueryableStoreExtension.class})
@QueryableStoreExtension.QueryableTypes(SpanContextStoreWrapper.class)
class TraceStoreTest {
  @Mock
  private Subject subject;

  @Mock
  private GlobalConfigStore globalConfigStore;

  private TraceStore store;

  @BeforeEach
  void initStore(SpanContextStoreWrapperStoreFactory queryableStoreFactory) {
    ThreadContext.bind(subject);
    store = new TraceStore(queryableStoreFactory, globalConfigStore);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldNotGetSpansIfNotPermitted() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("traceMonitor:read");
    assertThrows(AuthorizationException.class, () -> store.getAll());
  }

  @Test
  void shouldNotGetSpansByKindsIfNotPermitted() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("traceMonitor:read");
    assertThrows(AuthorizationException.class, () -> store.get("Jenkins"));
  }

  @Test
  void shouldAddSpan() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(100, null));

    SpanContext spanContext = addSpanContextToStore("Jenkins", false);

    SpanContext storedSpan = store.get("Jenkins").iterator().next();
    assertThat(storedSpan).isEqualTo(spanContext);
  }

  @Test
  void shouldGetEmptyList() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(100, null));

    Collection<SpanContext> spanContexts = store.getAll();

    assertThat(spanContexts).isEmpty();
  }

  @Test
  void shouldGetAllSpans() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(100, null));

    addSpanContextToStore("Jenkins", false);
    addSpanContextToStore("Redmine", true);

    Collection<SpanContext> storedSpans = store.getAll();
    assertThat(storedSpans).hasSize(2);
    assertThat(storedSpans)
      .extracting("kind")
      .containsExactlyInAnyOrder("Jenkins", "Redmine");
  }

  @Test
  void shouldGetAllKinds() {
    addSpanContextToStore("Jenkins", false);
    addSpanContextToStore("Redmine", true);

    Collection<String> storedSpans = store.getKinds();
    assertThat(storedSpans)
      .hasSize(2)
      .containsExactlyInAnyOrder("Jenkins", "Redmine");
  }

  @Test
  void shouldGetSpansForKindOnly() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(100, null));

    addSpanContextToStore("Jenkins", false);
    addSpanContextToStore("Jenkins", true);
    addSpanContextToStore("Redmine", false);
    addSpanContextToStore("Redmine", true);

    Collection<SpanContext> spans = store.get("Jenkins");
    assertThat(spans).hasSize(2);
    assertThat(spans.stream().allMatch(s -> s.getKind().equalsIgnoreCase("Jenkins"))).isTrue();
  }

  @Test
  void shouldOnlyReturnStoreLimitedAmountOfSpans() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(100, null));

    for (int i = 0; i < 1000; i++) {
      addSpanContextToStore("Jenkins", i, false);
    }

    Collection<SpanContext> spans = store.get("Jenkins");

    assertThat(spans).hasSize(100);
    // Should store the latest 100 entries
    assertThat(spans.stream().allMatch(s -> s.getOpened().isAfter(Instant.ofEpochMilli(899)))).isTrue();
  }

  @Test
  void shouldStoreMaxAmountPerKind() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(100, null));

    for (int i = 0; i < 100; i++) {
      addSpanContextToStore("Jenkins", i, false);
    }

    for (int i = 0; i < 100; i++) {
      addSpanContextToStore("Redmine", i, false);
    }

    Collection<SpanContext> spans = store.getAll();
    assertThat(spans).hasSize(100);
  }

  @Test
  void shouldHeedLimitAfterStoreSizeChanged() {
    when(globalConfigStore.get()).thenReturn(new GlobalConfig(100, null));

    for (int i = 0; i < 100; i++) {
      addSpanContextToStore("Jenkins", i, false);
    }

    for (int i = 0; i < 100; i++) {
      addSpanContextToStore("Redmine", i, false);
    }

    when(globalConfigStore.get()).thenReturn(new GlobalConfig(20, null));

    addSpanContextToStore("Jenkins", 300, false);

    Collection<SpanContext> jenkinsSpans = store.get("Jenkins");
    Collection<SpanContext> redmineSpans = store.get("Redmine");

    assertThat(jenkinsSpans).hasSize(20);
    assertThat(redmineSpans).hasSize(20);

    assertThat(jenkinsSpans.stream().allMatch(s -> s.getOpened().isAfter(Instant.ofEpochMilli(80)))).isTrue();
  }

  @Test
  void shouldCleanUpStore(SpanContextStoreWrapperStoreFactory storeFactory) {
    for (int i = 0; i < 10; i++) {
      addSpanContextToStore("Jenkins", i, false);
    }

    when(globalConfigStore.get()).thenReturn(new GlobalConfig(5, null));

    store.cleanUp();

    try (QueryableMutableStore<SpanContextStoreWrapper> queryableMutableStore = storeFactory.getMutable("Jenkins")) {
      assertThat(queryableMutableStore.query().count()).isEqualTo(5);
    }
  }

  private SpanContext addSpanContextToStore(String kind, long opened, boolean failed) {
    SpanContext spanContext = new SpanContext(kind, ImmutableMap.of("url", "hitchhiker.org/scm"), Instant.ofEpochMilli(opened), Instant.ofEpochMilli(200L), failed);
    store.add(spanContext);
    return spanContext;
  }

  private SpanContext addSpanContextToStore(String kind, boolean failed) {
    return addSpanContextToStore(kind, 0, failed);
  }
}
