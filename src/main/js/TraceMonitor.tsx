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

import React, { FC, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, useRouteMatch } from "react-router-dom";
import { Links } from "@scm-manager/ui-types";
import { ErrorNotification, LinkPaginator, Loading, Subtitle, Title, urls } from "@scm-manager/ui-components";
import TraceMonitorTable from "./TraceMonitorTable";
import { useTraceMonitor, useTraceMonitorKinds } from "./useTraceMonitor";
import TraceMonitorTableActions from "./TraceMonitorTableActions";
import { useDocumentTitle } from "@scm-manager/ui-core";

const usePrevious = <T,>(value: T) => {
  const ref = useRef(value);

  useEffect(() => {
    ref.current = value;
  }, [value]);

  return ref.current;
};

const TraceMonitor: FC<{ links: Links }> = () => {
  const [t] = useTranslation("plugins");

  const match = useRouteMatch();
  const page = urls.getPageFromMatch(match);

  const [kindFilter, setKindFilter] = useState("ALL");
  const previousKindFilter = usePrevious(kindFilter);

  const [onlyFailedFilter, setOnlyFailedFilter] = useState(false);
  const previousOnlyFailedFilter = usePrevious(onlyFailedFilter);

  const [queryLabelFilter, setQueryLabelFilter] = useState("");
  const previousLabelFilter = usePrevious(queryLabelFilter);

  const [redirectToFirstPage, setRedirectToFirstPage] = useState(false);

  const { data, error, isLoading } = useTraceMonitor(page, kindFilter, onlyFailedFilter, queryLabelFilter);
  const { data: kinds, error: kindsError, isLoading: kindsLoading } = useTraceMonitorKinds();

  const getDocumentTitle = () => {
    if (data) {
      return t("scm-trace-monitor-plugin.documentTitle", { current: page, total: data?.pageTotal });
    } else {
      return t("scm-trace-monitor-plugin.subtitle");
    }
  }
  useDocumentTitle(getDocumentTitle());

  useEffect(() => {
    if (queryLabelFilter !== previousLabelFilter) {
      setRedirectToFirstPage(true);
    }
  }, [queryLabelFilter, previousLabelFilter]);

  useEffect(() => {
    if (kindFilter !== previousKindFilter) {
      setRedirectToFirstPage(true);
    }
  }, [kindFilter, previousKindFilter]);

  useEffect(() => {
    if (onlyFailedFilter !== previousOnlyFailedFilter) {
      setRedirectToFirstPage(true);
    }
  }, [onlyFailedFilter, previousOnlyFailedFilter]);

  useEffect(() => {
    if (redirectToFirstPage) {
      setRedirectToFirstPage(false);
    }
  }, [redirectToFirstPage]);

  if (redirectToFirstPage || (data && data.pageTotal < page && page > 1)) {
    return <Redirect to={"/admin/trace-monitor/1"} />;
  }

  if (error || kindsError) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading || kindsLoading || !data || !kinds) {
    return <Loading />;
  }

  return (
    <>
      <Title title={t("scm-trace-monitor-plugin.title")} />
      <Subtitle subtitle={t("scm-trace-monitor-plugin.subtitle")} />
      <TraceMonitorTableActions
        key="actions"
        kindFilter={kindFilter}
        changeKindFilter={setKindFilter}
        statusFilter={onlyFailedFilter}
        changeStatusFilter={setOnlyFailedFilter}
        kinds={kinds.kinds}
        labelFilter={queryLabelFilter}
        setLabelFilter={setQueryLabelFilter}
      />
      <TraceMonitorTable spans={data.spans} />
      <hr />
      <LinkPaginator collection={data} page={page} />
    </>
  );
};

export default TraceMonitor;
