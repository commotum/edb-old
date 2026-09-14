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

public final class require$_main
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"prn");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__2 = RT.var((String)"datomic.require", (String)"require-and-run");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"symbol");

    public static Object invokeStatic(Object sname, ISeq args) {
        Object var2_2;
        try {
            Object object = sname;
            sname = null;
            ISeq iSeq = args;
            args = null;
            ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(object), (Object)iSeq));
            System.exit(RT.intCast((long)0L));
            var2_2 = null;
        }
        catch (Throwable t2) {
            Object t2 = null;
            t2.printStackTrace();
            System.exit(RT.intCast((long)-1L));
            var2_2 = null;
        }
        return var2_2;
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return require$_main.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

