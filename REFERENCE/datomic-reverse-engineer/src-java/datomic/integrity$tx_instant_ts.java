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
import datomic.integrity$tx_instant_ts$fn__22434;

public final class integrity$tx_instant_ts
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"drop-while");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__3 = RT.var((String)"datomic.api", (String)"tx->t");
    public static final Keyword const__4 = RT.keyword(null, (String)"tx");
    public static final Var const__5 = RT.var((String)"datomic.api", (String)"datoms");
    public static final Keyword const__6 = RT.keyword(null, (String)"avet");
    public static final Keyword const__7 = RT.keyword((String)"db", (String)"txInstant");

    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new integrity$tx_instant_ts$fn__22434(), ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), (Object)const__4), ((IFn)const__5.getRawRoot()).invoke(object, (Object)const__6, (Object)const__7)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$tx_instant_ts.invokeStatic(object2);
    }
}

