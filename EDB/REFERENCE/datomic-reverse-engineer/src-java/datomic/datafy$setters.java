/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.datafy$setters$fn__17159;

public final class datafy$setters
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"cls");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"filter");

    public static Object invokeStatic(Object cls) {
        Object object = cls;
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__2))));
        }
        Object object2 = cls;
        cls = null;
        return ((IFn)const__3.getRawRoot()).invoke((Object)new datafy$setters$fn__17159(), (Object)((Class)object2).getMethods());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$setters.invokeStatic(object2);
    }
}

