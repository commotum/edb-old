/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.future.GetChannel;

public final class future$fn__10153$G__10149__10155
extends AFunction {
    public Object invoke(Object gf__fut__10154) {
        Object object = gf__fut__10154;
        gf__fut__10154 = null;
        return ((GetChannel)object).get_channel();
    }
}

