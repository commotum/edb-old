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
import datomic.catalog$update_catalog$fn__11099;
import datomic.catalog$update_catalog$fn__11102;

public final class catalog$update_catalog
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"retry-fn");
    public static final Keyword const__1 = RT.keyword(null, (String)"pred");
    public static final Keyword const__2 = RT.keyword(null, (String)"backoff");
    public static final Object const__3 = 0L;
    public static final Keyword const__4 = RT.keyword(null, (String)"max-retries");
    public static final Object const__5 = 10L;
    public static final Keyword const__6 = RT.keyword(null, (String)"log-retry");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"log-retry");

    public static Object invokeStatic(Object cluster2, Object condition, Object f) {
        Object object = f;
        f = null;
        Object object2 = condition;
        condition = null;
        Object object3 = cluster2;
        cluster2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new catalog$update_catalog$fn__11099(object, object2, object3), (Object)const__1, (Object)new catalog$update_catalog$fn__11102(), (Object)const__2, const__3, (Object)const__4, const__5, (Object)const__6, const__7.getRawRoot());
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return catalog$update_catalog.invokeStatic(object4, object5, object6);
    }
}

