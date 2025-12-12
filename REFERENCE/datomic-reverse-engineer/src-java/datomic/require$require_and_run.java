/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class require$require_and_run
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"require");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"resolve");

    public static Object invokeStatic(Object sym, ISeq args) {
        Object temp__5457__auto__644;
        Object object = temp__5457__auto__644 = ((IFn)const__0.getRawRoot()).invoke(sym);
        if (object != null && object != Boolean.FALSE) {
            Object ns;
            Object object2 = temp__5457__auto__644;
            temp__5457__auto__644 = null;
            Object object3 = ns = object2;
            ns = null;
            ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object3));
        }
        Object object4 = sym;
        sym = null;
        ISeq iSeq = args;
        args = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object4), (Object)iSeq);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return require$require_and_run.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

