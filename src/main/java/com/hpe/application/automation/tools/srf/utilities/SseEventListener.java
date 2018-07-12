/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */
package com.hpe.application.automation.tools.srf.utilities;

import com.hpe.application.automation.tools.srf.model.SrfSseEventNotification;
import net.sf.json.JSONObject;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.InboundEvent;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class SseEventListener extends Observable implements EventListener {

    private PrintStream logger;
    private final String delim;
    private ArrayList<Observer> observers;

    public SseEventListener(PrintStream logger) {
        this.logger = logger;
        this.delim = "\r\n#########################################################################\r\n";
        this.observers = new ArrayList<>();
    }

    @Override
    public void onEvent(InboundEvent inboundEvent) {

        String eventName = inboundEvent.getName();
        String data = inboundEvent.readData();

        if(data == null || data.isEmpty())
            return;

        try {

            JSONObject obj = JSONObject.fromObject(data);

            switch (eventName) {
                case "test-run-count":
                    return;
                case "test-run-started":
                    this.testRunStartedHandler(obj, eventName);
                    break;
                case "script-run-started":
                    this.scriptRunStartedHandler();
                    break;
                case "script-step-created":
                    this.scriptStepCreatedHandler(obj, eventName);
                    break;
                case "script-run-ended":
                    this.scriptRunEndedHandler();
                    break;
                case "test-run-ended":
                    this.testRunEndedHandler(obj, eventName);
                    break;

                default:
                    return;
            }

            logger.print(delim);
            logger.print(obj.toString(2));
            logger.print("\r\n");
        }
        catch (Exception e){
            logger.print(e.getMessage());
        }
    }

    @Override
    public synchronized void addObserver(Observer o) {
        this.observers.add(o);
    }

    @Override
    public synchronized void deleteObserver(Observer o) {
        this.observers.remove(o);
    }

    @Override
    public void notifyObservers(Object arg) {
        for (Observer observer : this.observers) {
            observer.update(this, arg);
        }
    }

    private void testRunStartedHandler(JSONObject obj, String eventName) {
        this.logger.print(delim);
        obj.discard("runningCount");
        JSONObject o1 = JSONObject.fromObject(obj.get("testRun"));
        String str = String.format("\r\n%1s %2s Status:%3s\r\n",
                o1.get("name"),
                eventName,
                o1.get("status"));
        this.notifyObservers(new SrfSseEventNotification(SrfSseEventNotification.SrfTestRunEvents.TEST_RUN_START, o1.getString("id")));
        this.logger.print(str);
    }

    private void testRunEndedHandler(JSONObject obj, String eventName) {

        this.logger.print(delim);
        obj.discard("runningCount");
        JSONObject o1 = JSONObject.fromObject(obj.get("testRun"));
        String testRunId = o1.getString("id");
        o1.discard("id");
        o1.discard("tags");
        o1.discard("user");
        o1.discard("additionalData");
        obj.discard("testRun");

        JSONObject o2 = JSONObject.fromObject(o1.get("test"));
        o1.discard("test");
        obj.put("testRun", o1);
        obj.put("environments", o2.get("environments"));
        obj.put("scripts", o2.get("scripts"));

        String str = String.format("\r\n%1s %2s Status:%3s\r\n",
                o1.get("name"),
                eventName,
                o1.get("status")
        );
        this.logger.print(str);
        this.notifyObservers(new SrfSseEventNotification(SrfSseEventNotification.SrfTestRunEvents.TEST_RUN_END, testRunId));
    }

    private void scriptStepCreatedHandler(JSONObject obj, String eventName) {
        String status = obj.getString("status");
        if(status.compareTo("running") == 0)
            return;

        logger.print(delim);
        String str = String.format("\r\n%1s Status: %2s\r\n",
                eventName,
                obj.get("status")
        );
        logger.print(str);
        obj.discard("id");
        obj.discard("scriptRun");
        obj.discard("snapshot");
    }

    private void scriptRunStartedHandler() {
        logger.print(delim);
    }

    private void scriptRunEndedHandler() {
        logger.print(delim);
    }

}



