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

import { Checkbox, devices, FilterInput, Help, Select, SelectItem } from "@scm-manager/ui-components";
import React, { FC } from "react";
import styled from "styled-components";
import { useTranslation } from "react-i18next";

type Props = {
  categories: string[];
  categoryFilter: string;
  changeCategoryFilter: (category: string) => void;
  statusFilter: boolean;
  changeStatusFilter: (status: boolean) => void;
  searchFilter: string;
  setSearchFilter: (searchFilter: string) => void;
};


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

const TraceMonitorTableActions: FC<Props> = ({
  categories,
  categoryFilter,
  changeCategoryFilter,
  statusFilter,
  changeStatusFilter,
  searchFilter,
  setSearchFilter
}) => {
  const [t] = useTranslation("plugins");

  const createCategoryFilterOptions = () => {
    const filterCategories: SelectItem[] = [];
    filterCategories.push({ label: t("scm-trace-monitor-plugin.tableActions.all"), value: "ALL" });
    categories.forEach(category => filterCategories.push({ label: category, value: category }));
    return filterCategories;
  };

  return (
    <TableActions className="columns column is-mobile-action-spacing">
      <div className="column">
        <Level className="is-word-break-none">
          <span className="mr-2">
            {t("scm-trace-monitor-plugin.tableActions.searchFilter")}
            <Help message={t("scm-trace-monitor-plugin.tableActions.searchFilterHelp")} />
          </span>
          <FilterInput value={searchFilter} filter={setSearchFilter} placeholder="" autoFocus />
        </Level>
      </div>
      <FlexNoneColumn className="column is-centered is-flex">
        <Level>
          <span className="mr-2">{t("scm-trace-monitor-plugin.tableActions.categoryFilter")}</span>
          <Select value={categoryFilter} options={createCategoryFilterOptions()} onChange={changeCategoryFilter} />
        </Level>
      </FlexNoneColumn>
      <FlexColumn className="column is-flex is-flex-grow-0">
        <Checkbox
          checked={statusFilter}
          label={t("scm-trace-monitor-plugin.tableActions.statusFilter")}
          onChange={changeStatusFilter}
        />
      </FlexColumn>
    </TableActions>
  );
};

export default TraceMonitorTableActions;
