/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Reflector
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.Reflector;
import java.util.List;

public final class support$counted_seq$fn__19116
extends AFunction {
    Object base_seq;

    public support$counted_seq$fn__19116(Object object) {
        this.base_seq = object;
    }

    public Object invoke(Object object, Object o) {
        Object[] objectArray = new Object[1];
        Object object2 = o;
        o = null;
        objectArray[0] = object2;
        return Reflector.invokeInstanceMethod((Object)this.base_seq, (String)"toArray", (Object[])objectArray);
    }

    public Object invoke(Object object) {
        return ((List)this.base_seq).toArray();
    }
}

