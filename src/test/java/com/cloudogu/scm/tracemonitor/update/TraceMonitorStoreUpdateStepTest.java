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
