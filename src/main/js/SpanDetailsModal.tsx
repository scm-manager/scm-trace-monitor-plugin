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
import { Span } from "./TraceMonitor";
import { Modal, Icon } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { convertMillisToString, formatAsTimestamp } from "./table";

type Props = {
  active: boolean;
  onClose: () => void;
  modalData?: Span;
};

const SpanDetailsModal: FC<Props> = ({ onClose, modalData, active }) => {
  const [t] = useTranslation("plugins");
  const body = modalData ? (
    <>
      <table className="table">
        <tbody>
          <tr>
            <th>{t("scm-trace-monitor-plugin.table.column.status")}</th>
            <td>
              {modalData.failed ? (
                <>
                  <Icon color="danger" name="exclamation-triangle" />
                  {" " + t("scm-trace-monitor-plugin.table.failed")}
                </>
              ) : (
                <>
                  <Icon color="success" name="check-circle" iconStyle="far" />
                  {" " + t("scm-trace-monitor-plugin.table.success")}
                </>
              )}
            </td>
          </tr>
          <tr>
            <th>{t("scm-trace-monitor-plugin.table.column.timestamp")}</th>
            <td>{formatAsTimestamp(modalData)}</td>
          </tr>
          <tr>
            <th>{t("scm-trace-monitor-plugin.table.column.duration")}</th>
            <td>{convertMillisToString(modalData.durationInMillis)}</td>
          </tr>
          {Object.entries(modalData.labels).map((entry) => (
            <tr>
              <th>
                {t("scm-trace-monitor-plugin.table.column.spanLabel") + " "}
                {entry[0].charAt(0).toUpperCase() + entry[0].slice(1)}
              </th>
              <td>{entry[1]}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  ) : null;

  return (
    <Modal closeFunction={onClose} title={t("scm-trace-monitor-plugin.modal.title")} body={body} active={active} />
  );
};

export default SpanDetailsModal;
