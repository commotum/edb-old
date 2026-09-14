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

public final class domain$uncached_lookup_factory
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"lookup-transformer");
    public static final Var const__1 = RT.var((String)"datomic.cluster", (String)"uncached-val-lookup");
    public static final Keyword const__2 = RT.keyword(null, (String)"key-fn");
    public static final Var const__3 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
    public static final Keyword const__4 = RT.keyword(null, (String)"val-fn");
    public static final Var const__5 = RT.var((String)"datomic.fressian", (String)"val->obj");
    public static final Var const__6 = RT.var((String)"datomic.domain", (String)"common-read-handlers");

    public static Object invokeStatic(Object cluster2) {
        Object object = cluster2;
        cluster2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object), (Object)const__2, const__3.getRawRoot(), (Object)const__4, ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot()));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$uncached_lookup_factory.invokeStatic(object2);
    }
}

