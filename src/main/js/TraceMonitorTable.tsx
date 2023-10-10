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
