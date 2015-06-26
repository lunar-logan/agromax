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

/**
 * @author Anurag Gautam
 */
public class ServerConfigurations {
    public static final int DEFAULT_PORT = 34567;

    public static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 4;

    public static final String SERVER_NAME = "Agromax Event Loop";

    public static final String SERVER_VERSION = "1.1";

}
