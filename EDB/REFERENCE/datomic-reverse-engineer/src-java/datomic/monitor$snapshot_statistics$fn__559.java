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
import java.util.concurrent.atomic.LongAdder;

public final class monitor$snapshot_statistics$fn__559
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__4 = RT.keyword(null, (String)"count");

    public Object invoke(Object m, Object p__558) {
        Object object = p__558;
        p__558 = null;
        Object vec__560 = object;
        Object k = RT.nth((Object)vec__560, (int)RT.intCast((long)0L), null);
        Object object2 = vec__560;
        vec__560 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = m;
        m = null;
        Object object4 = k;
        k = null;
        Object[] objectArray = new Object[2];
        objectArray[0] = const__4;
        Object object5 = v;
        v = null;
        objectArray[1] = Numbers.num((long)((LongAdder)object5).sum());
        monitor$snapshot_statistics$fn__559 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object3, object4, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }
}

