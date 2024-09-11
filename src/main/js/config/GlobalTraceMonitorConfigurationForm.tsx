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
};

type Props = {
  initialConfiguration: Configuration;
  readOnly: boolean;
  onConfigurationChange: (p1: Configuration, p2: boolean) => void;
};

const GlobalTraceMonitorConfigurationForm: FC<Props> = ({ initialConfiguration, readOnly, onConfigurationChange }) => {
  const [t] = useTranslation("plugins");
  const [storeSize, setStoreSize] = useState<number>(initialConfiguration.storeSize);

  useEffect(() => {
    onConfigurationChange({ storeSize }, isValidConfig());
  }, [storeSize]);

  const isValidConfig = () => {
    return storeSize !== initialConfiguration.storeSize && storeSize > 0;
  };
  return (
    <>
      <InputField
        label={t("scm-trace-monitor-plugin.config.form.storeSize")}
        onChange={size => setStoreSize(parseInt(size))}
        type="number"
        value={storeSize.toString()}
        disabled={readOnly}
        helpText={t("scm-trace-monitor-plugin.config.form.storeSizeHelpText")}
      />
    </>
  );
};

export default GlobalTraceMonitorConfigurationForm;
