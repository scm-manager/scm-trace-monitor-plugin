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

import React, { FC } from "react";
import { Span } from "./TraceMonitor";
import { Modal, Icon } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { convertMillisToString, formatAsTimestamp } from "./time";

type Props = {
  active: boolean;
  onClose: () => void;
  modalData?: Span;
};

const SpanDetailsModal: FC<Props> = ({ onClose, modalData, active }) => {
  const [t] = useTranslation("plugins");
  const body = modalData ? (
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
        {Object.entries(modalData.labels).map(entry => (
          <tr>
            <th>{entry[0].charAt(0).toUpperCase() + entry[0].slice(1)}</th>
            <td>{entry[1]}</td>
          </tr>
        ))}
      </tbody>
    </table>
  ) : null;

  return (
    <Modal closeFunction={onClose} title={t("scm-trace-monitor-plugin.modal.title")} body={body} active={active} />
  );
};

export default SpanDetailsModal;
