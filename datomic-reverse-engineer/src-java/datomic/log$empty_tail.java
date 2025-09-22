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

public final class log$empty_tail
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"create-tail");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentVector.EMPTY, (Object)PersistentVector.EMPTY);
    }

    public Object invoke() {
        return log$empty_tail.invokeStatic();
    }
}

