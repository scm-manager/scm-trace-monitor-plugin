package com.cloudogu.scm.tracemonitor;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;
import sonia.scm.trace.SpanContext;

import java.time.Instant;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TraceStoreTest {


  private TraceStore store;

  @BeforeEach
  void initStore() {
    InMemoryDataStoreFactory factory = new InMemoryDataStoreFactory(new InMemoryDataStore<>());
    store = new TraceStore(factory);
  }

  @Test
  void shouldAddSpan() {
    SpanContext spanContext = addSpanContextToStore("Jenkins", false);

    SpanContext storedSpan = store.get("Jenkins").iterator().next();
    assertThat(storedSpan).isEqualTo(spanContext);
  }

  @Test
  void shouldGetEmptyList() {
    Collection<SpanContext> spanContexts = store.getAll();
    assertThat(spanContexts).isEmpty();
  }

  @Test
  void shouldGetAllSpans() {
    addSpanContextToStore("Jenkins", false);
    addSpanContextToStore("Redmine", true);

    Collection<SpanContext> storedSpans = store.getAll();
    assertThat(storedSpans).hasSize(2);
    assertThat(storedSpans.stream().anyMatch(s -> s.getKind().equalsIgnoreCase("Jenkins"))).isTrue();
    assertThat(storedSpans.stream().anyMatch(s -> s.getKind().equalsIgnoreCase("Redmine"))).isTrue();
  }

  @Test
  void shouldGetSpansForKindOnly() {
    addSpanContextToStore("Jenkins", false);
    addSpanContextToStore("Jenkins", true);
    addSpanContextToStore("Redmine", false);
    addSpanContextToStore("Redmine", true);

    Collection<SpanContext> spans = store.get("Jenkins");
    assertThat(spans).hasSize(2);
    assertThat(spans.stream().allMatch(s -> s.getKind().equalsIgnoreCase("Jenkins"))).isTrue();
  }

  @Test
  void shouldOnlyStoreLimitedAmountOfSpans() {
    for (int i = 0; i < 1000; i++) {
      addSpanContextToStore("Jenkins", i, false);
    }

    Collection<SpanContext> spans = store.get("Jenkins");

    //Assuming the evicting queue size is 100
    assertThat(spans).hasSize(100);
    // Should store the latest 100 entries
    assertThat(spans.stream().allMatch(s -> s.getOpened().isAfter(Instant.ofEpochMilli(899)))).isTrue();
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
