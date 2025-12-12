/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class tools$diagnostics$fn__21821
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.tools", (String)"peer-diagnostics");
    public static final Var const__1 = RT.var((String)"datomic.tools", (String)"tools-fault");

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke();
        }
        catch (Throwable t2) {
            Object t2 = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)t2);
        }
        return object;
    }
}

