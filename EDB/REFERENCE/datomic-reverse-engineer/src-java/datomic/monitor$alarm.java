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

public final class monitor$alarm
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"Alarm");
    public static final Object const__2 = 1L;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"name");

    public static Object invokeStatic(Object k) {
        ((IFn)const__0.getRawRoot()).invoke((Object)const__1, const__2);
        Object object = k;
        k = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)"Alarm", ((IFn)const__5.getRawRoot()).invoke(object))), const__2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return monitor$alarm.invokeStatic(object2);
    }
}

