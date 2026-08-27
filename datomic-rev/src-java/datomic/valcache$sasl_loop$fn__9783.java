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

public final class valcache$sasl_loop$fn__9783
extends AFunction {
    Object header;
    Object creds;
    Object sem;
    Object sc;
    public static final Var const__0 = RT.var((String)"datomic.valcache", (String)"supported-or-drain");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__2 = RT.keyword(null, (String)"sasl");
    public static final Var const__3 = RT.var((String)"datomic.valcache", (String)"sasl");

    public valcache$sasl_loop$fn__9783(Object object, Object object2, Object object3, Object object4) {
        this.header = object;
        this.creds = object2;
        this.sem = object3;
        this.sc = object4;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            Object G__9784 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this.header, (Object)const__2, this.creds), this.sc, const__3.getRawRoot());
            if (Util.identical((Object)G__9784, null)) {
                object2 = null;
            } else {
                Object object3 = G__9784;
                G__9784 = null;
                object2 = ((IFn)const__3.getRawRoot()).invoke(object3, this.sc);
            }
            object = object2;
        }
        finally {
            ((Semaphore)this.sem).release();
        }
        return object;
    }
}

