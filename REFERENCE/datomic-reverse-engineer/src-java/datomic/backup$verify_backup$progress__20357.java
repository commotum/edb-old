/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class backup$verify_backup$progress__20357
extends AFunction {
    int segcount;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mod");
    public static final Object const__2 = 1000L;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"format");

    public backup$verify_backup$progress__20357(int n) {
        this.segcount = n;
    }

    public Object invoke(Object x) {
        Object object;
        if (Util.equiv((Object)((IFn)const__1.getRawRoot()).invoke(x, const__2), (long)0L)) {
            Object object2 = x;
            x = null;
            backup$verify_backup$progress__20357 this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)"%s of %s", object2, (Object)this_.segcount));
        } else {
            object = null;
        }
        return object;
    }
}

