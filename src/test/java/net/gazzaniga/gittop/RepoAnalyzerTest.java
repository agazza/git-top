/*
 * Copyright (c) 2020 Andrea Gazzaniga.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.gazzaniga.gittop;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

public class RepoAnalyzerTest {

    private String path;

    RepoAnalyzerTest(){
        path = System.getProperty("gittop.test.runpath", "");
    }

    @Test
    void testGetLogStatFromGit() {
        List<String> lines = null;
        try {
            lines = RepoAnalyzer.getLogStatFromGit(path);
        } catch (IOException e) {
            Assertions.fail("Exception", e);
        }
        Assertions.assertNotNull(lines);
        Assertions.assertTrue(lines.stream().allMatch(line -> line.contains("|")));
    }

    @Test
    void testMapMerge() {
        Map<String,Integer[]> map = new HashMap<>();

        RepoAnalyzer.mapMerge(map, "key", new Integer[] {1,1});
        Assertions.assertArrayEquals(map.get("key"), new Integer[] {1,1});

        RepoAnalyzer.mapMerge(map, "key", new Integer[] {1,10});
        Assertions.assertArrayEquals(map.get("key"), new Integer[] {2,11});

        RepoAnalyzer.mapMerge(map, "key", new Integer[] {2,2});
        Assertions.assertArrayEquals(map.get("key"), new Integer[] {4,13});

        RepoAnalyzer.mapMerge(map, "key", new Integer[] {-2,-2});
        Assertions.assertArrayEquals(map.get("key"), new Integer[] {2,11});

        RepoAnalyzer.mapMerge(map, "key", new Integer[] {0,0});
        Assertions.assertArrayEquals(map.get("key"), new Integer[] {2,11});

    }

    @Test
    void testProcessGitStats() {
        try {
            LinkedHashMap<String, Integer[]> stats = RepoAnalyzer.processGitStats(
                    RepoAnalyzer.getLogStatFromGit(path));
            Assertions.assertTrue(stats.entrySet().iterator().hasNext());
            Integer[] first = stats.entrySet().iterator().next().getValue();
            Integer[] max = maxValue(stats);
            Assertions.assertArrayEquals(first, max);
        } catch (Exception e) {
            Assertions.fail("Exception", e);
        }
    }

    private Integer[] maxValue(Map<String, Integer[]> map) {
        Map.Entry<String, Integer[]> maxEntry = Collections.max(map.entrySet(),
                Comparator.comparingInt((Map.Entry<String, Integer[]> e) -> e.getValue()[0]));
        return maxEntry.getValue();
    }
}
