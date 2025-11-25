/*
 * (C) Copyright 2025 by ScaleOut Software, Inc.
 *
 * LICENSE AND DISCLAIMER
 * ----------------------
 * This material contains sample programming source code ("Sample Code").
 * ScaleOut Software, Inc. (SSI) grants you a nonexclusive license to compile,
 * link, run, display, reproduce, and prepare derivative works of
 * this Sample Code.  The Sample Code has not been thoroughly
 * tested under all conditions.  SSI, therefore, does not guarantee
 * or imply its reliability, serviceability, or function. SSI
 * provides no support services for the Sample Code.
 *
 * All Sample Code contained herein is provided to you "AS IS" without
 * any warranties of any kind. THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGMENT ARE EXPRESSLY
 * DISCLAIMED.  SOME JURISDICTIONS DO NOT ALLOW THE EXCLUSION OF IMPLIED
 * WARRANTIES, SO THE ABOVE EXCLUSIONS MAY NOT APPLY TO YOU.  IN NO
 * EVENT WILL SSI BE LIABLE TO ANY PARTY FOR ANY DIRECT, INDIRECT,
 * SPECIAL OR OTHER CONSEQUENTIAL DAMAGES FOR ANY USE OF THE SAMPLE CODE
 * INCLUDING, WITHOUT LIMITATION, ANY LOST PROFITS, BUSINESS
 * INTERRUPTION, LOSS OF PROGRAMS OR OTHER DATA ON YOUR INFORMATION
 * HANDLING SYSTEM OR OTHERWISE, EVEN IF WE ARE EXPRESSLY ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGES.
 */
package com.scaleoutsoftware.samples;

import com.google.gson.Gson;
import com.scaleout.client.GridConnection;
import com.scaleoutsoftware.modules.client.MsgModuleSender;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jline.reader.LineReader;
import org.slf4j.helpers.MessageFormatter;

import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.scaleoutsoftware.samples.Constants.*;

public class LoadGenerator implements Runnable {
    private final String[] _ids;
    private final String[] _types;
    private final String[] _conditions;
    private final String[] _regions;
    private final double[] _longitudes;
    private final double[] _latitudes;
    private final boolean[] _attacks;
    private final int _count;
    private final MsgModuleSender _sender;
    private final HashMap<String,Boolean> _msgsSentDuringInterval;
    private final HashMap<Integer,Long> _idxToNextInterval;
    private final HashMap<String, Long> _asyncAttackedNodes;
    private final HashMap<String, Integer> _idToIdx;
    private final int _msgsPerSecond;
    private final LineReader _reader;
    private final Random RANDOM = new Random();


