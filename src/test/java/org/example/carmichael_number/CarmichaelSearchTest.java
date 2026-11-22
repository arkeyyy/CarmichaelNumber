package org.example.carmichael_number;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class CarmichaelSearchTest {
    @Test
    void sampleMatchesAndTotals() {
        List<Integer> matches = IntStream.rangeClosed(1, 3000)
                .filter(CarmichaelSearch::isCarmichael).boxed().toList();

        assertEquals(List.of(561, 1105, 1729, 2465, 2821), matches);
        assertEquals(2995, 3000 - matches.size());
    }

    @Test
    void testsEveryFactorAndRequiresAComposite() {
        assertFalse(CarmichaelSearch.isCarmichael(1));
        assertFalse(CarmichaelSearch.isCarmichael(2));
        assertFalse(CarmichaelSearch.isCarmichael(15));
        assertFalse(CarmichaelSearch.isCarmichael(45));
        assertFalse(CarmichaelSearch.isCarmichael(46_337 * 46_337));
        assertFalse(CarmichaelSearch.isCarmichael(Integer.MAX_VALUE));
        assertTrue(CarmichaelSearch.isCarmichael(561));
        assertTrue(CarmichaelSearch.isCarmichael(41_041));  // Four distinct prime factors.
    }

    @Test
    void sampleRangesUseInclusiveBounds() {
        assertEquals(List.of(
                new CarmichaelSearch.Range(1, 301),
                new CarmichaelSearch.Range(302, 901),
                new CarmichaelSearch.Range(902, 1801),
                new CarmichaelSearch.Range(1802, 3000)
        ), CarmichaelSearch.partition(3000, 4));
    }

    @Test
    void rangesCoverEveryCandidateAndVaryInSizeWhenPossible() {
        for (int cnt = 1; cnt <= 64; cnt++) {
            for (int num = cnt; num <= cnt + 128; num++) {
                assertCoverage(num, cnt);
            }
            assertCoverage(Integer.MAX_VALUE, cnt);
        }
    }

    private void assertCoverage(int num, int cnt) {
        List<CarmichaelSearch.Range> ranges = CarmichaelSearch.partition(num, cnt);
        assertEquals(cnt, ranges.size());
        long next = 1;
        for (CarmichaelSearch.Range range : ranges) {
            assertEquals(next, range.start());
            assertTrue(range.size() > 0);
            next += range.size();
        }
        assertEquals((long) num + 1, next);
        if (cnt > 1 && num > cnt) {
            assertTrue(ranges.stream().map(CarmichaelSearch.Range::size).distinct().count() > 1);
        }
        if (num >= cnt * (cnt + 1) / 2) {
            assertEquals(cnt, ranges.stream().map(CarmichaelSearch.Range::size).distinct().count());
        }
    }
}
