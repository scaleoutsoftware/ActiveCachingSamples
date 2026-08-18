/*
 * (C) Copyright 2026 by ScaleOut Software, Inc.
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
package com.scaleoutsoftware.server;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.scaleout.client.caching.DeserializationException;
import com.scaleout.client.caching.SerializationException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class KryoSerializationHelper {
    private static final int MAX_STRING_LIST_SIZE = 1_000_000;
    private static final int INITIAL_BUFFER_SIZE = 256;
    private static final ThreadLocal<KryoState> STATE = ThreadLocal.withInitial(KryoState::new);

    private KryoSerializationHelper() {
    }

    public static byte[] serializeList(List<String> values) throws SerializationException {
        if (values == null) {
            throw new SerializationException("values cannot be null.");
        }

        KryoState state = STATE.get();
        Output output = state.output;
        output.reset();

        try {
            output.writeVarInt(values.size(), true);
            for (String value : values) {
                output.writeString(value);
            }
            return output.toBytes();
        } catch (KryoException e) {
            throw new SerializationException("Failed to serialize List<String> with Kryo.", e);
        }
    }

    public static List<String> deserializeList(byte[] bytes) throws DeserializationException {
        if (bytes == null) throw new DeserializationException("bytes cannot be null.");

        KryoState state = STATE.get();
        Input input = state.input;
        input.setBuffer(bytes);

        try {
            int size = input.readVarInt(true);

            if (size > MAX_STRING_LIST_SIZE) {
                throw new DeserializationException("Refusing to deserialize oversized List<String>: " + size);
            }

            List<String> values = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                values.add(input.readString());
            }

            return values;
        } catch (KryoException e) {
            throw new DeserializationException("Failed to deserialize List<String> with Kryo.", e);
        }
    }

    private static final class KryoState {
        private final Kryo kryo = createKryo();
        private final Output output = new Output(INITIAL_BUFFER_SIZE, -1);
        private final Input input = new Input(INITIAL_BUFFER_SIZE);
    }

    private static Kryo createKryo() {
        Kryo kryo = new Kryo();

        kryo.setReferences(false);
        kryo.setRegistrationRequired(true);
        kryo.register(LinkedList.class, 102);

        return kryo;
    }
}