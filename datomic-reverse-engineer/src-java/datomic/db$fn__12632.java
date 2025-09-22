/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$fn__12632
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"require-id");

    public static Object invokeStatic(Object this_, Object db2, Object procargs, Object _) {
        Object object;
        Object object2 = procargs;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = db2;
            db2 = null;
            Object object4 = this_;
            this_ = null;
            object = Numbers.num((long)((IFn.OOL)const__0.getRawRoot()).invokePrim(object3, object4));
        } else {
            Object object5 = db2;
            db2 = null;
            Object object6 = this_;
            this_ = null;
            Object object7 = procargs;
            procargs = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object5, object6, object7);
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$fn__12632.invokeStatic(object5, object6, object7, object8);
    }
}

