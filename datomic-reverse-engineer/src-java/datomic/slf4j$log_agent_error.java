/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class slf4j$log_agent_error
extends AFunction {
    public static Object invokeStatic(Object agent, Object error2) {
        IFn iFn = (IFn)error2;
        Object object = error2;
        error2 = null;
        return iFn.invoke((Object)"Unexpected error on agent.", object);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return slf4j$log_agent_error.invokeStatic(object3, object4);
    }
}

