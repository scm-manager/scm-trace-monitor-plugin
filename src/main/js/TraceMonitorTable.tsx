import React, { FC } from "react";
import { Column, comparators, Icon, Table, TextColumn } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Span } from "./TraceMonitor";
import { format } from "date-fns";

type Props = {
  spans: Span[];
  changeCategoryFilter: (category: string) => void;
  changeStatusFilter: (status: boolean) => void;
};

const TraceMonitorTable: FC<Props> = ({ spans }) => {
  const [t] = useTranslation("plugins");

  const convertMillisToString = (ms: number) => {
    const showWith0 = (value: number) => (value < 10 ? `0${value}` : value);
    const minutes = showWith0(Math.floor((ms / (1000 * 60)) % 60));
    const seconds = showWith0(Math.floor((ms / 1000) % 60));
    const millis = showWith0(Math.floor(ms % 1000));
    return `${minutes} m ${seconds} s ${millis} ms`;
  };

  const openModal = (span: Span) => {};

  return (
    <>
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
                <Icon color="success" name="check-circle" />
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
          {row => format(new Date(row.closed), "yyyy-MM-dd HH:mm:ss")}
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
          {row => <a onClick={() => openModal(row)}>{t("scm-trace-monitor-plugin.table.details")}</a>}
        </Column>
      </Table>
    </>
  );
};

export default TraceMonitorTable;
