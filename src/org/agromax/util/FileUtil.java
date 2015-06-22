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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Tony Stark
 */
public class FileUtil {
    private static final int BUFFER_SIZE = 2048; // In bytes

    public static String read(Path path) {
        Objects.requireNonNull(path, "File path must not be null");

        Util.log("Trying to read the following file: ", path);

        StringBuilder text = new StringBuilder();

        try (FileReader reader = new FileReader(path.toFile())) {
            char[] buf = new char[BUFFER_SIZE];
            while (true) {
                int bytesRead = reader.read(buf);
                if (bytesRead <= -1) break;
                text.append(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }

    public static void write(byte[] bytes, Charset charset, Path path) {
        write(new String(bytes, charset), path);
    }

    public static void write(char[] buf, Path path) {
        write(new String(buf), path);
    }

    public static void write(String text, Path path) {
        Objects.requireNonNull(path, "File path must not be null");

        Util.log("Writing to the following file: ", path);

        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(text.toCharArray());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
