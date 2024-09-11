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

import com.cloudogu.scm.landingpage.myevents.MyEvent;
import com.github.legman.Subscribe;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sonia.scm.EagerSingleton;
import sonia.scm.event.Event;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.trace.SpanContext;

import jakarta.inject.Inject;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

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
  @NoArgsConstructor
  @Getter
  public static class RequestFailedMyEvent extends MyEvent {

    private SpanContext context;

    public RequestFailedMyEvent(SpanContext context) {
      super("RequestFailedMyEvent", "traceMonitor:read");
      this.context = context;
    }
  }
}
