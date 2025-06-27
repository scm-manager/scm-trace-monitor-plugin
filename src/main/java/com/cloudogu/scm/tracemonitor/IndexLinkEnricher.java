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

import com.cloudogu.scm.tracemonitor.config.GlobalConfigResource;
import com.google.inject.Provider;
import org.apache.shiro.SecurityUtils;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;

import jakarta.inject.Inject;

@Extension
@Enrich(Index.class)
public class IndexLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> pathInfoStore;

  @Inject
  public IndexLinkEnricher(Provider<ScmPathInfoStore> pathInfoStore) {
    this.pathInfoStore = pathInfoStore;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    if (SecurityUtils.getSubject().isPermitted("configuration:read:traceMonitor")) {
      String globalTraceMonitorConfigUrl = new LinkBuilder(pathInfoStore.get().get(), GlobalConfigResource.class)
        .method("get")
        .parameters()
        .href();
      appender.appendLink("traceMonitorConfig", globalTraceMonitorConfigUrl);
    }
    if (SecurityUtils.getSubject().isPermitted("traceMonitor:read")) {
      String traceMonitorUrl = new LinkBuilder(pathInfoStore.get().get(), TraceMonitorResource.class)
        .method("get")
        .parameters()
        .href();
      appender.appendLink("traceMonitor", traceMonitorUrl);

      String traceMonitorKindsUrl = new LinkBuilder(pathInfoStore.get().get(), TraceMonitorResource.class)
        .method("getAvailableKinds")
        .parameters()
        .href();
      appender.appendLink("traceMonitorKinds", traceMonitorKindsUrl);
    }
  }
}
