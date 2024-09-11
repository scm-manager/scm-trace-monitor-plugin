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

import { binder } from "@scm-manager/ui-extensions";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalTraceMonitorConfiguration from "./config/GlobalTraceMonitorConfiguration";
import RequestFailedEvent from "./landingpage/RequestFailedEvent";
import { Route, Switch } from "react-router-dom";
import React, { FC } from "react";
import TraceMonitorNavigation from "./TraceMonitorNavigation";
import { Links } from "@scm-manager/ui-types";
import TraceMonitor from "./TraceMonitor";

cfgBinder.bindGlobal(
  "/trace-monitor",
  "scm-trace-monitor-plugin.global.nav-link",
  "traceMonitorConfig",
  GlobalTraceMonitorConfiguration
);

binder.bind("landingpage.myevents", RequestFailedEvent);

type PredicateProps = {
  links: Links;
};

export const predicate = ({ links }: PredicateProps) => {
  return !!(links && links.traceMonitor);
};

const TraceMonitorRoute: FC<{ links: Links }> = ({ links }) => {
  return (
    <Switch>
      <Route path="/admin/trace-monitor/" exact>
        <TraceMonitor links={links} />
      </Route>
      <Route path="/admin/trace-monitor/:page" exact>
        <TraceMonitor links={links} />
      </Route>
    </Switch>
  );
};

binder.bind("admin.route", TraceMonitorRoute, predicate);
binder.bind("admin.navigation", TraceMonitorNavigation, predicate);
