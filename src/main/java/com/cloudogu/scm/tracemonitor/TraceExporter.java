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

import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;
import sonia.scm.trace.Exporter;
import sonia.scm.trace.SpanContext;

import javax.inject.Inject;

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
