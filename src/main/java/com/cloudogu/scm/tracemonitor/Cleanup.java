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

import com.cloudogu.scm.tracemonitor.config.GlobalConfigStore;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.spi.SyncAsyncExecutorProvider;
import sonia.scm.schedule.Scheduler;
import sonia.scm.schedule.Task;

@Slf4j
@Extension
@Singleton
public class Cleanup implements ServletContextListener {

  private final Scheduler scheduler;
  private final GlobalConfigStore globalConfigStore;
  private final CleanupTask cleanupTask;
  private final SyncAsyncExecutorProvider syncAsyncExecutorProvider;

  private Task currentTask;

  @Inject
  Cleanup(Scheduler scheduler, GlobalConfigStore globalConfigStore, TraceStore traceStore, SyncAsyncExecutorProvider syncAsyncExecutorProvider) {
    this.scheduler = scheduler;
    this.globalConfigStore = globalConfigStore;
    this.cleanupTask = new CleanupTask(traceStore);
    this.syncAsyncExecutorProvider = syncAsyncExecutorProvider;
    reSchedule();
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    syncAsyncExecutorProvider.createExecutorWithDefaultTimeout()
      .execute(cleanupTask);
  }

  public void reSchedule() {
    log.debug("rescheduling cleanup task with expression {}", globalConfigStore.get().getCleanupExpression());
    if (currentTask != null) {
      currentTask.cancel();
    }
    this.currentTask = scheduler.schedule(globalConfigStore.get().getCleanupExpression(), cleanupTask);
  }
}

class CleanupTask implements Runnable {

  private final TraceStore traceStore;

  CleanupTask(TraceStore traceStore) {
    this.traceStore = traceStore;
  }

  @Override
  public void run() {
    traceStore.cleanUp();
  }
}
