/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.RT;

public final class async$channel_closed_error
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"error"), "Channel closed", RT.keyword((String)"cognitect.anomalies", (String)"message"), "Channel closed", RT.keyword((String)"cognitect.anomalies", (String)"category"), RT.keyword((String)"cognitect.anomalies", (String)"fault")});

    public static Object invokeStatic(Object x) {
        Object object = x;
        x = null;
        return object != null && object != Boolean.FALSE ? null : const__4;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return async$channel_closed_error.invokeStatic(object2);
    }
}

