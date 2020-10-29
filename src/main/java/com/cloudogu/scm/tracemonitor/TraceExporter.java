package com.cloudogu.scm.tracemonitor;

import sonia.scm.plugin.Extension;
import sonia.scm.trace.Exporter;
import sonia.scm.trace.SpanContext;

import javax.inject.Inject;

@Extension
public class TraceExporter implements Exporter {

  private final TraceStore store;

  @Inject
  public TraceExporter(TraceStore store) {
    this.store = store;
  }

  @Override
  public void export(SpanContext span) {
    store.add(span);
  }
}
