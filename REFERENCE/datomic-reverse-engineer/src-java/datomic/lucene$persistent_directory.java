/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentMap
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import datomic.impl.lucene.DirectoryRef;

public final class lucene$persistent_directory
extends AFunction {
    public static Object invokeStatic(Object pdmap) {
        Object object = pdmap;
        pdmap = null;
        return new DirectoryRef((IPersistentMap)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$persistent_directory.invokeStatic(object2);
    }
}

