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
