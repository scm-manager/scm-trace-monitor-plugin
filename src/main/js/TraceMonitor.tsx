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

import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, useHistory, useLocation, useRouteMatch } from "react-router-dom";
import { Links } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Title, Subtitle, urls, LinkPaginator } from "@scm-manager/ui-components";
import TraceMonitorTable from "./TraceMonitorTable";
import { useTraceMonitor, useTraceMonitorCategories } from "./useTraceMonitor";
import TraceMonitorTableActions from "./TraceMonitorTableActions";

const TraceMonitor: FC<{ links: Links }> = () => {
  const [t] = useTranslation("plugins");
  const match = useRouteMatch();
  const location = useLocation();
  const history = useHistory();
  const page = urls.getPageFromMatch(match);
  const [categoryFilter, setCategoryFilter] = useState("ALL");
  const [onlyFailedFilter, setOnlyFailedFilter] = useState(false);
  const [searchFilter, setSearchFilter] = useState("");
  const { data, error, isLoading } = useTraceMonitor(page, categoryFilter, onlyFailedFilter, searchFilter);
  const { data: categories, error: categoriesError, isLoading: categoriesLoading } = useTraceMonitorCategories();

  if (error || categoriesError) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading || categoriesLoading || !data || !categories) {
    return <Loading />;
  }

  if (data && data.pageTotal < page && page > 1) {
    return <Redirect to={`/admin/trace-monitor/${data.pageTotal}`} />;
  }

  return (
    <>
      <Title title={t("scm-trace-monitor-plugin.title")} />
      <Subtitle subtitle={t("scm-trace-monitor-plugin.subtitle")} />
      <TraceMonitorTableActions
        key="actions"
        categoryFilter={categoryFilter}
        changeCategoryFilter={setCategoryFilter}
        statusFilter={onlyFailedFilter}
        changeStatusFilter={setOnlyFailedFilter}
        categories={categories.categories}
        searchFilter={searchFilter}
        setSearchFilter={setSearchFilter}
      />
      <TraceMonitorTable spans={data.spans} />
      <hr />
      <LinkPaginator collection={data} page={page} />
    </>
  );
};

export default TraceMonitor;
