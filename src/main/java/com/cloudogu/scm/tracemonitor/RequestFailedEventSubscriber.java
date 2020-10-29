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

import com.cloudogu.scm.landingpage.myevents.MyEvent;
import com.github.legman.Subscribe;
import lombok.Getter;
import sonia.scm.EagerSingleton;
import sonia.scm.event.Event;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.trace.SpanContext;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Extension
@EagerSingleton
@Requires("scm-landingpage-plugin")
public class RequestFailedEventSubscriber {

  private final ScmEventBus eventBus;

  @Inject
  public RequestFailedEventSubscriber(ScmEventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Subscribe
  public void onEvent(RequestFailedEvent event) {
    eventBus.post(new RequestFailedMyEvent(event.getContext()));
  }

  @Event
  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement
  @Getter
  public static class RequestFailedMyEvent extends MyEvent {

    private final SpanContext context;

    public RequestFailedMyEvent(SpanContext context) {
      super("RequestFailedMyEvent", "traceMonitor:read");
      this.context = context;
    }
  }
}
