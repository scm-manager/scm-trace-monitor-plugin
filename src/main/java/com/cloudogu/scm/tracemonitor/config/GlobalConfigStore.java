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

package com.cloudogu.scm.tracemonitor.config;

import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

public class GlobalConfigStore {

  private static final String STORE_NAME = "trace-monitor-config";

  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public GlobalConfigStore(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public GlobalConfig get() {
    GlobalConfig globalConfig = createStore().get();
    if (globalConfig == null) {
      globalConfig = new GlobalConfig();
    }
    return globalConfig;
  }

  public void update(@NotNull GlobalConfig config) {
    ConfigurationPermissions.write("traceMonitor").check();
    createStore().set(config);
  }

  private ConfigurationStore<GlobalConfig> createStore() {
    return storeFactory.withType(GlobalConfig.class).withName(STORE_NAME).build();
  }
}
