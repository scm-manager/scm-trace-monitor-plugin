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

import lombok.Getter;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.trace.SpanContext;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TraceStore {

  private static final String STORE_NAME = "trace-monitor";
  private final DataStoreFactory storeFactory;

  @Inject
  public TraceStore(DataStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public Collection<SpanContext> getAll() {
    DataStore<StoreEntry> store = createStore();
    List<SpanContext> spans = new ArrayList<>();
    store.getAll().values().forEach(entry -> spans.addAll(entry.getSpans()));
    return spans;
  }

  public Collection<SpanContext> get(String kind) {
    return getAll().stream().filter(span -> kind.equalsIgnoreCase(span.getKind())).collect(Collectors.toList());
  }

  public void add(SpanContext spanContext) {
    DataStore<StoreEntry> store = createStore();
    StoreEntry storeEntry = store.get(spanContext.getKind());
    if (storeEntry == null) {
      storeEntry = new StoreEntry();
    }
    storeEntry.getSpans().add(spanContext);
    store.put(spanContext.getKind(), storeEntry);

  }

  private DataStore<StoreEntry> createStore() {
    return storeFactory.withType(StoreEntry.class).withName(STORE_NAME).build();
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  static class StoreEntry {
    // TODO get size from config
    private final EvictingQueue<SpanContext> spans = EvictingQueue.create(100);
  }

}