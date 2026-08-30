/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.activemq.artemis.api.core.ActiveMQBuffer
 */
package com.datomic.impl.peer;

import java.io.IOException;
import java.io.InputStream;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;

public class ActiveMQInputStream
extends InputStream {
    private final ActiveMQBuffer buf;

    public ActiveMQInputStream(ActiveMQBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int read() throws IOException {
        if (this.buf.readableBytes() < 1) {
            return -1;
        }
        return 0xFF & this.buf.readByte();
    }

    @Override
    public int read(byte[] bytes, int start, int length) throws IOException {
        int read = this.buf.readableBytes() < length ? this.buf.readableBytes() : length;
        if (read > 0) {
            this.buf.readBytes(bytes, start, read);
        }
        return read;
    }
}
