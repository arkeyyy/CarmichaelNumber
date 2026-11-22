package org.example.carmichael_number;

import java.util.ArrayList;
import java.util.List;

final class CarmichaelSearch {
    private static final int[] ODD_PRIMES = oddPrimesUpTo(46_340);

    private CarmichaelSearch() {}

    static boolean isCarmichael(int n) {
        // An even composite is divisible by 4 or has an odd prime factor whose p - 1 is even.
        if (n < 3 || n % 2 == 0) {
            return false;
        }

        int remaining = n;
        for (int prime : ODD_PRIMES) {
            if (prime * prime > remaining) {
                break;
            }
            if (remaining % prime == 0) {
                remaining /= prime;
                if (remaining % prime == 0 || (n - 1) % (prime - 1) != 0) {
                    return false;
                }
            }
        }
        // After trial division the remaining factor is prime. A prime input was never reduced.
        return remaining != n && (n - 1) % (remaining - 1) == 0;
    }

    private static int[] oddPrimesUpTo(int limit) {
        boolean[] composite = new boolean[limit + 1];
        List<Integer> primes = new ArrayList<>();
        for (int candidate = 3; candidate <= limit; candidate += 2) {
            if (!composite[candidate]) {
                primes.add(candidate);
                for (int multiple = candidate * candidate; multiple <= limit; multiple += 2 * candidate) {
                    composite[multiple] = true;
                }
            }
        }
        return primes.stream().mapToInt(Integer::intValue).toArray();
    }

    record Range(int start, int end) {
        int size() {
            return end - start + 1;
        }
    }

    static List<Range> partition(int num, int cnt) {
        if (num < 1 || cnt < 1 || cnt > num || cnt > 64) {
            throw new IllegalArgumentException("Invalid upper limit or thread count.");
        }

        // The smallest d distinct sizes require cnt single items plus 0 + 1 + ... + (d - 1).
        int distinct = cnt;
        while (triangular(distinct - 1) > num - cnt) {
            distinct--;
        }

        List<Range> ranges = new ArrayList<>(cnt);
        long start = 1;
        int singletonPrefix = cnt - distinct;
        long previousSize = 0;
        for (int index = 0; index < cnt; index++) {
            long size = 1;
            if (index >= singletonPrefix) {
                int slots = cnt - index;
                long available = num - start + 1;
                // Later sizes increase by at least one, even when a weighted cut needs rounding.
                long maximum = (available - triangular(slots - 1)) / slots;
                long minimum = index == singletonPrefix ? 1 : previousSize + 1;
                long weightedEnd = 1 + num * triangular(index + 1) / triangular(cnt);
                size = Math.max(minimum, Math.min(maximum, weightedEnd - start + 1));
            }
            long end = start + size - 1;
            ranges.add(new Range((int) start, (int) end));
            start = end + 1;
            previousSize = size;
        }
        return ranges;
    }

    private static long triangular(int value) {
        return (long) value * (value + 1) / 2;
    }
}
