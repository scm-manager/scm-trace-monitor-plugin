package com.cloudogu.scm.tracemonitor;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import sonia.scm.trace.SpanContext;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SpanContextMapperTest {

  SpanContextMapper mapper = new SpanContextMapperImpl();

  @Test
  void shouldMapToDto() {
    SpanContext spanContext = new SpanContext(
      "Hitchhiker",
      ImmutableMap.of("url", "hitchhiker.org/scm"),
      Instant.now(),
      Instant.now().plusMillis(100L),
      true);
    SpanContextDto dto = mapper.map(spanContext);

    assertThat(dto.getKind()).isEqualTo(spanContext.getKind());
    assertThat(dto.getClosed()).isEqualTo(spanContext.getClosed());
    assertThat(dto.getOpened()).isEqualTo(spanContext.getOpened());
    assertThat(dto.getLabels()).containsEntry("url", spanContext.getLabels().get("url"));
    assertThat(dto.getDurationInMillis()).isEqualTo(100L);
  }
}
