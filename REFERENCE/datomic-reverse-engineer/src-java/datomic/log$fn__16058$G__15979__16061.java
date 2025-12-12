/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.Log;

public final class log$fn__16058$G__15979__16061
extends AFunction {
    public Object invoke(Object gf__log__16059, Object gf__cs__16060) {
        Object object = gf__log__16059;
        gf__log__16059 = null;
        Object object2 = gf__cs__16060;
        gf__cs__16060 = null;
        return ((Log)object).claim(object2);
    }
}

