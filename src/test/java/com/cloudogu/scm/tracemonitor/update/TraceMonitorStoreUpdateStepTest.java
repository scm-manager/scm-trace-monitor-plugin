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

import com.cloudogu.scm.tracemonitor.TraceStore;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceMonitorStoreUpdateStepTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ConfigurationEntryStoreFactory configurationEntryStoreFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DataStoreFactory dataStoreFactory;
  @InjectMocks
  private TraceMonitorStoreUpdateStep updateStep;

  @Test
  void shouldMigrateConfigStoreToDataStore() {
    ConfigurationEntryStore<TraceStore.StoreEntry> entryStore = mock(ConfigurationEntryStore.class);
    DataStore<TraceStore.StoreEntry> dataStore = mock(DataStore.class);
    when(entryStore.getAll()).thenReturn(ImmutableMap.of("http", new TraceStore.StoreEntry(2), "Release Feed", new TraceStore.StoreEntry(12)));
    when(dataStoreFactory.withType(TraceStore.StoreEntry.class).withName("trace-monitor").build()).thenReturn(dataStore);
    when(configurationEntryStoreFactory.withType(TraceStore.StoreEntry.class).withName("trace-monitor").build()).thenReturn(entryStore);

    updateStep.doUpdate();

    ArgumentCaptor<TraceStore.StoreEntry> captor = ArgumentCaptor.forClass(TraceStore.StoreEntry.class);

    verify(dataStore).put(eq("http"), captor.capture());
    verify(dataStore).put(eq("Release Feed"), captor.capture());
    assertThat(captor.getAllValues().get(0).getSpans()).isEmpty();
    assertThat(captor.getAllValues().get(1).getSpans()).isEmpty();
  }

  @Test
  void shouldDoNothingIfNoEntriesExist() {
    ConfigurationEntryStore<TraceStore.StoreEntry> entryStore = mock(ConfigurationEntryStore.class);
    DataStore<TraceStore.StoreEntry> dataStore = mock(DataStore.class);
    when(dataStoreFactory.withType(TraceStore.StoreEntry.class).withName("trace-monitor").build()).thenReturn(dataStore);
    when(configurationEntryStoreFactory.withType(TraceStore.StoreEntry.class).withName("trace-monitor").build()).thenReturn(entryStore);

    updateStep.doUpdate();

    verify(dataStore, never()).put(anyString(), any());
  }
}
