/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.fulltext_index.LuceneProvider;

public final class fulltext_index$fn__12331$G__12327__12334
extends AFunction {
    public Object invoke(Object gf__this__12332, Object gf__attr__12333) {
        Object object = gf__this__12332;
        gf__this__12332 = null;
        Object object2 = gf__attr__12333;
        gf__attr__12333 = null;
        return ((LuceneProvider)object).fulltext_attr_reader(object2);
    }
}

