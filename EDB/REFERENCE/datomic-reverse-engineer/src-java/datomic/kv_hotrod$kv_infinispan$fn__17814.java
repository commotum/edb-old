/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.infinispan.client.hotrod.RemoteCacheManager
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.infinispan.client.hotrod.RemoteCacheManager;

public final class kv_hotrod$kv_infinispan$fn__17814
extends AFunction {
    Object host;
    Object lockee__5436__auto__;
    Object port;
    Object endpoint;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.kv-hotrod", (String)"managers");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");

    public kv_hotrod$kv_infinispan$fn__17814(Object object, Object object2, Object object3, Object object4) {
        this.host = object;
        this.lockee__5436__auto__ = object2;
        this.port = object3;
        this.endpoint = object4;
    }

    public Object invoke() {
        Object object;
        try {
            synchronized (this.lockee__5436__auto__) {
                Object object2;
                Object or__5238__auto__17816;
                Object object3 = or__5238__auto__17816 = RT.get((Object)((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), (Object)this.endpoint);
                if (object3 != null && object3 != Boolean.FALSE) {
                    object2 = or__5238__auto__17816;
                    or__5238__auto__17816 = null;
                } else {
                    this.host = null;
                    this.port = null;
                    RemoteCacheManager m = new RemoteCacheManager((String)((IFn)const__3.getRawRoot()).invoke(this.host), RT.intCast((Object)this.port));
                    this.endpoint = null;
                    ((IFn)const__5.getRawRoot()).invoke(const__2.getRawRoot(), const__6.getRawRoot(), this.endpoint, (Object)m);
                    object2 = m;
                    Object var2_2 = null;
                }
                object = object2;
            }
        }
        finally {
            this.lockee__5436__auto__ = null;
            // ** MonitorExit[this.lockee__5436__auto__] (shouldn't be in output)
        }
        {
            return object;
        }
    }
}

