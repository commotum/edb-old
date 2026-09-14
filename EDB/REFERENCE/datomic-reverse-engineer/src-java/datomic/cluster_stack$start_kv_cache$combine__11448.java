/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cluster_stack$start_kv_cache$combine__11448
extends AFunction {
    Object wrap;
    public static final Var const__4 = RT.var((String)"datomic.core2.val-store.double-store", (String)"create");
    public static final Keyword const__5 = RT.keyword(null, (String)"near-store");
    public static final Keyword const__6 = RT.keyword(null, (String)"far-store");
    public static final Keyword const__7 = RT.keyword(null, (String)"repair-metric");
    public static final Keyword const__8 = RT.keyword(null, (String)"get-fallback-msec");

    public cluster_stack$start_kv_cache$combine__11448(Object object) {
        this.wrap = object;
    }

    public Object invoke(Object c2, Object p__11447) {
        Object object;
        Object object2;
        Object and__5236__auto__11453;
        Object object3 = p__11447;
        p__11447 = null;
        Object vec__11449 = object3;
        Object c1 = RT.nth((Object)vec__11449, (int)RT.intCast((long)0L), null);
        Object metric = RT.nth((Object)vec__11449, (int)RT.intCast((long)1L), null);
        Object object4 = vec__11449;
        vec__11449 = null;
        Object fallback_msec = RT.nth((Object)object4, (int)RT.intCast((long)2L), null);
        Object object5 = and__5236__auto__11453 = c1;
        if (object5 != null && object5 != Boolean.FALSE) {
            object2 = c2;
        } else {
            object2 = and__5236__auto__11453;
            and__5236__auto__11453 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object[] objectArray = new Object[8];
            objectArray[0] = const__5;
            Object object6 = c1;
            c1 = null;
            objectArray[1] = ((IFn)this_.wrap).invoke(object6);
            objectArray[2] = const__6;
            Object object7 = c2;
            c2 = null;
            objectArray[3] = object7;
            objectArray[4] = const__7;
            Object object8 = metric;
            metric = null;
            objectArray[5] = object8;
            objectArray[6] = const__8;
            Object object9 = fallback_msec;
            fallback_msec = null;
            objectArray[7] = object9;
            cluster_stack$start_kv_cache$combine__11448 this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            Object or__5238__auto__11454;
            Object object10 = c1;
            c1 = null;
            Object object11 = or__5238__auto__11454 = ((IFn)this_.wrap).invoke(object10);
            if (object11 != null && object11 != Boolean.FALSE) {
                object = or__5238__auto__11454;
                or__5238__auto__11454 = null;
            } else {
                object = c2;
                Object var1_1 = null;
            }
        }
        return object;
    }
}

