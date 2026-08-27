/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.lucene.Readerable;

public final class lucene$fn__12275$G__12271__12277
extends AFunction {
    public Object invoke(Object gf_____12276) {
        Object object = gf_____12276;
        gf_____12276 = null;
        return ((Readerable)object).index_reader();
    }
}

