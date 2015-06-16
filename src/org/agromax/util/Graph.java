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

package org.agromax.util;

import java.util.*;

/**
 * @author Joker, Hahahahahahaaaa!!
 */
public class Graph {
    public final Map<String, HashSet<String>> graph;

    public Graph() {
        graph = new HashMap<>();
    }

    public void addUndirectedEdge(String a, String b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        addDirectedEdge(a, b);
        addDirectedEdge(b, a);
    }

    private void addDirectedEdge(String from, String to) {
        if (graph.containsKey(from)) {
            graph.get(from).add(to);
        } else {
            HashSet<String> adjList = new HashSet<>();
            adjList.add(to);
            graph.put(from, adjList);
        }
    }

    public Set<String> relatedTerms(String term, boolean exactOnly) {
        Objects.requireNonNull(term);

        final Set<String> relatedTerms = new HashSet<>();

        if (graph.containsKey(term)) {
            // Found the exact match, well and good
            relatedTerms.addAll(graph.get(term));
        } else if (!exactOnly) {
            // Find all the related keys
            graph.forEach((k, v) -> {
                if (k.contains(term)) {
                    relatedTerms.addAll(v);
                }
            });
        }

        return relatedTerms;
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        graph.forEach((k, v) -> {
            out.append(k).append(" => ").append(v).append("\n");
        });
        return out.toString();
    }
/*
    public static void main(String[] args) {
        Graph g = new Graph();
        g.addUndirectedEdge("A", "B");
        g.addUndirectedEdge("C", "A");
        g.addUndirectedEdge("D", "B");
        System.out.println(g);

        System.out.println(g.relatedTerms("B", true));
    }
    */
}
