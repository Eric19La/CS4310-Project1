# CS4310-Project1

CPU job scheduling simulator implementing FCFS, SJF, Round-Robin (quantum=2), and Round-Robin (quantum=5), as four standalone Java programs.

**The project submission document is `CS4310-Project1-Report-EricLa.docx`** — the official assignment template with all Part 1/2/3 answers, tables, screenshots, and graphs filled in.

## Build & Run

```
javac -d out src/*.java
java -cp out FCFS job.txt
java -cp out SJF job.txt
java -cp out RR2 job.txt
java -cp out RR5 job.txt
```

Pass a different input file path as an argument to run against another test case, e.g.:

```
java -cp out FCFS testdata/job10.txt
```

## Input Format

`job.txt` alternates job name and burst time (ms) on separate lines, e.g.:

```
Job1
7
Job2
18
```

All jobs are assumed to arrive at time 0; the file may contain up to 30 jobs.

## Project Layout

- `src/` — the four submitted program files: `FCFS.java`, `SJF.java`, `RR2.java`, `RR5.java`
- `job.txt`, `testdata/job5.txt`, `testdata/job10.txt`, `testdata/job15.txt` — the designed test cases used in Part 1(b)/Part 2
- `analysis/experiment.py` — generates 20 random trials each for 5/10/15-job inputs (burst times 1–20ms), runs all four compiled programs, and produces `analysis/results/summary_table.csv` and `analysis/results/graphs/*.png` for Part 3
- `analysis/fill_report.py` — fills the assignment's `.docx` template with the design/testing/implementation/performance-analysis answers and graphs, producing `CS4310-Project1-Report-EricLa.docx`

### Regenerating the analysis

```
javac -d out src/*.java
python3 analysis/experiment.py      # re-runs the 20-trial experiment, regenerates graphs
python3 analysis/fill_report.py     # re-fills the docx from the latest results
```
