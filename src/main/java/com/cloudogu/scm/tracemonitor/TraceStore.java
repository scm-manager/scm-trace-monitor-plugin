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
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStore;
import sonia.scm.trace.SpanContext;

import java.util.Collection;

@Slf4j
@Singleton
public class TraceStore {

  private final SpanContextStoreWrapperStoreFactory storeFactory;
  private final GlobalConfigStore globalConfigStore;

  @Inject
  public TraceStore(SpanContextStoreWrapperStoreFactory storeFactory, GlobalConfigStore globalConfigStore) {
    this.storeFactory = storeFactory;
    this.globalConfigStore = globalConfigStore;
  }

  public Collection<SpanContext> getAll() {
    SecurityUtils.getSubject().checkPermission("traceMonitor:read");
    log.debug("reading all spans");
    try (QueryableStore<SpanContextStoreWrapper> store = storeFactory.getOverall()) {
      return store
        .query()
        .orderBy(SpanContextStoreWrapperQueryFields.INTERNAL_ID, QueryableStore.Order.DESC)
        .findAll(0, globalConfigStore.get().getStoreSize())
        .stream()
        .map(SpanContextStoreWrapper::getSpanContext)
        .toList();
    }
  }

  public Collection<String> getKinds() {
    log.debug("reading all kinds of spans");
    try (QueryableStore<SpanContextStoreWrapper> store = storeFactory.getOverall()) {
      return store
        .query()
        .project(SpanContextStoreWrapperQueryFields.SPANCONTEXTKIND_ID)
        .distinct()
        .findAll()
        .stream()
        .map(row -> row[0].toString())
        .sorted()
        .toList();
    }
  }

  public void cleanUp() {
    log.debug("cleaning up span stores");
    getKinds()
      .forEach(kind -> {
        try (QueryableMutableStore<SpanContextStoreWrapper> store = storeFactory.getMutable(kind)) {
          store.query()
            .orderBy(SpanContextStoreWrapperQueryFields.SPANCONTEXTKIND_ID, QueryableStore.Order.DESC)
            .retain(globalConfigStore.get().getStoreSize());
        }
      });
  }

  public Collection<SpanContext> get(String kind) {
    SecurityUtils.getSubject().checkPermission("traceMonitor:read");
    log.debug("reading all spans for kind '{}'", kind);
    try (QueryableStore<SpanContextStoreWrapper> store = storeFactory.get(kind)) {
      return store
        .query()
        .orderBy(SpanContextStoreWrapperQueryFields.INTERNAL_ID, QueryableStore.Order.DESC)
        .findAll(0, globalConfigStore.get().getStoreSize())
        .stream()
        .map(SpanContextStoreWrapper::getSpanContext)
        .toList();
    }
  }

  synchronized void add(SpanContext spanContext) {
    log.debug("add span to store for kind '{}'", spanContext.getKind());
    try (QueryableMutableStore<SpanContextStoreWrapper> store = storeFactory.getMutable(spanContext.getKind())) {
      store.put(new SpanContextStoreWrapper(spanContext));
    }
  }
}
