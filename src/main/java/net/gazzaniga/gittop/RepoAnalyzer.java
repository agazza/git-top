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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RepoAnalyzer {

    /**
     * Run git log --stat and return the output as InputStream
     *
     * @param path the local path of the repo to analyze
     * @return the command output
     */
    static InputStreamReader execGitLogStat(String path) throws IOException {

        ProcessBuilder pb = new ProcessBuilder("git", "log", "--stat");
        if ("".equals(path)) {
            path=System.getProperty("user.dir");
        }
        pb.directory(new File(path));
        Process gitProc = pb.start();
        return new InputStreamReader(gitProc.getInputStream());
    }

    /**
     *
     * @param path the local path of the repo to analyze
     * @return list of log lines with the changed files
     */
    static List<String> getLogStatFromGit(String path) throws IOException {
        return new BufferedReader(execGitLogStat(path)).lines()
                .filter(line -> line.contains("|"))
                .collect(Collectors.toList());
    }

    /**
     * @param lines a raw list of changed files, as printed by git log
     * @return a map containing the number of times a file has changed and the sum of
     * changed lines per file, ordered by number of time, decrescent.
     */
    static LinkedHashMap<String,Integer[]> processGitStats(List<String> lines) {
        Comparator<Integer[]> revIntArrayComparator = Comparator.comparingInt(a -> -a[0]);
        Comparator<Map.Entry<String, Integer[]>> cmp = Map.Entry.comparingByValue(revIntArrayComparator);
        Map<String,Integer[]> ret = new HashMap<>();

        Pattern pattern = Pattern.compile("^\\s*(\\S+)\\s*\\|\\s+(\\d+)\\s*(.*)$");
        for(String line:lines) {
            Matcher matcher = pattern.matcher(line);

            if(matcher.matches()) {
                String name = matcher.group(1);
                String changes = matcher.group(2);

                mapMerge(ret, name, new Integer[] {1, stringToInt(changes)});
            }
        }
        return ret.entrySet().stream().sorted(cmp).limit(50).collect(
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Convert number of changes to int, ignoring the false ones.
     *
     * @param param the string to convert
     * @return int value or 0 in case of error
     */
    static int stringToInt(String param) {
        try {
            return Integer.parseInt(param);
        } catch(Exception e) {
            return 0;
        }
    }

    static void mapMerge(Map<String,Integer[]> map, String key, Integer[] value) {
        map.merge(key, value, (a, b) -> {
            a[0] += b[0];
            a[1] += b[1];
            return a;
        });
    }
}

