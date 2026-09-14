/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.builtins$build_retract_args$fn__23400;

public final class builtins$build_retract_args
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Keyword const__1 = RT.keyword((String)"db", (String)"retract");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__5 = RT.var((String)"datomic.builtins", (String)"component-es-set");

    public static Object invokeStatic(Object db2, Object e) {
        Object object;
        Object temp__5457__auto__23411;
        Object object2 = e;
        e = null;
        Object object3 = temp__5457__auto__23411 = ((IFn)const__0.getRawRoot()).invoke(db2, object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object retract2;
            Object object4 = temp__5457__auto__23411;
            temp__5457__auto__23411 = null;
            Object e2 = object4;
            Object object5 = retract2 = ((IFn)const__0.getRawRoot()).invoke(db2, (Object)const__1);
            retract2 = null;
            builtins$build_retract_args$fn__23400 builtins$build_retract_args$fn__23400 = new builtins$build_retract_args$fn__23400(object5, db2);
            Object object6 = db2;
            db2 = null;
            Object object7 = e2;
            e2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)builtins$build_retract_args$fn__23400, ((IFn)const__4.getRawRoot()).invoke((Object)PersistentVector.EMPTY), ((IFn)const__5.getRawRoot()).invoke(object6, object7)));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return builtins$build_retract_args.invokeStatic(object3, object4);
    }
}

