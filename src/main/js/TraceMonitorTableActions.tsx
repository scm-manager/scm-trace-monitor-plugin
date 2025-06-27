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

import { Checkbox, devices, FilterInput, Help, Select, SelectItem } from "@scm-manager/ui-components";
import React, { FC } from "react";
import styled from "styled-components";
import { useTranslation } from "react-i18next";

type Props = {
  kinds: string[];
  kindFilter: string;
  changeKindFilter: (kind: string) => void;
  statusFilter: boolean;
  changeStatusFilter: (status: boolean) => void;
  labelFilter: string;
  setLabelFilter: (searchFilter: string) => void;
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
  kinds,
  kindFilter,
  changeKindFilter,
  statusFilter,
  changeStatusFilter,
  labelFilter,
  setLabelFilter
}) => {
  const [t] = useTranslation("plugins");

  const createKindFilterOptions = () => {
    const filterKinds: SelectItem[] = [];
    filterKinds.push({ label: t("scm-trace-monitor-plugin.tableActions.all"), value: "ALL" });
    kinds.forEach(kind => filterKinds.push({ label: kind, value: kind }));
    return filterKinds;
  };

  return (
    <TableActions className="columns column is-mobile-action-spacing">
      <div className="column">
        <Level className="is-word-break-none">
          <span className="mr-2">
            {t("scm-trace-monitor-plugin.tableActions.labelFilter")}
            <Help message={t("scm-trace-monitor-plugin.tableActions.labelFilterHelp")} />
          </span>
          <FilterInput value={labelFilter} filter={setLabelFilter} placeholder="" autoFocus />
        </Level>
      </div>
      <FlexNoneColumn className="column is-centered is-flex">
        <Level>
          <span className="mr-2">{t("scm-trace-monitor-plugin.tableActions.kindFilter")}</span>
          <Select value={kindFilter} options={createKindFilterOptions()} onChange={changeKindFilter} />
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
