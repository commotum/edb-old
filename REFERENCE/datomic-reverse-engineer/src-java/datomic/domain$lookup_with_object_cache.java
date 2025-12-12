/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.domain$lookup_with_object_cache$fn__16813;
import datomic.domain$lookup_with_object_cache$load_counter__16811;

public final class domain$lookup_with_object_cache
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.domain", (String)"lookup-with-object-cache");
    public static final Var const__1 = RT.var((String)"datomic.domain", (String)"system-cache");
    public static final Var const__2 = RT.var((String)"datomic.cache", (String)"lookup-cache");

    public static Object invokeStatic(Object lookup, Object object_cache) {
        domain$lookup_with_object_cache$load_counter__16811 load_counter = new domain$lookup_with_object_cache$load_counter__16811();
        Object object = lookup;
        lookup = null;
        Object object2 = object_cache;
        object_cache = null;
        domain$lookup_with_object_cache$load_counter__16811 domain$lookup_with_object_cache$load_counter__16811 = load_counter;
        load_counter = null;
        return ((IFn)const__2.getRawRoot()).invoke(object, object2, (Object)new domain$lookup_with_object_cache$fn__16813((Object)domain$lookup_with_object_cache$load_counter__16811));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return domain$lookup_with_object_cache.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object lookup) {
        Object object = lookup;
        lookup = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$lookup_with_object_cache.invokeStatic(object2);
    }
}

