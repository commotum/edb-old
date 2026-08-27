/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.store.RAMDirectory
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.store.RAMDirectory;

public final class lucene$ram_directory
extends AFunction {
    public static Object invokeStatic() {
        return new RAMDirectory();
    }

    public Object invoke() {
        return lucene$ram_directory.invokeStatic();
    }
}