    public LoadGenerator(String connectionString, String filePath, String[] defaultAttackNodeIds, int trackerCount, int msgsPerSecond, LineReader reader) {
        String[] tempids        = new String[trackerCount];
        String[] temptypes      = new String[trackerCount];
        String[] tempconditions = new String[trackerCount];
        String[] tempregions    = new String[trackerCount];
        double[] templongitudes = new double[trackerCount];
        double[] templatitudes  = new double[trackerCount];
        boolean[] tempattacks   = new boolean[trackerCount];
        _asyncAttackedNodes = new HashMap<>();
        _idxToNextInterval = new HashMap<>();
        _msgsPerSecond = msgsPerSecond;
        _idToIdx = new HashMap<>();
        _reader = reader;
        _reader.printAbove(MessageFormatter.format("Attempting to read in and create {} GeoSpatial trackers.", trackerCount).getMessage());
        Reader in = null;
        try {
            in = new FileReader(filePath);
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .build();
            Iterable<CSVRecord> records = csvFormat.parse(in);
            Iterator<CSVRecord> recordIterator = records.iterator();
            boolean onlyDefaultAttackNodes = false;
            if(trackerCount <= defaultAttackNodeIds.length) {
                onlyDefaultAttackNodes = true;
            }

            int actualCount = 0;
            for(int i = 0; i < trackerCount; i++) {
                if(recordIterator.hasNext()) {
                    CSVRecord record = recordIterator.next();
                    boolean add = false;
                    if(onlyDefaultAttackNodes) {
                        for(String id : defaultAttackNodeIds) {
                            if(record.get(HEADER_IDX_ID).compareTo(id) == 0) {
                                add = true;
                                break;
                            }
                        }
                    } else {
                        add = true;
                    }
                    if(add) {
                        actualCount++;
                        tempids[i]          = record.get(HEADER_IDX_ID);
                        temptypes[i]        = record.get(HEADER_IDX_TYPE);
                        tempconditions[i]   = Constants.convertColorToStatus(record.get(HEADER_IDX_STATUS));
                        tempregions[i]      = record.get(HEADER_IDX_REGION);
                        templongitudes[i]   = Double.parseDouble(record.get(HEADER_IDX_LONGITUDE));
                        templatitudes[i]    = Double.parseDouble(record.get(HEADER_IDX_LATITUDE));
                        tempattacks[i]      = Boolean.parseBoolean(record.get(HEADER_IDX_ATTACKED));
                        _idToIdx.put(tempids[i], i);
                        if(tempattacks[i]) {
                            _asyncAttackedNodes.put(tempids[i], -1L);
                        }
                    }
                } else {
                    _reader.printAbove("Expected > " + trackerCount + " but found " + actualCount + "; using " + actualCount+ " trackers.");
                    break;
                }
            }
            _ids        = new String[actualCount];
            _types      = new String[actualCount];
            _conditions = new String[actualCount];
            _regions    = new String[actualCount];
            _longitudes = new double[actualCount];
            _latitudes  = new double[actualCount];
            _attacks    = new boolean[actualCount];
            for(int i = 0; i < actualCount; i++) {
                _ids[i]         = tempids[i];
                _types[i]       = temptypes[i];
                _conditions[i]  = tempconditions[i];
                _regions[i]     = tempregions[i];
                _longitudes[i]  = templongitudes[i];
                _latitudes[i]   = templatitudes[i];
                _attacks[i]     = tempattacks[i];
            }
            _count = actualCount;
            _reader.printAbove(MessageFormatter.format("Created {} trackers; Connecting to ScaleOut and initializing demo. {}", _count, connectionString).getMessage());
            GridConnection connection = GridConnection.connect(connectionString);
            _sender = new MsgModuleSender(connection);
            _msgsSentDuringInterval = new HashMap<>(actualCount);
            Gson gson = new Gson();
            List<CompletableFuture<Void>> futureList = new LinkedList<>();
            long startTime = System.currentTimeMillis();
            // send initialization messages, the MSG module will create the SOSS object based off the incoming message
            for(int i = 0; i < _count; i++) {
                _msgsSentDuringInterval.put(_ids[i], false);
                GeoSpatialMessage message = new GeoSpatialMessage(_ids[i], _regions[i], _conditions[i], _types[i], _latitudes[i], _longitudes[i]);
                _idxToNextInterval.put(i, startTime);
                String jsonMessage = gson.toJson(message);
                byte[] serializedMsg = jsonMessage.getBytes(StandardCharsets.UTF_8);
                futureList.add(_sender.sendToModule(TARGET_MODULE_NAME, _ids[i], serializedMsg));
            }
            for(CompletableFuture<Void> future : futureList) {
                future.get();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void attackAll(String[] nodesToAttack) {
        synchronized (_asyncAttackedNodes) {
            for(String zip : nodesToAttack) {
                int idx = _idToIdx.getOrDefault(zip, -1);
                if(idx == -1) {
                    _reader.printAbove(MessageFormatter.format("Zip {} not found", zip).getMessage());
                    continue;
                }
                _attacks[idx] = true;
                _asyncAttackedNodes.put(zip, -1L);
            }

        }

    }

    void attack(String zip) {
        synchronized (_asyncAttackedNodes) {
            int idx = _idToIdx.getOrDefault(zip, -1);
            if(idx == -1) {
                _reader.printAbove(MessageFormatter.format("Zip {} not found", zip).getMessage());
                return;
            }
            _attacks[idx] = true;
            _asyncAttackedNodes.put(zip, -1L);
        }
    }

    private GeoSpatialMessage calculateNextMessage(int idx) {
        GeoSpatialMessage message;
        /* if we have been asynchronously attacked, send message and delay accordingly */
        if (_attacks[idx]) {
            _reader.printAbove(MessageFormatter.format("Node {} attacked!", _ids[idx]).getMessage());
            _conditions[idx] = Constants.NODE_CONDITION_SEVERE;
            _attacks[idx] = false;
            message = new GeoSpatialMessage(NODE_CONDITION_SEVERE);
        } else if (_conditions[idx].compareTo(NODE_CONDITION_SEVERE) == 0) {
            _reader.printAbove(MessageFormatter.format("LoadGenerator stops tracking attack for {}.",  _ids[idx]).getMessage());
            _conditions[idx] = Constants.NODE_CONDITION_OFFLINE;
            message = new GeoSpatialMessage(NODE_CONDITION_OFFLINE);
        }
        /* compute if we should update our status... */
        else if (calculateState(Constants.SIMULATION_PR_OFFLINE)) {
            _conditions[idx] = Constants.NODE_CONDITION_OFFLINE;
            message = new GeoSpatialMessage(NODE_CONDITION_OFFLINE);
        } else if (calculateState(Constants.SIMULATION_PR_NORMAL)) {
            _conditions[idx] = NODE_CONDITION_NORMAL;
            message = new GeoSpatialMessage(NODE_CONDITION_NORMAL);
        } else if (calculateState(Constants.SIMULATION_PR_MINOR)) {
            _conditions[idx] = NODE_CONDITION_MINOR;
            message = new GeoSpatialMessage(NODE_CONDITION_MINOR);
        } else if (calculateState(Constants.SIMULATION_PR_MODERATE)) {
            _conditions[idx] = NODE_CONDITION_MODERATE;
            message = new GeoSpatialMessage(NODE_CONDITION_MODERATE);
        } else {
            // no change
            message = new GeoSpatialMessage(_conditions[idx]);
        }
        return message;
    }

    private boolean calculateState(int probability) {
        int rVal = Math.abs(RANDOM.nextInt(0x7fff));
        int	max = 0x7fff * probability / 100;
        return rVal <= max;
    }

    /**
     * Run continuously, sending messages to SOSS objects in the MSG module
     */
    @Override
    public void run() {
        try {
            _reader.printAbove("Starting load generator...");
            Gson gson = new Gson();
            List<CompletableFuture<Void>> futureList = new LinkedList<>();
            // run continuously
            while(true) {
                long intervalTimeMs = System.currentTimeMillis();
                for(int i = 0; i < _msgsPerSecond; i++) {
                    if(_asyncAttackedNodes.size() == _ids.length) break;
                    // randomly select ID
                    int next = RANDOM.nextInt(_ids.length);
                    int maxIdFindingAttempt = 1000;
                    int attempt = 0;
                    // make sure it's not in attacked node list
                    while(_asyncAttackedNodes.containsKey(_ids[next])) {
                        attempt++;
                        next = RANDOM.nextInt(_ids.length);
                        // check delay based on current state
                        String curState = _conditions[next];
                        long nextIntervalTimeMs = intervalTimeMs + findDelayTimeMs(curState);
                        if(_idxToNextInterval.get(next) >= nextIntervalTimeMs) break;

                        if(attempt == maxIdFindingAttempt) {
                            break;
                        }
                    }
                    if(attempt != maxIdFindingAttempt) {
                        // calculate tracker condition
                        GeoSpatialMessage msg = calculateNextMessage(next);
                        String jsonMessage = gson.toJson(msg);
                        byte[] serializedMsg = jsonMessage.getBytes(StandardCharsets.UTF_8);
                        futureList.add(_sender.sendToModule(TARGET_MODULE_NAME, _ids[next], serializedMsg, Duration.ofSeconds(5)));
                    }

                }
                if(!_asyncAttackedNodes.isEmpty()) {
                    synchronized (_asyncAttackedNodes) {
                        List<String> nodesToRemove = new LinkedList<>();
                        for(String zip : _asyncAttackedNodes.keySet()) {
                            int idx = _idToIdx.getOrDefault(zip, -1);
                            if(idx == -1) {
                                _reader.printAbove(MessageFormatter.format("ZIP {} did not exist.", zip).getMessage());
                                continue;
                            }
                            long attackStartTimeMs = _asyncAttackedNodes.get(zip);
                            if(attackStartTimeMs == -1) {
                                _asyncAttackedNodes.put(zip, intervalTimeMs);
                                GeoSpatialMessage msg = calculateNextMessage(idx);
                                String jsonMessage = gson.toJson(msg);
                                byte[] serializedMsg = jsonMessage.getBytes(StandardCharsets.UTF_8);
                                futureList.add(_sender.sendToModule(TARGET_MODULE_NAME, _ids[idx], serializedMsg, Duration.ofSeconds(5)));
                            } else if (intervalTimeMs - attackStartTimeMs >= ATTACK_DURATION) {
                                nodesToRemove.add(zip);
                                GeoSpatialMessage msg = calculateNextMessage(idx);
                                String jsonMessage = gson.toJson(msg);
                                byte[] serializedMsg = jsonMessage.getBytes(StandardCharsets.UTF_8);
                                futureList.add(_sender.sendToModule(TARGET_MODULE_NAME, _ids[idx], serializedMsg, Duration.ofSeconds(5)));
                            }
                        }
                        for(String zip : nodesToRemove) {
                            _asyncAttackedNodes.remove(zip);
                        }
                    }
                }
                for(CompletableFuture<Void> future : futureList) {
                    future.get();
                }
                long stopTimeMs = System.currentTimeMillis();
                long elapsedTimeMs = stopTimeMs - intervalTimeMs;
                long sleepTimeMs = Math.max(0, 1000-elapsedTimeMs);
                _reader.printAbove("Finished interval in " + elapsedTimeMs + ". Sleeping for " + sleepTimeMs + ".");
                Thread.sleep(sleepTimeMs);
            }
        } catch (Exception e) {
            _reader.printAbove(e.getMessage());
            System.exit(-19);
            throw new RuntimeException(e);
        }
    }
}
