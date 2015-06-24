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
import org.agromax.util.Util;
import org.agromax.util.WordUtil;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Anurag Gautam
 */
public class TripleGenerator {

    public static class Triple<P, Q, R> {
        public final P first;
        public final Q second;
        public final R third;

        public Triple(P first, Q second, R third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        @Override
        public String toString() {
            return String.format("(%s, %s, %s)", String.valueOf(first), String.valueOf(second), String.valueOf(third));
        }
    }

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
            TreeMap<Word, TreeSet<Word>> relations = new TreeMap<>();
            for (TypedDependency d : gs.typedDependencies()) {
                Word gov = new Word(d.gov());
                Word dep = new Word(d.dep());

                if (relations.containsKey(gov)) {
                    relations.get(gov).add(dep);
                } else {
                    TreeSet<Word> set = new TreeSet<>();
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

    private static List<Triple<String, String, String>> process(GrammaticalStructure gs, TreeMap<Word, TreeSet<Word>> relationMap) {
        final Queue<Word> queue = new LinkedList<>();
        final TreeSet<Word> visited = new TreeSet<>();
        final List<Triple<String, String, String>> triples = new LinkedList<>();

        for (Word u : relationMap.keySet()) {
            TreeSet<Word> subjectPhrase = new TreeSet<>();
            TreeSet<Word> objectPhrase = new TreeSet<>();

            relationMap.get(u).stream().forEach(v -> {
                queue.clear();
                visited.clear();

                // Get the subject
                if (v.getIndex() < u.getIndex()) {
                    queue.add(v);
                    while (!queue.isEmpty()) {
                        Word w = queue.poll();
                        subjectPhrase.add(w);
                        if (!visited.contains(w)) {
                            if (relationMap.containsKey(w)) {
                                for (Word x : relationMap.get(w)) {
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
                        Word w = queue.poll();
                        objectPhrase.add(w);
                        if (!visited.contains(w)) {
                            if (relationMap.containsKey(w)) {
                                for (Word x : relationMap.get(w)) {
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
                Stream<Word> subStream = subjectPhrase.stream().map(w -> {
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
