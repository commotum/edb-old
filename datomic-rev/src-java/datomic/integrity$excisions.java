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
import datomic.integrity$excisions$fn__22387;

public final class integrity$excisions
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"datomic.api", (String)"datoms");
    public static final Keyword const__3 = RT.keyword(null, (String)"aevt");
    public static final Keyword const__4 = RT.keyword((String)"db", (String)"excise");

    public static Object invokeStatic(Object db2) {
        integrity$excisions$fn__22387 integrity$excisions$fn__22387 = new integrity$excisions$fn__22387(db2);
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)integrity$excisions$fn__22387, ((IFn)const__2.getRawRoot()).invoke(object, (Object)const__3, (Object)const__4)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$excisions.invokeStatic(object2);
    }
}

