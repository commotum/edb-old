/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IDeref
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.domain;

import clojure.lang.AFn;
import clojure.lang.IDeref;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.summary.Summary;

public final class ValcachePoller
implements Summary,
IDeref,
AutoCloseable,
IType {
    public final Object cluster;
    public final Object valcache_group_config;
    public final Object server_specs_ref;
    public final Object timer;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__2 = RT.keyword(null, (String)"server-specs");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"redact");
    public static final AFn const__5 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"password")});

    public ValcachePoller(Object object, Object object2, Object object3, Object object4) {
        this.cluster = object;
        this.valcache_group_config = object2;
        this.server_specs_ref = object3;
        this.timer = object4;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"valcache-group-config"), (Object)Symbol.intern(null, (String)"server-specs-ref"), (Object)((IObj)Symbol.intern(null, (String)"timer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.AutoCloseable")})));
    }

    public Object summary() {
        ValcachePoller this_ = null;
        return ((IFn)const__1.getRawRoot()).invoke(this_.valcache_group_config, (Object)const__2, ((IFn)const__3.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(this_.server_specs_ref), (Object)const__5));
    }

    public Object deref() {
        ValcachePoller this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.server_specs_ref);
    }

    public void close() throws Exception {
        ((AutoCloseable)this.timer).close();
    }
}

