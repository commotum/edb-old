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

public final class kv_cache$start_kv_cache$combine__10073
extends AFunction {
    Object record;
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"repairing-cache-stack");
    public static final Keyword const__1 = RT.keyword(null, (String)"cache-1");
    public static final Keyword const__2 = RT.keyword(null, (String)"cache-2");
    public static final Keyword const__3 = RT.keyword(null, (String)"on-repair");

    public kv_cache$start_kv_cache$combine__10073(Object object) {
        this.record = object;
    }

    public Object invoke(Object c2, Object c1) {
        Object object;
        Object object2;
        Object and__5236__auto__10075;
        Object object3 = and__5236__auto__10075 = c1;
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = c2;
        } else {
            object2 = and__5236__auto__10075;
            and__5236__auto__10075 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object[] objectArray = new Object[6];
            objectArray[0] = const__1;
            Object object4 = c1;
            c1 = null;
            objectArray[1] = object4;
            objectArray[2] = const__2;
            Object object5 = c2;
            c2 = null;
            objectArray[3] = object5;
            objectArray[4] = const__3;
            objectArray[5] = this_.record;
            kv_cache$start_kv_cache$combine__10073 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            Object or__5238__auto__10076;
            Object object6 = c1;
            c1 = null;
            Object object7 = or__5238__auto__10076 = object6;
            if (object7 != null && object7 != Boolean.FALSE) {
                object = or__5238__auto__10076;
                or__5238__auto__10076 = null;
            } else {
                object = c2;
                Object var1_1 = null;
            }
        }
        return object;
    }
}

