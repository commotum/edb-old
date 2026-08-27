/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class config$version
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.java.io", (String)"resource");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"trim");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"slurp");
    public static final Var const__3 = RT.var((String)"datomic.require", (String)"require-and-run");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"datomic.cli", (String)"development-version");

    public static Object invokeStatic(ISeq _) {
        Object object;
        Object temp__5455__auto__774;
        Object object2 = temp__5455__auto__774 = ((IFn)const__0.getRawRoot()).invoke((Object)"datomic/VERSION");
        if (object2 != null && object2 != Boolean.FALSE) {
            Object f;
            Object object3 = temp__5455__auto__774;
            temp__5455__auto__774 = null;
            Object object4 = f = object3;
            f = null;
            object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object4));
        } else {
            object = ((IFn)const__3.getRawRoot()).invoke((Object)const__4);
        }
        return object;
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return config$version.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

