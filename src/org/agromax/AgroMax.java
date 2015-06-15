package org.agromax;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Bruce Wayne
 */
public class AgroMax {
    public static final class Word implements Comparable<Word> {
        private final String word;
        private final String posTag;
        private final int index;

        public Word(String str, int index) {
            int slash = str.indexOf('/');
            if (slash >= 0) {
                word = str.substring(0, slash);
                posTag = str.substring(slash + 1);
                this.index = index;
            } else {
                word = str;
                posTag = "";
                this.index = 0;
            }
        }

        public String getWord() {
            return word;
        }

        public String getTag() {
            return posTag;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public int compareTo(Word o) {
            return index < o.index ? -1 : (index > o.index ? 1 : 0);
        }

        @Override
        public String toString() {
            return String.format("%s/%s/%d", word, posTag, index);
        }
    }

    public static void getTriples(String text) {
        MaxentTagger tagger = new MaxentTagger(Util.SP_TAGGER_PATH);
        DependencyParser parser = DependencyParser.loadFromModelFile(Util.SP_MODEL_PATH);

        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
        for (List<HasWord> sentence : tokenizer) {
            List<TaggedWord> tagged = tagger.tagSentence(sentence);
            GrammaticalStructure gs = parser.predict(tagged);

            // Print typed dependencies
            triples(gs, (ArrayList<TaggedWord>) tagged);

            // For debugging purpose
            /*System.out.println("------------------------------------");
            gs.allTypedDependencies().forEach(e -> {
                System.out.println(e.reln() + "[ " + e.gov() + "(" + e.gov().index() + ") ---> " + e.dep() + "(" + e.dep().index() + ") ]");
            });
            System.out.println("-------------------------------------");*/
        }
    }

    private static void triples(GrammaticalStructure gs, ArrayList<TaggedWord> tagged) {
        final TreeMap<Word, TreeSet<Word>> relations = new TreeMap<>();

        for (TypedDependency d : gs.typedDependencies()) {
            Word gov = new Word(d.gov().toString(), d.gov().index());
            String dep = d.dep().toString();
//            String reln = d.reln().toString();

            if (relations.containsKey(gov)) {
                relations.get(gov).add(new Word(dep, d.dep().index()));
            } else {
                TreeSet<Word> set = new TreeSet<>();
                set.add(new Word(dep, d.dep().index()));
                relations.put(gov, set);
            }
        }

        System.out.println(relations);

        // Find the triples
        relations.forEach((p, o) -> {
            final TreeSet<Word> subjectPhrase = new TreeSet<Word>();
            final TreeSet<Word> objectPhrase = new TreeSet<Word>();
            if (o.size() > 1) {
                o.forEach(e -> {
                    if (e.getIndex() < p.getIndex()) {
                        subjectPhrase.add(e);
                        if (relations.containsKey(e))
                            subjectPhrase.addAll(relations.get(e));
                    } else {
                        objectPhrase.add(e);
                        if (relations.containsKey(e))
                            objectPhrase.addAll(relations.get(e));
                    }
                });
            }
            if (!subjectPhrase.isEmpty() && !objectPhrase.isEmpty()) {
                System.out.println("\n==--- Triple---===");
                System.out.println("SP: " + subjectPhrase);
                System.out.println("PR: " + p);
                System.out.println("OB: " + objectPhrase);
                System.out.println("==--- Triple---===");
            }

        });

    }
}
