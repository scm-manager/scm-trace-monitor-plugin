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

package com.cloudogu.scm.tracemonitor.update;

import com.cloudogu.scm.tracemonitor.SpanContextStoreWrapper;
import com.cloudogu.scm.tracemonitor.SpanContextStoreWrapperStoreFactory;
import com.cloudogu.scm.tracemonitor.update.TraceMonitorQueryableStoreUpdateStep.LegacyStoreEntry;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.store.InMemoryByteDataStore;
import sonia.scm.store.InMemoryByteDataStoreFactory;
import sonia.scm.store.QueryableStoreExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(QueryableStoreExtension.class)
@QueryableStoreExtension.QueryableTypes(SpanContextStoreWrapper.class)
class TraceMonitorQueryableStoreUpdateStepTest {

  private final InMemoryByteDataStoreFactory dataStoreFactory = new InMemoryByteDataStoreFactory();

  @Test
  void shouldUpdateLegacyStoreEntry(SpanContextStoreWrapperStoreFactory queryableStoreFactory) throws IOException {
    loadTestData("Plugin Center");
    loadTestData("Release Feed");

    new TraceMonitorQueryableStoreUpdateStep(
      queryableStoreFactory,
      dataStoreFactory
    ).doUpdate();

    try (var dataStore = queryableStoreFactory.getMutable("Plugin Center")) {
      assertThat(dataStore.getAll()).hasSize(4);
    }
    try (var dataStore = queryableStoreFactory.getMutable("Release Feed")) {
      assertThat(dataStore.getAll()).hasSize(2);
    }
  }

  private void loadTestData(String file) throws IOException {
    String xml = String.join("\n",
      Resources.readLines(
        Resources.getResource("com/cloudogu/scm/tracemonitor/update/" + file + ".xml"),
        StandardCharsets.UTF_8
      ).toArray(new String[0])
    );

    ((InMemoryByteDataStore<LegacyStoreEntry>) dataStoreFactory
      .withType(LegacyStoreEntry.class)
      .withName("trace-monitor")
      .build()
    ).putRawXml(file, xml);
  }
}
