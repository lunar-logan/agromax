/*
 * Copyright 2015 Anurag Gautam
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.agromax.platform.server;

import org.agromax.core.SPOGenerator;
import org.agromax.core.Triple;
import org.agromax.core.nlp.pipeline.ComparableWord;
import org.agromax.core.nlp.pipeline.SPPipeline;
import org.agromax.util.FileUtil;
import org.agromax.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Anurag Gautam
 */
public final class CmdHost implements Runnable {

    private final SPPipeline pipeline;

    public CmdHost(SPPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String s = br.readLine();
                if (s == null || !handle(s)) {
                    System.out.println("Exiting... Bye!");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean handle(String s) {
        if (s.equalsIgnoreCase("quit") || s.equalsIgnoreCase("exit") || s.equalsIgnoreCase("q"))
            return false;
        String[] elements = s.replaceAll(" +", " ").split(" ");
        if (elements[0].equalsIgnoreCase("tag")) {
            handleTag(elements);
        } else if (elements[0].equalsIgnoreCase("graph")) {
            handleGraph(elements);
        } else if (elements[0].equalsIgnoreCase("spo")) {
            handleSPO(elements);
        } else {
            System.err.println("Unknown command \"" + elements[0] + "\"");
        }

        return true;
    }

    private void handleSPO(String[] elements) {
        String testFile = elements[1];
        Path path = Util.dirPath("data", testFile);
        String test = FileUtil.read(path);
        List<List<Triple<String, String, String>>> triples = SPOGenerator.generate(pipeline, test);
        triples.forEach(t -> t.forEach(System.out::println));
//        triples.forEach(System.out::println);
    }

    private void handleGraph(String[] elements) {
        String testFile = elements[1];
        Path path = Util.dirPath("data", testFile);
        String test = FileUtil.read(path);
        List<TreeMap<ComparableWord, TreeSet<ComparableWord>>> relationshipGraph = SPOGenerator.getRelationshipGraph(pipeline, test);
        relationshipGraph.forEach(e -> {
            e.forEach((k, v) -> System.out.println(k + " => " + v));
        });
    }

    private void handleTag(String[] elements) {
        String testFile = elements[1];
        Path path = Util.dirPath("data", testFile);
        String test = FileUtil.read(path);
        System.out.println("Not yet implemented");
    }
}
