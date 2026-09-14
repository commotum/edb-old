/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.datomic.lucene.store.RAMFile
 *  com.datomic.lucene.store.RAMInputStream
 */
package com.datomic.lucene.store;

import com.datomic.lucene.store.RAMFile;
import com.datomic.lucene.store.RAMInputStream;
import java.io.IOException;

public class DatomicThunk {
    public static RAMFile createRAMFile() {
        return new RAMFile();
    }

    public static RAMInputStream createRAMInputStream(RAMFile rf) throws IOException {
        return new RAMInputStream(rf);
    }
}

