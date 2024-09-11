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

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
@Getter
@SuppressWarnings("java:S2160") // we do not need equals and hashcode for dto`s
public class TraceMonitorResultDto extends HalRepresentation {
  private final Collection<SpanContextDto> spans;
  private int page;
  private int pageSize;
  private int pageTotal;

  public TraceMonitorResultDto(Links links, Collection<SpanContextDto> spans, int page, int pageSize, int pageTotal) {
    super(links);
    this.spans = spans;
    this.page = page;
    this.pageSize = pageSize;
    this.pageTotal = pageTotal;
  }
}
