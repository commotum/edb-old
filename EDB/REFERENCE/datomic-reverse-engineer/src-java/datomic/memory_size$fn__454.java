/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import java.net.URI;

public final class memory_size$fn__454
extends AFunction {
    public static Object invokeStatic(Object uri2) {
        long l = Numbers.add((long)Numbers.add((long)Numbers.add((long)Numbers.add((long)Numbers.add((long)16L, (long)32L), (long)160L), (long)48L), (long)8L), (long)Numbers.multiply((long)2L, (long)RT.count((Object)((URI)uri2).getSchemeSpecificPart())));
        Object object = uri2;
        uri2 = null;
        return Numbers.num((long)Numbers.add((long)l, (long)Numbers.multiply((long)2L, (long)RT.count((Object)((URI)object).getScheme()))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory_size$fn__454.invokeStatic(object2);
    }
}

