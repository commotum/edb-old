/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LD
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.atomic.LongAdder;

public final class db$summarize_tx_stats$fn__13409
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc!");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"timing-key?");
    public static final Var const__2 = RT.var((String)"datomic.monitor", (String)"ns->ms");

    public Object invoke(Object m, Object k, Object adder) {
        Object object = m;
        m = null;
        Object object2 = k;
        Object object3 = adder;
        adder = null;
        long G__13410 = ((LongAdder)object3).sum();
        Object object4 = k;
        k = null;
        Object object5 = ((IFn)const__1.getRawRoot()).invoke(object4);
        db$summarize_tx_stats$fn__13409 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)(object5 != null && object5 != Boolean.FALSE ? (Number)((IFn.LD)const__2.getRawRoot()).invokePrim(G__13410) : (Number)Numbers.num((long)G__13410)));
    }
}

