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

import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;
import sonia.scm.trace.Exporter;
import sonia.scm.trace.SpanContext;

import jakarta.inject.Inject;

@Extension
public class TraceExporter implements Exporter {

  private final TraceStore store;
  private final ScmEventBus eventBus;

  @Inject
  public TraceExporter(TraceStore store, ScmEventBus eventBus) {
    this.store = store;
    this.eventBus = eventBus;
  }

  @Override
  public void export(SpanContext span) {
    store.add(span);
    if (span.isFailed()) {
      eventBus.post(new RequestFailedEvent(span));
    }
  }
}
