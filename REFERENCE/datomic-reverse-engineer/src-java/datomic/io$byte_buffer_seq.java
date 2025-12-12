/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import datomic.io$byte_buffer_seq$fn__9350;

public final class io$byte_buffer_seq
extends AFunction {
    public static Object invokeStatic(Object bb) {
        Object object = bb;
        bb = null;
        return new LazySeq((IFn)new io$byte_buffer_seq$fn__9350(object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$byte_buffer_seq.invokeStatic(object2);
    }
}

