package com.datomic.lucene.store;

import com.datomic.lucene.store.RAMFile;
import com.datomic.lucene.store.RAMInputStream;
import java.io.IOException;

/**
 * Package-level construction bridge for Lucene RAM files and their input
 * streams. It lets the Datomic directory implementation create these store
 * objects without widening their constructors.
 */
public class DatomicThunk {
    public static RAMFile createRAMFile() {
        return new RAMFile();
    }

    public static RAMInputStream createRAMInputStream(RAMFile rf) throws IOException {
        return new RAMInputStream(rf);
    }
}
