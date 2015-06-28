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

package org.agromax.core;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import org.agromax.core.nlp.pipeline.ComparableWord;
import org.agromax.util.Util;
import org.agromax.util.WordUtil;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Anurag Gautam
 */
public class TripleGenerator {

    private static String lastSubjectPhrase = "";

    public static List<Triple<String, String, String>> getTriples(String text) {
        MaxentTagger tagger = new MaxentTagger(Util.SP_TAGGER_PATH);
        DependencyParser parser = DependencyParser.loadFromModelFile(Util.SP_MODEL_PATH);
        List<Triple<String, String, String>> allTriples = new LinkedList<>();
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));

        for (List<HasWord> sentence : tokenizer) {
            List<TaggedWord> tagged = tagger.tagSentence(sentence);
            GrammaticalStructure gs = parser.predict(tagged);

//            System.out.println(gs);
            TreeMap<ComparableWord, TreeSet<ComparableWord>> relations = new TreeMap<>();
            for (TypedDependency d : gs.typedDependencies()) {
                ComparableWord gov = new ComparableWord(d.gov());
                ComparableWord dep = new ComparableWord(d.dep());

                if (relations.containsKey(gov)) {
                    relations.get(gov).add(dep);
                } else {
                    TreeSet<ComparableWord> set = new TreeSet<>();
                    set.add(dep);
                    relations.put(gov, set);
                }
            }
            Util.log("Relationship graph has been generated");
            System.out.println("==== Relationship Graph ====");
            relations.forEach((k, v) -> {
                System.out.println(String.format("%s ==> %s", k.toString(), v.toString()));
            });
            System.out.println("====\n");

            Util.log("Beginning to process the graph");
            List<Triple<String, String, String>> triples = process(gs, relations);
            Util.log("graph processing is complete");

            Util.log("Generated ", triples.size(), " triple(s)");
            System.out.println(triples);
            allTriples.addAll(triples);
        }


        return allTriples;
    }

    private static List<Triple<String, String, String>> process(GrammaticalStructure gs, TreeMap<ComparableWord, TreeSet<ComparableWord>> relationMap) {
        final Queue<ComparableWord> queue = new LinkedList<>();
        final TreeSet<ComparableWord> visited = new TreeSet<>();
        final List<Triple<String, String, String>> triples = new LinkedList<>();

        for (ComparableWord u : relationMap.keySet()) {
            TreeSet<ComparableWord> subjectPhrase = new TreeSet<>();
            TreeSet<ComparableWord> objectPhrase = new TreeSet<>();

            relationMap.get(u).stream().forEach(v -> {
                queue.clear();
                visited.clear();

                // Get the subject
                if (v.getIndex() < u.getIndex()) {
                    queue.add(v);
                    while (!queue.isEmpty()) {
                        ComparableWord w = queue.poll();
                        subjectPhrase.add(w);
                        if (!visited.contains(w)) {
                            if (relationMap.containsKey(w)) {
                                for (ComparableWord x : relationMap.get(w)) {
                                    subjectPhrase.add(x);
                                    queue.add(x);
                                }
                            }
                            visited.add(w);
                        }
                    }

                } else {

                    // Get the object
                    queue.add(v);
                    while (!queue.isEmpty()) {
                        ComparableWord w = queue.poll();
                        objectPhrase.add(w);
                        if (!visited.contains(w)) {
                            if (relationMap.containsKey(w)) {
                                for (ComparableWord x : relationMap.get(w)) {
                                    objectPhrase.add(x);
                                    queue.add(x);
                                }
                            }
                            visited.add(w);
                        }
                    }
                }
            });

            if (subjectPhrase.size() > 0 && objectPhrase.size() > 0) {

                // Co-reference resolution phase
                // Replace pronouns with previous sentence's subject
                Stream<ComparableWord> subStream = subjectPhrase.stream().map(w -> {
                    if (w.getTag().equalsIgnoreCase("PRP") && w.getWord().equalsIgnoreCase("it") && !lastSubjectPhrase.isEmpty()) {
                        w.setWord(lastSubjectPhrase);
                    }
                    return w;
                });

                // Identify NNP word from subjectPhrase, will be used for coreference resolution in the next line
                // of text
                subjectPhrase.forEach(w -> {
                    if (w.getTag().startsWith("NNP")) {
                        lastSubjectPhrase = w.getWord();
                    }
                });

                String subject = WordUtil.join(subStream, " ", w -> {
                    if (!Stopwords.STOPWORDS.contains(w.toLowerCase()))
                        return String.format("<b>%s</b>", w);
                    return w;
                });
                String object = WordUtil.join(objectPhrase.stream(), " ", w -> {
                    if (!Stopwords.STOPWORDS.contains(w.toLowerCase()))
                        return String.format("<b>%s</b>", w);
                    return w;
                });
                String predicate = u.getWord();

                triples.add(new Triple<>(subject, predicate, object));
            }

        }
        return triples;
    }

    public static String publish(List<Triple<String, String, String>> triples) {
        StringBuilder markup = new StringBuilder();
        markup.append("<!doctype HTML>")
                .append("<html>")
                .append("<head>")
                .append("<link rel=\"stylesheet\"").append(" ").append("href=\"style.css\">")
                .append("</head>")
                .append("<body>")
                .append("<table>")
                .append("<thead>")
                .append("<th>").append("Subject").append("</th>")
                .append("<th>").append("Predicate").append("</th>")
                .append("<th>").append("Object").append("</th>")
                .append("</thead>");
        triples.stream().forEach(t -> {
            markup.append("<tr>")
                    .append("<td>").append(t.first).append("</td>")
                    .append("<td>").append(t.second).append("</td>")
                    .append("<td>").append(t.third).append("</td>")
                    .append("</tr>");
        });
        markup.append("</table>")
                .append("</body>")
                .append("</html>");
        return markup.toString();
    }

}
