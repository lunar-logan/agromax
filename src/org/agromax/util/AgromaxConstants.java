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

import java.nio.file.Paths;

/**
 * @author Anurag Gautam
 */
public class AgromaxConstants {

    // Path of English model of Stanford parser
    public static final String SP_MODEL_PATH = "E:\\Coding\\Java Related\\stanford-parser\\stanford-parser-full-2015-04-20\\edu\\stanford\\nlp\\models\\parser\\nndep\\english_UD.gz";

    // Path of English tagger of Stanford parser
    public static final String SP_TAGGER_PATH = "E:\\Coding\\Java Related\\stanford-parser\\stanford-parser-full-2015-04-20\\edu\\stanford\\nlp\\models\\pos-tagger\\english-left3words\\english-left3words-distsim.tagger";

    public static final String VOCAB_DIR = Paths.get(System.getProperty("user.dir"), "data", "vocab").toString();

    public static final String DATA_DIR = System.getProperty("user.dir");

    public static final String BASE_URL = "http://agro";

}
