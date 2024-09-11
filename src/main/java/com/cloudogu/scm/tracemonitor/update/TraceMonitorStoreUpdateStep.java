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
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.version.Version;

import jakarta.inject.Inject;
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
