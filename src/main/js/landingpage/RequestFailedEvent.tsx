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

import React from "react";
import { useTranslation } from "react-i18next";
import { CardColumnSmall, DateFromNow, Icon } from "@scm-manager/ui-components";

const RequestFailedEvent = ({ event }) => {
  const [t] = useTranslation("plugins");
  const link = `/admin/trace-monitor`;
  const icon = <Icon name="envelope" className="fa-fw fa-lg" color="inherit" />;

  return (
    <CardColumnSmall
      link={link}
      avatar={icon}
      contentLeft={<strong>{t("scm-trace-monitor-plugin.landingpage.requestFailed.header")}</strong>}
      contentRight={<small><DateFromNow date={event.date} /></small>}
      footer={t("scm-trace-monitor-plugin.landingpage.requestFailed.category", {
        ...event.context
      })}
    />
  );
};

RequestFailedEvent.type = "RequestFailedMyEvent";

export default RequestFailedEvent;
