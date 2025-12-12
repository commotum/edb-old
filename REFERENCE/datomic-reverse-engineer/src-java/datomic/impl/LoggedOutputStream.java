/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.fressian.impl.BytesOutputStream
 *  org.slf4j.Logger
 */
package datomic.impl;

import java.io.IOException;
import java.io.OutputStream;
import org.fressian.impl.BytesOutputStream;
import org.slf4j.Logger;

public class LoggedOutputStream
extends OutputStream {
    BytesOutputStream bos = new BytesOutputStream();
    Logger logger;

    public LoggedOutputStream(Logger l) {
        this.logger = l;
    }

    @Override
    public void write(int i) throws IOException {
        if (i == 10) {
            this.logger.debug(new String(this.bos.internalBuffer()));
            this.bos.reset();
        } else {
            this.bos.write(i);
        }
    }
}

