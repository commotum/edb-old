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
import datomic.impl.db.IDatum;
import java.io.Writer;

public final class db$fn__12531
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"print-method");

    public static Object invokeStatic(Object d, Object w) {
        ((Writer)w).write("#datom[");
        ((IFn)const__0.getRawRoot()).invoke((Object)Numbers.num((long)((IDatum)d).getE()), w);
        ((Writer)w).write(" ");
        ((IFn)const__0.getRawRoot()).invoke((Object)((IDatum)d).getA(), w);
        ((Writer)w).write(" ");
        ((IFn)const__0.getRawRoot()).invoke(((IDatum)d).getV(), w);
        ((Writer)w).write(" ");
        ((IFn)const__0.getRawRoot()).invoke((Object)Numbers.num((long)((IDatum)d).getTx()), w);
        ((Writer)w).write(" ");
        Object object = d;
        d = null;
        ((IFn)const__0.getRawRoot()).invoke((Object)(((IDatum)object).isAssertion() ? Boolean.TRUE : Boolean.FALSE), w);
        Object object2 = w;
        w = null;
        ((Writer)object2).write("]");
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$fn__12531.invokeStatic(object3, object4);
    }
}

