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
import datomic.integrity$validate_indexed_excisions$fn__22402;
import datomic.integrity$validate_indexed_excisions$fn__22404;

public final class integrity$validate_indexed_excisions
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"dorun");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"take-while");
    public static final Var const__3 = RT.var((String)"datomic.integrity", (String)"excisions");

    public static Object invokeStatic(Object db2) {
        integrity$validate_indexed_excisions$fn__22404 integrity$validate_indexed_excisions$fn__22404 = new integrity$validate_indexed_excisions$fn__22404(db2);
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)new integrity$validate_indexed_excisions$fn__22402(), ((IFn)const__2.getRawRoot()).invoke((Object)integrity$validate_indexed_excisions$fn__22404, ((IFn)const__3.getRawRoot()).invoke(object))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$validate_indexed_excisions.invokeStatic(object2);
    }
}

