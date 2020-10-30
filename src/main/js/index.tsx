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

import {binder} from "@scm-manager/ui-extensions";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalTraceMonitorConfiguration from "./config/GlobalTraceMonitorConfiguration";
import RequestFailedEvent from "./landingpage/RequestFailedEvent";
import { Route } from "react-router-dom";
import React from "react";
import TraceMonitorNavigation from "./TraceMonitorNavigation";
import { Links } from "@scm-manager/ui-types";
import TraceMonitor from "./TraceMonitor";


cfgBinder.bindGlobal("/trace-monitor", "scm-trace-monitor-plugin.global.nav-link", "traceMonitorConfig", GlobalTraceMonitorConfiguration);

binder.bind("landingpage.myevents", RequestFailedEvent);

type PredicateProps = {
  links: Links;
};

// @VisibleForTesting
export const predicate = ({ links }: PredicateProps) => {
  return !!(links && links.traceMonitor);
};

const TraceMonitorRoute = ({ links }) => {
  return (
    <>
      <Route path="/admin/trace-monitor" component={() => <TraceMonitor link={links.traceMonitor.href} />} />
    </>
  );
};

binder.bind("admin.route", TraceMonitorRoute, predicate);

binder.bind("admin.navigation", TraceMonitorNavigation, predicate);

