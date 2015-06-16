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

package org.agromax.vocab;

import java.io.*;

/**
 * @author Bruce Wayne (aka Batman)
 */
public class VocabParser {
    private final VocabStore vocabStore = new VocabStore();

    public VocabParser(InputStream in) {
        try (VocabularyReader vr = new VocabularyReader(in)) {
            while (true) {
                String inp = null;
                try {
                    inp = vr.next();
                } catch (EOFException e) {
                    e.printStackTrace();
                }
                if (inp == null) break;
                parse(inp);
            }
        }
    }

    static class VocabularyReader implements Closeable {
        private final BufferedReader br;

        public VocabularyReader(InputStream in) {
            this.br = new BufferedReader(new InputStreamReader(in));
        }

        public String next() throws EOFException {
            try {
                String inp = br.readLine();
                if (inp == null || inp.isEmpty()) return null;
                return inp;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void close() {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public VocabStore getVocabStore() {
        return vocabStore;
    }

    public void parse(String token) {
        token = token.replaceAll(" +", " ");
        int sp = token.indexOf(' ');
        if (sp > 0) {
            String catalogue = token.substring(0, sp);
            String concept = token.substring(sp + 1).toLowerCase();
            vocabStore.insert(catalogue, concept);
        } else {
            System.err.println("Invalid vocabulary input.");
        }
    }
/*
    public static void main(String[] args) throws IOException {
        VocabularyReader vr = new VocabularyReader(new FileInputStream(Util.VOCAB_DIR + "/Agricultural economics.txt"));
        while (true) {
            String inp = vr.next();
            if (inp == null) break;
            parse(inp);
        }
        vr.close();
        System.out.println("Vocabulary parsing is complete...");
        System.out.println(vocabStore);

        System.out.println("Approx memory used: " + vocabStore.getMemoryUsage());
        Random r = new Random(123456789L);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String inp = br.readLine();
            if (inp.startsWith("quit")) break;
//            String[] chain = inp.trim().split("-");
            System.out.println(vocabStore.getRelatedDomains(inp));
        }

    }*/
}
