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

import org.agromax.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Anurag Gautam
 */
public class VocabLoader {
    private static final Logger logger = Logger.getLogger(VocabLoader.class.getName());

    public static ArrayList<VocabStore> loadAll() {
        final ArrayList<VocabStore> stores = new ArrayList<>();

        File vocabRoot = new File(Util.VOCAB_DIR);
        try {
            Files.newDirectoryStream(vocabRoot.toPath()).forEach(f -> {
                File vocabFile = f.toFile();
                String fileName = vocabFile.getName();
                if (vocabFile.isFile() && (fileName.endsWith("txt") || fileName.endsWith("pdf"))) {
                    logger.info("Trying to load vocabulary from: " + vocabFile);
                    try {
                        VocabParser parser = new VocabParser(new FileInputStream(vocabFile));
                        stores.add(parser.getVocabStore());
                        logger.info("Vocabulary added to the stores list");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stores;
    }
}
