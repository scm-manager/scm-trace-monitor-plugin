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

import com.cloudogu.scm.tracemonitor.IndexLinkEnricher;
import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexLinkEnricherTest {

  @Mock
  private Subject subject;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;
  @Mock
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;

  private IndexLinkEnricher enricher;

  @BeforeEach
  void initEnricher() {
    ThreadContext.bind(subject);
    enricher = new IndexLinkEnricher(Providers.of(scmPathInfoStore));
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldAppendConfigLink() {
    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/scm/"));
    when(subject.isPermitted("configuration:read:traceMonitor")).thenReturn(true);
    enricher.enrich(context, appender);

    verify(appender).appendLink("traceMonitorConfig", "/scm/v2/config/trace-monitor/");
  }

  @Test
  void shouldNotAppendLink() {
    when(subject.isPermitted("configuration:read:traceMonitor")).thenReturn(false);
    enricher.enrich(context, appender);

    verify(appender, never()).appendLink("traceMonitorConfig", "/scm/v2/config/trace-monitor/");
  }

  @Test
  void shouldAppendTraceMonitorLinks() {
    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/scm/"));
    when(subject.isPermitted("configuration:read:traceMonitor")).thenReturn(false);
    when(subject.isPermitted("traceMonitor:read")).thenReturn(true);
    enricher.enrich(context, appender);

    verify(appender).appendLink("traceMonitor", "/scm/v2/trace-monitor/");
    verify(appender).appendLink("traceMonitorKinds", "/scm/v2/trace-monitor/available-kinds");
  }
}
