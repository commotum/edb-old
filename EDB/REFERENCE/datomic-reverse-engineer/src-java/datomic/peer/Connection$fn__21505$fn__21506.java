/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.peer;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.reconnector2.Reconnectable;
import java.lang.ref.Reference;

public final class Connection$fn__21505$fn__21506
extends AFunction {
    Object conn_ref;
    private static Class __cached_class__0;
    public static final Var const__0;

    public Connection$fn__21505$fn__21506(Object object) {
        this.conn_ref = object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        Object conn;
        Object temp__5457__auto__21508;
        this_.conn_ref = null;
        Object t = temp__5457__auto__21508 = ((Reference)this_.conn_ref).get();
        if (t == null) return null;
        if (t == Boolean.FALSE) return null;
        Object t2 = temp__5457__auto__21508;
        temp__5457__auto__21508 = null;
        Object t3 = conn = t2;
        conn = null;
        Object t4 = t3;
        if (Util.classOf(t3) != __cached_class__0) {
            if (t4 instanceof Reconnectable) {
                object = ((Reconnectable)t4).reconnect();
                return object;
            }
            t4 = t4;
            __cached_class__0 = Util.classOf(t4);
        }
        Connection$fn__21505$fn__21506 this_ = null;
        object = const__0.getRawRoot().invoke(t4);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.reconnector2", (String)"reconnect");
    }
}

