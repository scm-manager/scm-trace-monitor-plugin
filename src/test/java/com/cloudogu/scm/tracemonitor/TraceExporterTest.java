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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TraceExporterTest {

  private final InMemoryDataStore<SpanContext> dataStore = new InMemoryDataStore<>();

  private TraceExporter traceExporter;

  @BeforeEach
  void initStore() {
    InMemoryDataStoreFactory factory = new InMemoryDataStoreFactory(dataStore);
    TraceStore store = new TraceStore(factory);
    traceExporter = new TraceExporter(store);
  }

  @Test
  void shouldAddSpanToStore() {
    traceExporter.export(new SpanContext("Jenkins", ImmutableMap.of("url", "hitchhiker.org/scm"), Instant.ofEpochMilli(0L), Instant.ofEpochMilli(200L), false));

    assertThat(dataStore.getAll()).isNotEmpty();
    SpanContext storedSpanContext = dataStore.getAll().values().iterator().next();
    assertThat(storedSpanContext.getKind()).isEqualTo("Jenkins");
    assertThat(storedSpanContext.getClosed()).isEqualTo(Instant.ofEpochMilli(200L));
    assertThat(storedSpanContext.getOpened()).isEqualTo(Instant.ofEpochMilli(0L));
    assertThat(storedSpanContext.isFailed()).isFalse();
    assertThat(storedSpanContext.getLabels().values().iterator().next()).isEqualTo("hitchhiker.org/scm");
  }

}
