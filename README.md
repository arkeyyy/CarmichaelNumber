# Carmichael Number

A JavaFX desktop application that finds Carmichael numbers from 1 through an inclusive upper limit. The default search covers 10,000 integers with four worker threads.

The search uses Korselt's criterion. A composite integer `n` qualifies if each prime factor occurs exactly once and every prime factor `p` satisfies `(n - 1) % (p - 1) == 0`. For example, 561 has prime factors 3, 11, and 17. Subtracting one from each gives 2, 10, and 16, all of which divide 560.

## Search display

Each worker checks a separate contiguous range and reports its progress. Once every worker finishes, the results area lists the matches in ascending order and the counters show the final totals. Stopping a search discards partial results and resets the totals to zero.

With an upper limit of 3,000 and four workers, the matches are **561, 1105, 1729, 2465, and 2821**. The totals are 5 Carmichael numbers and 2,995 non-Carmichael numbers out of 3,000 checked.

## Project

The application uses JavaFX 21, FXML, and Maven. The Maven wrapper is included for Windows, macOS, and Linux.
