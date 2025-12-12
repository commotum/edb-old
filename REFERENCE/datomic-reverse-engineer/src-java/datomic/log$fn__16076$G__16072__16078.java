/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.TailTxes;

public final class log$fn__16076$G__16072__16078
extends AFunction {
    public Object invoke(Object gf_____16077) {
        Object object = gf_____16077;
        gf_____16077 = null;
        return ((TailTxes)object).tail_txes();
    }
}

