# /// script
# dependencies = ["airium"]
# ///

"""
RUNNING INSTRUCTIONS:
-------------------
uv run BenchmarkAirium.py
"""

import time
from airium import Airium
from typing import Any

def run_airium(row_count: int, col_count: int) -> str:
    """Generates a large HTML table using Airium."""
    a = Airium(source_minify=True)

    # Compressed format is achieved by not adding indentation
    with a.html():
        with a.body():
            with a.table():
                for i in range(row_count):
                    with a.tr():
                        for j in range(col_count):
                            # In Airium, _t is used for text content
                            with a.td():
                                a._t(f"Data {i}:{j}")

    result: str = str(a)
    return result

def format_number(number: int) -> str:
    """Formats number with space separator."""
    return f"{number:,}".replace(",", " ")

def main() -> None:
    run_benchmark: bool = True
    rows: int = 100_000 if run_benchmark else 1
    cols: int = 50

    print(f"Starting benchmark for {format_number(rows)} rows...")

    # 1. WARM-UP PHASE (Small sample to jit/cache)
    run_airium(10, cols)

    # 2. Airium Benchmark
    start_time: float = time.perf_counter()
    airium_html: str = run_airium(rows, cols)
    end_time: float = time.perf_counter()

    duration_ms: int = int((end_time - start_time) * 1000)
    html_length: int = len(airium_html)

    print(f"Airium Benchmark: {format_number(duration_ms)} ms "
          f"(Length: {format_number(html_length)})")

if __name__ == "__main__":
    main()