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
import { apiClient, ErrorNotification, Loading, Title, Subtitle } from "@scm-manager/ui-components";
import TraceMonitorTable from "./TraceMonitorTable";
import { useTranslation } from "react-i18next";

export type Span = {
  kind: string;
  opened: Date;
  closed: Date;
  durationInMillis: number;
  labels: {};
  failed: boolean;
};

type Props = {
  link: string;
};

const TraceMonitor: FC<Props> = ({ link }) => {
  const [spans, setSpans] = useState<Span[]>([]);
  const [categoryFilter, setCategoryFilter] = useState("");
  const [onlyFailedFilter, setOnlyFailedFilter] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | undefined>();
  const [t] = useTranslation("plugins");

  useEffect(() => {
    apiClient
      .get(createUrl())
      .then(r => r.json())
      .then(r => r.spans)
      .then(setSpans)
      .then(() => setLoading(false))
      .catch(setError);
  }, [categoryFilter, onlyFailedFilter]);

  const createUrl = () => {
    let url = link;
    if (!categoryFilter && !onlyFailedFilter) {
      return url;
    }

    url += `?`;
    if (categoryFilter && categoryFilter !== "ALL") {
      url += `category=${categoryFilter}`;
    }
    if (onlyFailedFilter) {
      if (categoryFilter) {
        url += "&";
      }
      url += `onlyFailed=true`;
    }

    return url;
  };

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  return (
    <>
      <Title title={t("scm-trace-monitor-plugin.title")} />
      <Subtitle subtitle={t("scm-trace-monitor-plugin.subtitle")} />
      <TraceMonitorTable
        spans={spans}
        categoryFilter={categoryFilter}
        changeCategoryFilter={setCategoryFilter}
        statusFilter={onlyFailedFilter}
        changeStatusFilter={setOnlyFailedFilter}
      />
    </>
  );
};

export default TraceMonitor;
