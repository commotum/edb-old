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

public final class integrity$progress_dot
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"flush");

    public static Object invokeStatic(ISeq _) {
        ((IFn)const__0.getRawRoot()).invoke((Object)".");
        return ((IFn)const__1.getRawRoot()).invoke();
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return integrity$progress_dot.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

