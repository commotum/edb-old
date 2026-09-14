/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.Log;

public final class log$fn__16008$G__15983__16010
extends AFunction {
    public Object invoke(Object gf__log__16009) {
        Object object = gf__log__16009;
        gf__log__16009 = null;
        return ((Log)object).get_root_id();
    }
}

