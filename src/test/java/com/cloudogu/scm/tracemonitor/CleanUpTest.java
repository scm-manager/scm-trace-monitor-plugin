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

import com.cloudogu.scm.tracemonitor.config.GlobalConfig;
import com.cloudogu.scm.tracemonitor.config.GlobalConfigStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.spi.SyncAsyncExecutor;
import sonia.scm.repository.spi.SyncAsyncExecutorProvider;
import sonia.scm.schedule.Scheduler;
import sonia.scm.schedule.Task;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanupTest {

  @Mock
  Scheduler scheduler;
  @Mock
  GlobalConfigStore globalConfigStore;
  @Mock
  GlobalConfig globalConfig;

  @Mock
  TraceStore traceStore;
  @Mock
  SyncAsyncExecutorProvider syncAsyncExecutorProvider;
  @Mock
  Task task1;
  @Mock
  Task task2;
  @Mock
  SyncAsyncExecutor executor;
  @Captor
  ArgumentCaptor<Runnable> runnableCaptor;

  @Test
  void shouldExecuteCleanupTask() {
    when(globalConfigStore.get()).thenReturn(globalConfig);
    when(globalConfig.getCleanupExpression()).thenReturn("0 0 * * *");
    when(scheduler.schedule(anyString(), any(Runnable.class))).thenReturn(task1);
    when(syncAsyncExecutorProvider.createExecutorWithDefaultTimeout()).thenReturn(executor);

    Cleanup cleanup = new Cleanup(scheduler, globalConfigStore, traceStore, syncAsyncExecutorProvider);
    cleanup.contextInitialized(null);

    verify(syncAsyncExecutorProvider).createExecutorWithDefaultTimeout();
    verify(executor).execute(runnableCaptor.capture());
    assertInstanceOf(CleanupTask.class, runnableCaptor.getValue());
  }

  @Test
  void shouldCancelPreviousTaskAndReschedule() {
    when(globalConfigStore.get()).thenReturn(globalConfig);
    when(globalConfig.getCleanupExpression()).thenReturn("0 0 * * *");
    when(scheduler.schedule(anyString(), any(Runnable.class))).thenReturn(task1, task2);

    Cleanup cleanup = new Cleanup(scheduler, globalConfigStore, traceStore, syncAsyncExecutorProvider);
    cleanup.reSchedule();

    verify(task1).cancel();
    verify(scheduler, times(2)).schedule(eq("0 0 * * *"), any(Runnable.class));
  }

  @Test
  void shouldInvokeTraceStoreCleanUp() {
    CleanupTask task = new CleanupTask(traceStore);
    task.run();

    verify(traceStore, times(1)).cleanUp();
  }
}
