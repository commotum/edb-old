/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.atomic.LongAccumulator;

public final class monitor$snapshot_statistics$fn__547
extends AFunction {
    public static final Object const__1 = 0L;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__4 = RT.keyword(null, (String)"hi");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"not=");

    public Object invoke(Object m, Object p__546) {
        Object v;
        Object object = p__546;
        p__546 = null;
        Object vec__548 = object;
        Object k = RT.nth((Object)vec__548, (int)RT.intCast((long)0L), null);
        Object object2 = vec__548;
        vec__548 = null;
        Object object3 = v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        v = null;
        long gtrv = ((LongAccumulator)object3).get();
        Object object4 = m;
        m = null;
        Object object5 = k;
        k = null;
        Object[] objectArray = new Object[2];
        objectArray[0] = const__4;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)Numbers.num((long)Long.MIN_VALUE), (Object)Numbers.num((long)gtrv));
        objectArray[1] = object6 != null && object6 != Boolean.FALSE ? Numbers.num((long)gtrv) : const__1;
        monitor$snapshot_statistics$fn__547 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object4, object5, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }
}

