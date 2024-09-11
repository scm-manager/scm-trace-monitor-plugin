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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { Column, comparators, Icon, NoStyleButton, Table, TextColumn } from "@scm-manager/ui-components";
import { Span } from "./useTraceMonitor";
import SpanDetailsModal from "./SpanDetailsModal";
import { convertMillisToString, formatAsTimestamp } from "./time";

const ScrollableViewport = styled.div`
  overflow-x: auto;
`;

type Props = {
  spans: Span[];
};

const TraceMonitorTable: FC<Props> = ({ spans }) => {
  const [t] = useTranslation("plugins");
  const [showModal, setShowModal] = useState(false);
  const [modalData, setModalData] = useState<Span | undefined>();

  const openModal = (span: Span) => {
    setModalData(span);
    setShowModal(true);
  };

  return (
    <>
      {showModal && <SpanDetailsModal onClose={() => setShowModal(false)} modalData={modalData} active={showModal} />}
      <ScrollableViewport>
        <Table data={spans} emptyMessage={t("scm-trace-monitor-plugin.table.emptyMessage")}>
          <TextColumn header={t("scm-trace-monitor-plugin.table.column.kind")} dataKey="kind" />
          <Column
            header={t("scm-trace-monitor-plugin.table.column.status")}
            createComparator={() => comparators.byKey("failed")}
            ascendingIcon="sort"
            descendingIcon="sort"
          >
            {row =>
              row.failed ? (
                <>
                  <Icon color="danger" name="exclamation-triangle" />
                  {" " + t("scm-trace-monitor-plugin.table.failed")}
                </>
              ) : (
                <>
                  <Icon color="success" name="check-circle" iconStyle="far" />
                  {" " + t("scm-trace-monitor-plugin.table.success")}
                </>
              )
            }
          </Column>
          <Column
            header={t("scm-trace-monitor-plugin.table.column.timestamp")}
            createComparator={() => comparators.byKey("opened")}
            ascendingIcon="sort"
            descendingIcon="sort"
          >
            {row => formatAsTimestamp(row)}
          </Column>
          <Column
            header={t("scm-trace-monitor-plugin.table.column.duration")}
            createComparator={() => comparators.byKey("durationInMillis")}
            ascendingIcon="sort"
            descendingIcon="sort"
          >
            {row => convertMillisToString(row.durationInMillis)}
          </Column>
          <Column header="">
            {row => (
              <NoStyleButton className={"has-text-info is-hovered"} onClick={() => openModal(row)}>
                {t("scm-trace-monitor-plugin.table.details")}
              </NoStyleButton>
            )}
          </Column>
        </Table>
      </ScrollableViewport>
    </>
  );
};

export default TraceMonitorTable;
