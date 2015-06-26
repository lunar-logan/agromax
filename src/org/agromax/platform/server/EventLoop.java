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

import org.agromax.core.StanfordParserContext;
import org.agromax.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author Anurag Gautam
 */
public class EventLoop implements Runnable {

    private static final EventLoop ourInstance = new EventLoop();

    private static final Logger logger = Logger.getLogger(EventLoop.class.getName());

    public static EventLoop getInstance(StanfordParserContext context) {
        ourInstance.stanfordContext = context;
        return ourInstance;
    }

    private volatile boolean stopServer = false;
    private volatile StanfordParserContext stanfordContext = null;

    private EventLoop() {
    }

    @Override
    public void run() {
        ExecutorService pool = Executors.newFixedThreadPool(ServerConfigurations.DEFAULT_NUM_THREADS);

        logger.info("Starting server at port " + ServerConfigurations.DEFAULT_PORT);
        try (ServerSocket serverSocket = new ServerSocket(ServerConfigurations.DEFAULT_PORT)) {
            logger.info(ServerConfigurations.SERVER_NAME + " listening at " + serverSocket.getInetAddress() + ":" + ServerConfigurations.DEFAULT_PORT);
            for (; !stopServer; ) {
                Socket client = serverSocket.accept();
                logger.info("Request: " + client);
                pool.execute(new RequestHandler(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }

    public void stopService() {
        stopServer = true;
    }

    static class Request {
        private final String commandName;
        private final Object[] parameters;

        public Request(String commandName, Object... params) {
            this.commandName = commandName;
            this.parameters = params;
        }

        public Object[] getParameters() {
            return parameters;
        }

        public Object getCommand(int i) {
            if (Util.inClosedRange(0, i, parameters.length - 1)) {
                throw new IndexOutOfBoundsException("Index " + i + " is out of bounds");
            }
            return parameters[i];
        }

        public String getCommandName() {
            return commandName;
        }
    }

    static class Response {
        private final Request request;

        public Response(Request request) {
            this.request = request;
        }

        public void write(OutputStream outputStream) {
            try {
                outputStream.write("Input received".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class RequestHandler implements Runnable {
        private final Socket client;

        public RequestHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String[] reqElements = br.readLine().replaceAll(" +", " ").trim().split(" ");
                System.out.println(Arrays.toString(reqElements));
                Request req = new Request(reqElements[0], reqElements);
                Response res = new Response(req);
                res.write(client.getOutputStream());
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}