# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 3.1.2 - 2025-04-11
### Added
- Descriptive document titles

## 3.1.1 - 2025-01-17
### Fixed
- Apply fix from 1.2.2 (Handle store exceptions to prevent other processes from failing)

## 1.2.2 - 2025-01-17
### Fixed
- Handle store exceptions to prevent other processes from failing

## 3.1.0 - 2024-12-05
### Changed
- Redirect to first page when filtering for labels, category or whether a request has failed or not

## 3.0.0 - 2024-09-11
### Changed
- Changeover to AGPLv3 license

## 1.2.1 - 2024-02-08
### Fixed
- Link from the landing page to the trace monitor overview

## 1.2.0 - 2023-11-15
### Fixed
- Max amount of stored requests per category configuration

### Changed
- Improve ui/ux for trace monitor actions

## 1.1.0 - 2023-04-12
### Added
- Paging for trace monitor table

## 1.0.6 - 2022-02-18
### Fixed
- Correct behavior of table and actions on mobile pages ([#9](https://github.com/scm-manager/scm-trace-monitor-plugin/pull/9))

## 1.0.5 - 2021-11-04
### Fixed
- Replace <a> tag by <button> ([#8](https://github.com/scm-manager/scm-trace-monitor-plugin/pull/8))

## 1.0.4 - 2021-10-21
### Changed
- Styling to match landing-page-plugin update ([#5](https://github.com/scm-manager/scm-trace-monitor-plugin/pull/5))

## 1.0.3 - 2021-04-28
### Fixed
- Set default limit for entries back to 50

## 1.0.2 - 2021-03-26
### Fixed
- Limit spans after filters to prevent missing entries on failed requests ([#3](https://github.com/scm-manager/scm-trace-monitor-plugin/pull/3))

## 1.0.1 - 2020-12-18
### Fixed
- Synchronize store access to prevent concurrent edit errors ([#2](https://github.com/scm-manager/scm-trace-monitor-plugin/pull/2))

## 1.0.0 - 2020-11-06
### Added
- Add trace monitor and configuration ([#1](https://github.com/scm-manager/scm-trace-monitor-plugin/pull/1))

