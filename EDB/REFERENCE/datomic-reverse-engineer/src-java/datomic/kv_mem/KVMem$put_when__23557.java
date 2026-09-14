/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.kv_mem;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public final class KVMem$put_when__23557
extends AFunction {
    Object m;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");

    public KVMem$put_when__23557(Object object) {
        this.m = object;
    }

    public Object invoke(Object k, Object val, Object emap2) {
        Boolean bl;
        block2: {
            while (true) {
                Object oldv = ((Map)this.m).get(k);
                if (!Util.equiv((Object)emap2, (Object)((IFn)const__1.getRawRoot()).invoke(oldv, ((IFn)const__2.getRawRoot()).invoke(emap2)))) break;
                Object v = oldv;
                oldv = null;
                boolean or__5238__auto__23559 = ((ConcurrentMap)this.m).replace(k, v, val);
                if (or__5238__auto__23559) {
                    bl = or__5238__auto__23559 ? Boolean.TRUE : Boolean.FALSE;
                    break block2;
                }
                Object object = k;
                k = null;
                Object object2 = val;
                val = null;
                Object object3 = emap2;
                emap2 = null;
                emap2 = object3;
                val = object2;
                k = object;
            }
            bl = null;
        }
        return bl;
    }
}

