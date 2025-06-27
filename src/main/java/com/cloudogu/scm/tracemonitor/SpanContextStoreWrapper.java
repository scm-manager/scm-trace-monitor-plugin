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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sonia.scm.store.IdGenerator;
import sonia.scm.store.QueryableType;
import sonia.scm.trace.SpanContext;

@Data
@NoArgsConstructor
@AllArgsConstructor
@QueryableType(idGenerator = IdGenerator.AUTO_INCREMENT, value = SpanContextKind.class)
public class SpanContextStoreWrapper {
  private SpanContext spanContext;
}

class SpanContextKind {
}
