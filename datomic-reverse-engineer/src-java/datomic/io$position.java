/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OL
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import java.nio.Buffer;

public final class io$position
extends AFunction
implements IFn.OL {
    public static long invokeStatic(Object bb) {
        Object object = bb;
        bb = null;
        return ((Buffer)object).position();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return new Long(io$position.invokeStatic(object2));
    }

    public final long invokePrim(Object object) {
        Object object2 = object;
        object = null;
        return io$position.invokeStatic(object2);
    }
}

