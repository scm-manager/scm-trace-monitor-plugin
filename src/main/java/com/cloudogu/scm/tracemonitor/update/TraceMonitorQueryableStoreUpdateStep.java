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

import com.cloudogu.scm.tracemonitor.EvictingQueue;
import com.cloudogu.scm.tracemonitor.SpanContextStoreWrapper;
import com.cloudogu.scm.tracemonitor.SpanContextStoreWrapperStoreFactory;
import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.trace.SpanContext;
import sonia.scm.version.Version;

@Extension
class TraceMonitorQueryableStoreUpdateStep implements UpdateStep {

  private static final String STORE_NAME = "trace-monitor";
  private final SpanContextStoreWrapperStoreFactory queryableStoreFactory;
  private final DataStoreFactory dataStoreFactory;

  @Inject
  TraceMonitorQueryableStoreUpdateStep(SpanContextStoreWrapperStoreFactory queryableStoreFactory, DataStoreFactory dataStoreFactory) {
    this.queryableStoreFactory = queryableStoreFactory;
    this.dataStoreFactory = dataStoreFactory;
  }

  @Override
  public void doUpdate() {
    dataStoreFactory.withType(LegacyStoreEntry.class).withName(STORE_NAME).build()
      .getAll()
      .forEach((kind, legacyStoreEntry) -> {
        try (var dataStore = queryableStoreFactory.getMutable(kind)) {
          dataStore.transactional(() -> {
            legacyStoreEntry.getSpans().forEach(spanContext -> {
              SpanContextStoreWrapper wrapper = new SpanContextStoreWrapper(spanContext);
              dataStore.put(wrapper);
            });
            return true;
          });
        }
      });
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("3.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.plugin.tracemonitor";
  }

  @XmlRootElement(name = "storeEntry")
  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  @NoArgsConstructor
  static class LegacyStoreEntry {
    @XmlElement(name = "request")
    private EvictingQueue<SpanContext> spans;

    @VisibleForTesting
    public LegacyStoreEntry(int storeSize) {
      spans = EvictingQueue.create(storeSize);
    }
  }
}
