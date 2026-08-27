/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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

public final class integrity$selfcheck_index_cli$progress__22125
extends AFunction {
    Object count;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mod");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"inc");
    public static final Object const__4 = 10000L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"flush");

    public integrity$selfcheck_index_cli$progress__22125(Object object) {
        this.count = object;
    }

    public Object invoke() {
        Object object;
        if (Numbers.isZero((Object)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this_.count, const__3.getRawRoot()), const__4))) {
            ((IFn)const__5.getRawRoot()).invoke((Object)".");
            integrity$selfcheck_index_cli$progress__22125 this_ = null;
            object = ((IFn)const__6.getRawRoot()).invoke();
        } else {
            object = null;
        }
        return object;
    }
}

