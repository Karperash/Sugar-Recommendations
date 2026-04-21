#!/usr/bin/env python3
"""
Конвертер OhioT1DM (файл вида *-ws-training.xml / *-ws-testing.xml) -> CSV для импорта DiaGuide.

Официальный датасет: запросить у авторов (институциональная почта):
  https://webpages.charlotte.edu/rbunescu/data/ohiot1dm/OhioT1DM-dataset.html

После распаковки положите XML, например:
  data/ohio/raw/OhioT1DM-2-training/540-ws-training.xml

Пример:
  python scripts/ohio_xml_to_diaguide_csv.py data/ohio/raw/OhioT1DM-2-training/540-ws-training.xml -o out/540_diaguide.csv

Зависимости: только стандартная библиотека Python 3.9+ (zoneinfo).
"""

from __future__ import annotations

import argparse
import csv
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo


def _round_minute(dt: datetime, step: int = 5) -> datetime:
    m = (dt.minute // step) * step
    return dt.replace(minute=m, second=0, microsecond=0)


def _parse_ts(s: str) -> datetime:
    return datetime.strptime(s.strip(), "%d-%m-%Y %H:%M:%S")


def _extract_glucose(root: ET.Element) -> list[tuple[datetime, float]]:
    rows: list[tuple[datetime, float]] = []
    for ev in root.findall("glucose_level/event"):
        val = ev.get("value")
        ts = ev.get("ts")
        if not val or not ts:
            continue
        try:
            g = float(val)
        except ValueError:
            continue
        rows.append((_round_minute(_parse_ts(ts)), g))
    rows.sort(key=lambda x: x[0])
    return rows


def _extract_meal_buckets(root: ET.Element) -> set[datetime]:
    buckets: set[datetime] = set()
    for ev in root.findall("meal/event"):
        ts = ev.get("ts")
        if not ts:
            continue
        buckets.add(_round_minute(_parse_ts(ts)))
    return buckets


def convert(
    xml_path: Path,
    out_path: Path,
    zone: str = "America/New_York",
) -> int:
    root = ET.parse(xml_path).getroot()
    meals = _extract_meal_buckets(root)
    glucose = _extract_glucose(root)
    tz = ZoneInfo(zone)
    out_path.parent.mkdir(parents=True, exist_ok=True)

    with out_path.open("w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
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
        for ts_local, g in glucose:
            local = ts_local.replace(tzinfo=tz)
            instant = local.astimezone(ZoneInfo("UTC"))
            ts_iso = instant.strftime("%Y-%m-%dT%H:%M:%SZ")
            meal = "true" if ts_local in meals else "false"
            w.writerow(
                [
                    ts_iso,
                    f"{g:.1f}",
                    "MG_DL",
                    "UNKNOWN",
                    meal,
                    "false",
                    "false",
                ]
            )
    return len(glucose)


def main() -> None:
    p = argparse.ArgumentParser(description="OhioT1DM XML to DiaGuide CGM CSV")
    p.add_argument("xml", type=Path, help="Путь к *-ws-*.xml")
    p.add_argument(
        "-o",
        "--output",
        type=Path,
        required=True,
        help="Выходной .csv",
    )
    p.add_argument(
        "--zone",
        default="America/New_York",
        help="Часовой пояс меток времени в XML (по умолчанию America/New_York)",
    )
    args = p.parse_args()
    n = convert(args.xml, args.output, zone=args.zone)
    print(f"Записей CGM: {n} -> {args.output}")


if __name__ == "__main__":
    main()
