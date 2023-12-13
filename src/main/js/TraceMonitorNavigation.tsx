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
import React, { FC } from "react";
import { SecondaryNavigationItem } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useRouteMatch } from "react-router-dom";

const TraceMonitorNavigation: FC = () => {
  const [t] = useTranslation("plugins");
  const match = useRouteMatch();

  const matchesTraceMonitor = (route: any) => {
    const regex = new RegExp("/admin/trace-monitor/.+");
    return !!route.location.pathname.match(regex);
  };

  return (
    <SecondaryNavigationItem
      to={match.url + "/trace-monitor/"}
      icon="fas fa-desktop"
      label={t("scm-trace-monitor-plugin.navLink")}
      title={t("scm-trace-monitor-plugin.navLink")}
      activeWhenMatch={matchesTraceMonitor}
      activeOnlyWhenExact={false}
    />
  );
};

export default TraceMonitorNavigation;
