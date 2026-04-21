#!/usr/bin/env python3
"""
Конвертер глюкозы T1D-UOM (Zenodo / UoMGlucose*.csv) -> CSV для импорта DiaGuide.

Источник данных (CC BY 4.0): Longitudinal Multimodal T1D, University of Manchester.
Карточка и загрузка: https://doi.org/10.5281/zenodo.15169263
Репозиторий с описанием колонок: https://github.com/sharpic/ManchesterCSCoordinatedDiabetesStudy

Входной CSV ожидается в подпапке Dataset/Glucose Data/ с колонками:
  bg_ts  — дата/время (MM/DD/YYYY HH:MM:SS)
  value  — глюкоза в ммоль/л

Пример:
  python scripts/t1d_uom_glucose_to_diaguide_csv.py ^
    data/zenodo/raw/Dataset/Glucose Data/UoMGlucose01.csv -o out/uom01_diaguide.csv

Зависимости: Python 3.9+ (stdlib: csv, zoneinfo).
"""

from __future__ import annotations

import argparse
import csv
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo


def _parse_bg_ts(text: str) -> datetime:
    t = text.strip().strip('"')
    for fmt in ("%m/%d/%Y %H:%M:%S", "%m/%d/%Y %H:%M"):
        try:
            return datetime.strptime(t, fmt)
        except ValueError:
            continue
    raise ValueError(f"Unrecognized bg_ts: {text!r}")


def convert(src: Path, dst: Path, zone: str = "Europe/London") -> int:
    tz = ZoneInfo(zone)
    utc = ZoneInfo("UTC")
    dst.parent.mkdir(parents=True, exist_ok=True)
    n = 0
    with src.open(newline="", encoding="utf-8-sig") as fin, dst.open("w", newline="", encoding="utf-8") as fout:
        reader = csv.DictReader(fin)
        if not reader.fieldnames or "bg_ts" not in reader.fieldnames or "value" not in reader.fieldnames:
            raise SystemExit(
                "Expected columns bg_ts, value. Found: " + ",".join(reader.fieldnames or []),
            )
        w = csv.writer(fout)
        w.writerow(
            [
                "timestamp",
                "glucoseValue",
                "unit",
                "trendDirection",
                "meal",
                "insulin",
                "activity",
            ]
        )
        for row in reader:
            raw_ts = row.get("bg_ts") or ""
            raw_val = row.get("value") or ""
            if not raw_ts.strip():
                continue
            try:
                v = float(raw_val)
            except ValueError:
                continue
            local = _parse_bg_ts(raw_ts).replace(tzinfo=tz)
            inst = local.astimezone(utc).strftime("%Y-%m-%dT%H:%M:%SZ")
            w.writerow([inst, f"{v:.3f}", "MMOL_L", "UNKNOWN", "false", "false", "false"])
            n += 1
    return n


def main() -> None:
    p = argparse.ArgumentParser(description="T1D-UOM UoMGlucose CSV to DiaGuide CGM CSV")
    p.add_argument("csv", type=Path, help="UoMGlucoseID.csv from Zenodo zip")
    p.add_argument("-o", "--output", type=Path, required=True, help="Output .csv")
    p.add_argument(
        "--zone",
        default="Europe/London",
        help="Timezone for bg_ts if values are local (default Europe/London)",
    )
    args = p.parse_args()
    n = convert(args.csv, args.output, zone=args.zone)
    print(f"Rows: {n} -> {args.output}")


if __name__ == "__main__":
    main()
