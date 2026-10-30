#!/usr/bin/env python3
"""
Part 3 performance-analysis experiment runner.

Not one of the four submitted program files -- this is a helper script used
only to generate the random test data, drive the four compiled Java programs
(FCFS, SJF, RR2, RR5) across 20 trials each for input sizes 5, 10, and 15
jobs, and produce the summary table + graphs required by Part 3 of the
project document.

Usage (run from the project root):
    javac -d out src/*.java
    python3 analysis/experiment.py
"""
import csv
import random
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = ROOT / "out"
RESULTS_DIR = ROOT / "analysis" / "results"
TRIALS = 20
SIZES = [5, 10, 15]
MAX_BURST = 20
ALGORITHMS = ["FCFS", "SJF", "RR2", "RR5"]
AVG_PATTERN = re.compile(r"Average Turnaround Time:\s*([0-9.]+)")


def generate_job_file(path: Path, num_jobs: int, rng: random.Random) -> None:
    lines = []
    for i in range(1, num_jobs + 1):
        lines.append(f"Job{i}")
        lines.append(str(rng.randint(1, MAX_BURST)))
    path.write_text("\n".join(lines) + "\n")


def run_algorithm(algorithm: str, job_file: Path) -> float:
    result = subprocess.run(
        ["java", "-cp", str(OUT_DIR), algorithm, str(job_file)],
        capture_output=True, text=True, check=True,
    )
    match = AVG_PATTERN.search(result.stdout)
    if not match:
        raise RuntimeError(f"Could not parse average turnaround from {algorithm} output:\n{result.stdout}")
    return float(match.group(1))


def main() -> None:
    if not OUT_DIR.exists():
        print("out/ not found -- compile first: javac -d out src/*.java", file=sys.stderr)
        sys.exit(1)

    RESULTS_DIR.mkdir(parents=True, exist_ok=True)
    rng = random.Random(42)  # fixed seed for reproducibility

    # raw[size][algorithm] = list of 20 per-trial average turnaround times
    raw = {size: {alg: [] for alg in ALGORITHMS} for size in SIZES}

    trial_job_dir = RESULTS_DIR / "trial_inputs"
    trial_job_dir.mkdir(exist_ok=True)

    for size in SIZES:
        for trial in range(1, TRIALS + 1):
            job_file = trial_job_dir / f"job_{size}_{trial:02d}.txt"
            generate_job_file(job_file, size, rng)
            for alg in ALGORITHMS:
                raw[size][alg].append(run_algorithm(alg, job_file))
        print(f"Completed {TRIALS} trials for {size}-job input size.")

    # Part 3(a) summary table: average of the 20 trial averages, per algorithm/size
    summary_path = RESULTS_DIR / "summary_table.csv"
    with summary_path.open("w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["Input Size (jobs)"] + ALGORITHMS)
        for size in SIZES:
            row = [size] + [round(sum(raw[size][alg]) / TRIALS, 2) for alg in ALGORITHMS]
            writer.writerow(row)

    # raw per-trial data, for transparency / re-derivation
    raw_path = RESULTS_DIR / "raw_trials.csv"
    with raw_path.open("w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["Input Size", "Trial", "Algorithm", "Average Turnaround Time"])
        for size in SIZES:
            for alg in ALGORITHMS:
                for trial_idx, value in enumerate(raw[size][alg], start=1):
                    writer.writerow([size, trial_idx, alg, value])

    print(f"\nWrote {summary_path}")
    print(f"Wrote {raw_path}")

    print("\n=== Part 3(a) Summary Table ===")
    print(f"{'Jobs':<6}" + "".join(f"{alg:<10}" for alg in ALGORITHMS))
    for size in SIZES:
        row = [round(sum(raw[size][alg]) / TRIALS, 2) for alg in ALGORITHMS]
        print(f"{size:<6}" + "".join(f"{v:<10}" for v in row))

    make_graphs(raw)


def make_graphs(raw) -> None:
    try:
        import matplotlib
        matplotlib.use("Agg")
        import matplotlib.pyplot as plt
    except ImportError:
        print("\nmatplotlib not installed -- skipping graph generation.", file=sys.stderr)
        print("Install with: pip3 install matplotlib", file=sys.stderr)
        return

    graphs_dir = RESULTS_DIR / "graphs"
    graphs_dir.mkdir(exist_ok=True)

    averages = {
        alg: [sum(raw[size][alg]) / TRIALS for size in SIZES]
        for alg in ALGORITHMS
    }

    # One graph per algorithm
    for alg in ALGORITHMS:
        plt.figure(figsize=(5, 4))
        plt.plot(SIZES, averages[alg], marker="o")
        plt.title(f"{alg}: Average Turnaround Time vs Input Size")
        plt.xlabel("Number of Jobs")
        plt.ylabel("Average Turnaround Time (ms)")
        plt.xticks(SIZES)
        plt.grid(True, linestyle="--", alpha=0.5)
        plt.tight_layout()
        plt.savefig(graphs_dir / f"{alg}.png", dpi=150)
        plt.close()

    # Combined graph
    plt.figure(figsize=(6, 5))
    for alg in ALGORITHMS:
        plt.plot(SIZES, averages[alg], marker="o", label=alg)
    plt.title("Average Turnaround Time vs Input Size (All Algorithms)")
    plt.xlabel("Number of Jobs")
    plt.ylabel("Average Turnaround Time (ms)")
    plt.xticks(SIZES)
    plt.legend()
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.tight_layout()
    plt.savefig(graphs_dir / "combined.png", dpi=150)
    plt.close()

    print(f"\nWrote graphs to {graphs_dir}")


if __name__ == "__main__":
    main()
