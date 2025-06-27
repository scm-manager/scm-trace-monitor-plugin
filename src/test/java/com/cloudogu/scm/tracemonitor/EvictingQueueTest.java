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

import org.junit.jupiter.api.Test;

import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

class EvictingQueueTest {

  @Test
  void shouldReturnFalseIfMaxSizeIsZero() {
    Queue<String> queue = EvictingQueue.create(0);
    assertThat(queue.add("a")).isFalse();
  }

  @Test
  void shouldEvictFirstAddedEntry() {
    Queue<String> queue = EvictingQueue.create(2);
    assertThat(queue.add("a")).isTrue();
    assertThat(queue.add("b")).isTrue();
    assertThat(queue.add("c")).isTrue();
    assertThat(queue).containsOnly("b", "c");
  }

  @Test
  void shouldLimitEntriesByQueueSize() {
    EvictingQueue<String> queue = EvictingQueue.create(100);
    for (int i = 0; i < 120; i++) {
      assertThat(queue.add("a" + i)).isTrue();
    }
    assertThat(queue).hasSize(100);
  }

  @Test
  void shouldAllowMoreThan100Entries() {
    EvictingQueue<String> queue = new EvictingQueue<>();
    for (int i = 0; i < 120; i++) {
      assertThat(queue.add("a" + i)).isTrue();
    }
    assertThat(queue).hasSize(120);
  }

}
