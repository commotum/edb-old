/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.fressian.StreamingWriter
 */
package datomic;

import clojure.lang.AFunction;
import org.fressian.StreamingWriter;

public final class fressian$begin_closed_list
extends AFunction {
    public static Object invokeStatic(Object writer2) {
        Object object = writer2;
        writer2 = null;
        return ((StreamingWriter)object).beginClosedList();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$begin_closed_list.invokeStatic(object2);
    }
}

