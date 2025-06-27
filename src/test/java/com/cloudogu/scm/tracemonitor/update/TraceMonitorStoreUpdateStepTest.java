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
import com.cloudogu.scm.tracemonitor.update.TraceMonitorStoreUpdateStep.LegacyStoreEntry;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStore;
import sonia.scm.store.QueryableStoreExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, QueryableStoreExtension.class})
@QueryableStoreExtension.QueryableTypes(SpanContextStoreWrapper.class)
class TraceMonitorStoreUpdateStepTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ConfigurationEntryStoreFactory configurationEntryStoreFactory;
  @Mock
  private ConfigurationEntryStore<LegacyStoreEntry> entryStore;
  @InjectMocks
  private TraceMonitorStoreUpdateStep updateStep;

  @BeforeEach
  void initUpdateStep(SpanContextStoreWrapperStoreFactory queryableStoreFactory) {
    this.updateStep = new TraceMonitorStoreUpdateStep(queryableStoreFactory, configurationEntryStoreFactory);
  }

  @Test
  void shouldMigrateConfigStoreToDataStore(SpanContextStoreWrapperStoreFactory storeFactory) {
    when(entryStore.getAll()).thenReturn(ImmutableMap.of("http", new LegacyStoreEntry(2), "Release Feed", new LegacyStoreEntry(12)));
    when(configurationEntryStoreFactory.withType(LegacyStoreEntry.class).withName("trace-monitor").build()).thenReturn(entryStore);

    updateStep.doUpdate();

    try (QueryableMutableStore<SpanContextStoreWrapper> store = storeFactory.getMutable("http")) {
      assertThat(store.getAll()).isEmpty();
    }
    try (QueryableMutableStore<SpanContextStoreWrapper> store = storeFactory.getMutable("Release Feed")) {
      assertThat(store.getAll()).isEmpty();
    }
  }

  @Test
  void shouldDoNothingIfNoEntriesExist(SpanContextStoreWrapperStoreFactory storeFactory) {
    when(configurationEntryStoreFactory.withType(LegacyStoreEntry.class).withName("trace-monitor").build()).thenReturn(entryStore);

    updateStep.doUpdate();

    try (QueryableStore<SpanContextStoreWrapper> store = storeFactory.getOverall()) {
      assertThat(store.query().findAll()).isEmpty();
    }
  }
}
