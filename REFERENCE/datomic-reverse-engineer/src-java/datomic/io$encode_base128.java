/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.impl.JavaByteUtil;

public final class io$encode_base128
extends AFunction {
    public static Object invokeStatic(Object raw) {
        Object object = raw;
        raw = null;
        return JavaByteUtil.to7Bit((byte[])object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$encode_base128.invokeStatic(object2);
    }
}

