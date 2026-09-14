/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.common.AsyncShutdown;

public final class common$fn__9077$G__9073__9079
extends AFunction {
    public Object invoke(Object gf__o__9078) {
        Object object = gf__o__9078;
        gf__o__9078 = null;
        return ((AsyncShutdown)object).async_shutdown();
    }
}

