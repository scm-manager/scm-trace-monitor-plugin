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
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.util.Map;

import static sonia.scm.version.Version.parse;

@Extension
public class TraceMonitorStoreUpdateStep implements UpdateStep {

  private static final String STORE_NAME = "trace-monitor";
  private final DataStoreFactory dataStoreFactory;
  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;

  @Inject
  public TraceMonitorStoreUpdateStep(DataStoreFactory dataStoreFactory, ConfigurationEntryStoreFactory configurationEntryStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
  }

  @Override
  public void doUpdate() {
    ConfigurationEntryStore<TraceStore.StoreEntry> configurationEntryStore = configurationEntryStoreFactory
      .withType(TraceStore.StoreEntry.class)
      .withName(STORE_NAME)
      .build();
    Map<String, TraceStore.StoreEntry> spans = configurationEntryStore.getAll();

    DataStore<TraceStore.StoreEntry> dataStore = dataStoreFactory
      .withType(TraceStore.StoreEntry.class)
      .withName(STORE_NAME)
      .build();

    spans.forEach(dataStore::put);
    configurationEntryStore.clear();
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.9.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.plugin.tracemonitor";
  }
}
