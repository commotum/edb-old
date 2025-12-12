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
import datomic.domain$peer_object_lookup$fn__16795;
import datomic.domain$peer_object_lookup$load_counter__16793;

public final class domain$peer_object_lookup
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.domain", (String)"peer-object-lookup");
    public static final Var const__1 = RT.var((String)"datomic.domain", (String)"system-cache");
    public static final Var const__2 = RT.var((String)"datomic.cache", (String)"lookup-cache");
    public static final Var const__3 = RT.var((String)"datomic.cache", (String)"lookup-with-inflight-cache");
    public static final Var const__4 = RT.var((String)"datomic.cache", (String)"lookup-transformer");
    public static final Keyword const__5 = RT.keyword(null, (String)"key-fn");
    public static final Var const__6 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
    public static final Keyword const__7 = RT.keyword(null, (String)"val-fn");
    public static final Var const__8 = RT.var((String)"datomic.fressian", (String)"val->obj");

    public static Object invokeStatic(Object val_lookup, Object read_lookup, Object object_cache) {
        domain$peer_object_lookup$load_counter__16793 load_counter = new domain$peer_object_lookup$load_counter__16793();
        Object object = val_lookup;
        val_lookup = null;
        Object object2 = read_lookup;
        read_lookup = null;
        Object object3 = object_cache;
        object_cache = null;
        domain$peer_object_lookup$load_counter__16793 domain$peer_object_lookup$load_counter__16793 = load_counter;
        load_counter = null;
        return ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object, (Object)const__5, const__6.getRawRoot(), (Object)const__7, ((IFn)const__8.getRawRoot()).invoke(object2))), object3, (Object)new domain$peer_object_lookup$fn__16795((Object)domain$peer_object_lookup$load_counter__16793));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return domain$peer_object_lookup.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object val_lookup, Object read_lookup) {
        Object object = val_lookup;
        val_lookup = null;
        Object object2 = read_lookup;
        read_lookup = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, ((IFn)const__1.getRawRoot()).invoke());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return domain$peer_object_lookup.invokeStatic(object3, object4);
    }
}

