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
import { Configuration, InputField } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

export type Configuration = {
  storeSize: number;
  cleanupExpression: string;
};

type Props = {
  initialConfiguration: Configuration;
  readOnly: boolean;
  onConfigurationChange: (p1: Configuration, p2: boolean) => void;
};

const GlobalTraceMonitorConfigurationForm: FC<Props> = ({ initialConfiguration, readOnly, onConfigurationChange }) => {
  const [t] = useTranslation("plugins");
  const [storeSize, setStoreSize] = useState<number>(initialConfiguration.storeSize);
  const [cleanupExpression, setCleanupExpression] = useState<string>(initialConfiguration.cleanupExpression);

  useEffect(() => {
    onConfigurationChange({ storeSize, cleanupExpression }, isValidConfig());
  }, [storeSize, cleanupExpression]);

  const isValidConfig = () => {
    return (
      (storeSize !== initialConfiguration.storeSize ||
      cleanupExpression !== initialConfiguration.cleanupExpression) &&
      storeSize > 0 &&
      cleanupExpression.length > 0
    );
  };

  return (
    <>
      <InputField
        label={t("scm-trace-monitor-plugin.config.form.storeSize")}
        onChange={(size) => setStoreSize(parseInt(size))}
        type="number"
        value={storeSize.toString()}
        disabled={readOnly}
        helpText={t("scm-trace-monitor-plugin.config.form.storeSizeHelpText")}
      />
      <InputField
        label={t("scm-trace-monitor-plugin.config.form.cleanupExpression")}
        onChange={setCleanupExpression}
        value={cleanupExpression}
        disabled={readOnly}
        helpText={t("scm-trace-monitor-plugin.config.form.cleanupExpressionHelpText")}
      />
    </>
  );
};

export default GlobalTraceMonitorConfigurationForm;
