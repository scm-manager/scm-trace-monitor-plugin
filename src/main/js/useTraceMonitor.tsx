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
import { apiClient } from "@scm-manager/ui-components";
import { useQuery } from "react-query";
import { ApiResult, useRequiredIndexLink } from "@scm-manager/ui-api";
import { PagedCollection, HalRepresentation } from "@scm-manager/ui-types";

export type Span = {
  kind: string;
  opened: Date;
  closed: Date;
  durationInMillis: number;
  labels: { [key: string]: string };
  failed: boolean;
};

type TraceMonitor = PagedCollection & {
  spans: Span[];
  pageSize: number;
};

type TraceMonitorCategories = HalRepresentation & {
  categories: string[];
};

type TraceMonitorConfig = HalRepresentation & {
  storeSize: number;
};

export const useTraceMonitor = (
  page: number,
  categoryFilter: string,
  onlyFailedFilter: boolean
): ApiResult<TraceMonitor> => {
  const indexLink = useRequiredIndexLink("traceMonitor");
  return useQuery<TraceMonitor, Error>(["traceMonitor", page, categoryFilter, onlyFailedFilter], () => {
    let link = indexLink + `?page=${page}`;
    if (categoryFilter && categoryFilter !== "ALL") {
      link += `&category=${categoryFilter}`;
    }
    if (onlyFailedFilter) {
      link += "&onlyFailed=true";
    }
    return apiClient.get(link).then(response => response.json());
  });
};

export const useTraceMonitorCategories = (): ApiResult<TraceMonitorCategories> => {
  const indexLink = useRequiredIndexLink("traceMonitorCategories");
  return useQuery<TraceMonitorCategories, Error>(["traceMonitorCategories"], () => {
    return apiClient.get(indexLink).then(response => response.json());
  });
};

export const useTraceMonitorConfig = (): ApiResult<TraceMonitorConfig> => {
  const indexLink = useRequiredIndexLink("traceMonitorConfig");
  return useQuery<TraceMonitorConfig, Error>(["traceMonitorConfig"], () => {
    return apiClient.get(indexLink).then(response => response.json());
  });
};
