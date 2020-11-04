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
