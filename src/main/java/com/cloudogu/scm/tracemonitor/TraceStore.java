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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Striped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shiro.SecurityUtils;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.trace.SpanContext;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

@Singleton
public class TraceStore {

  private static final String STORE_NAME = "trace-monitor";
  private final DataStoreFactory storeFactory;
  private final GlobalConfigStore globalConfigStore;
  private final Striped<ReadWriteLock> locks = Striped.readWriteLock(10);

  @Inject
  public TraceStore(DataStoreFactory storeFactory, GlobalConfigStore globalConfigStore) {
    this.storeFactory = storeFactory;
    this.globalConfigStore = globalConfigStore;
  }

  public Collection<SpanContext> getAll() {
    SecurityUtils.getSubject().checkPermission("traceMonitor:read");
    List<SpanContext> spans = new ArrayList<>();
    createStore()
      .getAll()
      .values()
      .forEach(entry -> spans.addAll(entry.getSpans()));
    return spans;
  }

  public Collection<SpanContext> get(String kind) {
    SecurityUtils.getSubject().checkPermission("traceMonitor:read");
    return doSynchronized(kind, false, () -> Collections.unmodifiableCollection(createStore().get(kind).getSpans()));
  }

  synchronized void add(SpanContext spanContext) {
    doSynchronized(spanContext.getKind(), true, () -> {
      DataStore<StoreEntry> store = createStore();
      StoreEntry storeEntry = store.get(spanContext.getKind());
      int configuredStoreSize = globalConfigStore.get().getStoreSize();
      if (storeEntry == null) {
        storeEntry = new StoreEntry(configuredStoreSize);
      }
      storeEntry = resizeStoreEntryMaxSize(storeEntry, configuredStoreSize);
      storeEntry.getSpans().add(spanContext);
      store.put(spanContext.getKind(), storeEntry);
      return null;
    });
  }

  private StoreEntry resizeStoreEntryMaxSize(StoreEntry storeEntry, int configuredStoreSize) {
    if (storeEntry.getSpans().maxSize != configuredStoreSize) {
      if (configuredStoreSize > storeEntry.getSpans().maxSize) {
        storeEntry.getSpans().maxSize = configuredStoreSize;
      } else {
        StoreEntry resizedStoreEntry = new StoreEntry(configuredStoreSize);
        storeEntry.getSpans().forEach(s -> resizedStoreEntry.getSpans().add(s));
        storeEntry = resizedStoreEntry;
      }
    }
    return storeEntry;
  }

  private <T> T doSynchronized(String category, boolean write, Supplier<T> callback) {
    final ReadWriteLock lockFactory = locks.get(category);
    Lock lock = write ? lockFactory.writeLock() : lockFactory.readLock();
    lock.lock();
    try {
      return callback.get();
    } finally {
      lock.unlock();
    }
  }

  private DataStore<StoreEntry> createStore() {
    return storeFactory.withType(StoreEntry.class).withName(STORE_NAME).build();
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  @NoArgsConstructor
  public static class StoreEntry {
    @XmlElement(name = "request")
    private EvictingQueue<SpanContext> spans;

    @VisibleForTesting
    public StoreEntry(int storeSize) {
      spans = EvictingQueue.create(storeSize);
    }
  }
}
