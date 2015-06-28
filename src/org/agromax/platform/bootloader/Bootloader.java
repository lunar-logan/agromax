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

package org.agromax.platform.bootloader;

import org.agromax.util.TimeUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Anurag Gautam
 */
public class Bootloader {
    private static final Logger logger = Logger.getLogger(Bootloader.class.getName());

    private Queue<BootAction> actions = new LinkedList<>();

    public void addAction(BootAction action) {
        if (action == null)
            throw new NullPointerException("Boot action cannot be null");
        actions.add(action);
    }

    public void addAllActions(BootAction... actions) {
        addAllActions(Arrays.asList(actions));
    }

    public void addAllActions(Collection<? extends BootAction> actions) {
        for (BootAction action : actions) {
            addAction(action);
        }
    }

    public BootResult boot() {

        logger.info("Beginning cold boot");
        long startTime = System.currentTimeMillis();
        while (!actions.isEmpty()) {
            BootAction action = actions.poll();
            try {
                action.perform();
                logger.info("Performing " + action.getName() + " action ... [ OK ]");
            } catch (BootActionException e) {
                e.printStackTrace();
                logger.info("Performing " + action.getName() + " action ... [ ERR ]");
                if (action.getType() == BootActionType.REQUIRED) {
                    logger.log(Level.SEVERE, "A critical boot action \"" + action.getName() + "\" could not be performed, exiting");
                    return new BootResult(TimeUtil.getTimeDelta(startTime), false);
                }
            }
        }
        logger.info("All actions performed");
        return new BootResult(TimeUtil.getTimeDelta(startTime), true);
    }

/*
    public static void main(String[] args) {
        Bootloader bootloader = new Bootloader();
        bootloader.addAction(new BootAction() {
            @Override
            public String getName() {
                return "Action0";
            }

            @Override
            public BootActionType getType() {
                return BootActionType.OPTIONAL;
            }

            @Override
            public void perform() throws BootActionException {
                System.out.println("Action0 performed");
            }
        });
        bootloader.boot();
    }
    */
}
