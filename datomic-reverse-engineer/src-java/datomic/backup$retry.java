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
import datomic.backup$retry$fn__20041;
import datomic.backup$retry$fn__20043;

public final class backup$retry
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"retry-fn");
    public static final Keyword const__1 = RT.keyword(null, (String)"pred");
    public static final Keyword const__2 = RT.keyword(null, (String)"backoff");
    public static final Keyword const__3 = RT.keyword(null, (String)"log-retry");
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"log-retry");
    public static final Keyword const__5 = RT.keyword(null, (String)"max-retries");
    public static final Object const__6 = 8L;

    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1, (Object)new backup$retry$fn__20041(), (Object)const__2, (Object)new backup$retry$fn__20043(), (Object)const__3, const__4.getRawRoot(), (Object)const__5, const__6);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$retry.invokeStatic(object2);
    }
}

