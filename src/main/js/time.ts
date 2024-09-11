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

import { Span } from "./TraceMonitor";
import { format } from "date-fns";

export const convertMillisToString = (ms: number) => {
  const showWith0 = (value: number) => (value < 10 ? `0${value}` : value);
  const minutes = showWith0(Math.floor((ms / (1000 * 60)) % 60));
  const seconds = showWith0(Math.floor((ms / 1000) % 60));
  const millis = showWith0(Math.floor(ms % 1000));
  return `${minutes} m ${seconds} s ${millis} ms`;
};

export const formatAsTimestamp = (span: Span) => {
  return format(new Date(span.closed), "yyyy-MM-dd HH:mm:ss");
};
