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
import {
  Checkbox,
  Column,
  comparators,
  devices,
  FilterInput,
  Icon,
  NoStyleButton,
  Select,
  SelectItem,
  Table,
  TextColumn
} from "@scm-manager/ui-components";
import { Span } from "./TraceMonitor";
import SpanDetailsModal from "./SpanDetailsModal";
import { convertMillisToString, formatAsTimestamp } from "./time";

const FilterLabel = styled.span`
  color: #9a9a9a;
  margin: 0 0.5rem;
  word-break: keep-all;
`;

const Level = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;

  @media screen and (max-width: ${devices.mobile.width}px) {
    flex-direction: column;
    align-items: flex-start;
  }
`;

const FlexColumn = styled.div`
  flex-grow: 0;
  flex-basis: auto;
`;

const FlexNoneColumn = styled.div`
  flex: none;
`;

const TableActions = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  .field:not(:last-child) {
    margin-bottom: 0;
  }

  @media screen and (max-width: ${devices.desktop.width - 1}px) {
    flex-direction: column;
    align-items: flex-start;
  }
`;

const ScrollableTable = styled.div`
  overflow-x: auto;
`;

type Props = {
  spans: Span[];
  categories: string[];
  categoryFilter: string;
  changeCategoryFilter: (category: string) => void;
  statusFilter: boolean;
  changeStatusFilter: (status: boolean) => void;
};

const TraceMonitorTable: FC<Props> = ({
  spans,
  statusFilter,
  changeStatusFilter,
  categoryFilter,
  changeCategoryFilter,
  categories
}) => {
  const [t] = useTranslation("plugins");
  const [searchFilter, setSearchFilter] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [modalData, setModalData] = useState<Span | undefined>();

  const createCategoryFilterOptions = () => {
    const filterCategories: SelectItem[] = [];
    filterCategories.push({ label: t("scm-trace-monitor-plugin.tableActions.all"), value: "ALL" });
    categories.forEach(category => filterCategories.push({ label: category, value: category }));
    return filterCategories;
  };

  const tableActions = (
    <TableActions className="columns column is-mobile-action-spacing is-clipped">
      <FlexColumn className="column is-flex">
        <Checkbox
          checked={statusFilter}
          label={t("scm-trace-monitor-plugin.tableActions.statusFilter")}
          onChange={changeStatusFilter}
        />
      </FlexColumn>
      <FlexNoneColumn className="column is-centered is-flex">
        <Level>
          <FilterLabel>{t("scm-trace-monitor-plugin.tableActions.categoryFilter")}</FilterLabel>
          <Select value={categoryFilter} options={createCategoryFilterOptions()} onChange={changeCategoryFilter} />
        </Level>
      </FlexNoneColumn>
      <div className="column is-flex">
        <Level>
          <FilterLabel>{t("scm-trace-monitor-plugin.tableActions.searchFilter")}</FilterLabel>
          <FilterInput value={searchFilter} filter={setSearchFilter} placeholder="" />
        </Level>
      </div>
    </TableActions>
  );

  const filteredSpans = () => {
    if (searchFilter) {
      const filtered: Span[] = [];
      for (const span of spans) {
        let add = false;
        if (span.labels) {
          add = Object.values(span.labels).some((value: any) => value.includes(searchFilter));
        }
        if (add) {
          filtered.push(span);
        }
      }
      return filtered;
    }
    return spans;
  };

  const openModal = (span: Span) => {
    setModalData(span);
    setShowModal(true);
  };

  return (
    <>
      {showModal && <SpanDetailsModal onClose={() => setShowModal(false)} modalData={modalData} active={showModal} />}
      {tableActions}
      <ScrollableTable>
        <Table data={filteredSpans()} emptyMessage={t("scm-trace-monitor-plugin.table.emptyMessage")}>
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
      </ScrollableTable>
    </>
  );
};

export default TraceMonitorTable;
