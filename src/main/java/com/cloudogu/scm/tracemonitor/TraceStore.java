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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
