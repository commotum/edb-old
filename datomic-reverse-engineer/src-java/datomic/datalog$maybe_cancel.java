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
import java.util.concurrent.TimeoutException;

public final class datalog$maybe_cancel
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.datalog", (String)"*cancel*");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic() {
        Object temp__5457__auto__18054;
        Object object = temp__5457__auto__18054 = ((IFn)const__0.getRawRoot()).invoke(const__1.get());
        if (object != null && object != Boolean.FALSE) {
            Object why;
            Object object2 = temp__5457__auto__18054;
            temp__5457__auto__18054 = null;
            Object object3 = why = object2;
            why = null;
            throw (Throwable)new TimeoutException((String)((IFn)const__2.getRawRoot()).invoke((Object)"Query canceled: ", object3));
        }
        return null;
    }

    public Object invoke() {
        return datalog$maybe_cancel.invokeStatic();
    }
}

