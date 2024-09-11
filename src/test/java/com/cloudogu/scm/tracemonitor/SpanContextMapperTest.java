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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import sonia.scm.trace.SpanContext;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SpanContextMapperTest {

  private SpanContextMapper mapper = new SpanContextMapperImpl();

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
