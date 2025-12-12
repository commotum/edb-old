/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$capture_last_ex$ex_handler__15426;

public final class index$capture_last_ex
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");

    public static Object invokeStatic() {
        index$capture_last_ex$ex_handler__15426 ex_handler;
        Object last_ex = ((IFn)const__0.getRawRoot()).invoke(null);
        index$capture_last_ex$ex_handler__15426 index$capture_last_ex$ex_handler__15426 = ex_handler = new index$capture_last_ex$ex_handler__15426(last_ex);
        ex_handler = null;
        Object object = last_ex;
        last_ex = null;
        return Tuple.create((Object)((Object)index$capture_last_ex$ex_handler__15426), (Object)object);
    }

    public Object invoke() {
        return index$capture_last_ex.invokeStatic();
    }
}

