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
package datomic.cluster_stack;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ValStoreOnKvCache$fn__11261
extends AFunction {
    Object kv_cache;
    Object k;
    public static final Keyword const__0 = RT.keyword(null, (String)"val");
    public static final Var const__2 = RT.var((String)"datomic.core2.anomalies", (String)"fault");

    public ValStoreOnKvCache$fn__11261(Object object, Object object2) {
        this.kv_cache = object;
        this.k = object2;
    }

    public Object invoke() {
        Object object;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.k = null;
            objectArray[1] = RT.get((Object)this.kv_cache, (Object)this.k);
            object = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t2) {
            Object t2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)t2);
        }
        return object;
    }
}

