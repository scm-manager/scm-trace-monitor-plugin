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
