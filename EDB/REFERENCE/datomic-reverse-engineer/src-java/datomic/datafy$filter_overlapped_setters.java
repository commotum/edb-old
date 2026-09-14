/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.datafy$filter_overlapped_setters$fn__17166;
import datomic.datafy$filter_overlapped_setters$fn__17172;

public final class datafy$filter_overlapped_setters
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"group-by");

    public static Object invokeStatic(Object coll) {
        Object object = coll;
        coll = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new datafy$filter_overlapped_setters$fn__17166(), (Object)PersistentVector.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)new datafy$filter_overlapped_setters$fn__17172(), object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$filter_overlapped_setters.invokeStatic(object2);
    }
}

