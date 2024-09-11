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
  onlyFailedFilter: boolean,
  labelFilter: string
): ApiResult<TraceMonitor> => {
  const indexLink = useRequiredIndexLink("traceMonitor");
  return useQuery<TraceMonitor, Error>(["traceMonitor", page, categoryFilter, onlyFailedFilter, labelFilter], () => {
    let link = indexLink + `?page=${page}&labelFilter=${labelFilter}`;
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
