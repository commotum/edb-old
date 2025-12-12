/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.concurrent.Semaphore;

public final class valcache$start_server$socket_loop__9798$fn__9800
extends AFunction {
    Object sc;
    Object header;
    Object handled;
    Object path;
    Object sem;
    public static final Var const__0 = RT.var((String)"datomic.valcache", (String)"supported-or-drain");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__2 = RT.keyword(null, (String)"root");
    public static final Var const__3 = RT.var((String)"datomic.valcache", (String)"handle");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"inc");

    public valcache$start_server$socket_loop__9798$fn__9800(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.sc = object;
        this.header = object2;
        this.handled = object3;
        this.path = object4;
        this.sem = object5;
    }

    public Object invoke() {
        Object object;
        try {
            Object G__9801 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this.header, (Object)const__2, this.path), this.sc, const__3.getRawRoot());
            if (Util.identical((Object)G__9801, null)) {
            } else {
                Object object2 = G__9801;
                G__9801 = null;
                ((IFn)const__3.getRawRoot()).invoke(object2, this.sc);
            }
            object = ((IFn)const__5.getRawRoot()).invoke(this.handled, const__6.getRawRoot());
        }
        finally {
            ((Semaphore)this.sem).release();
        }
        return object;
    }
}

