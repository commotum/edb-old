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
import datomic.datalog$push_preds$fn__18585$fn__18597$fn__18598;

public final class datalog$push_preds$fn__18585$fn__18597
extends AFunction {
    Object ctors;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"every-pred");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");

    public datalog$push_preds$fn__18585$fn__18597(Object object) {
        this.ctors = object;
    }

    public Object invoke() {
        datalog$push_preds$fn__18585$fn__18597 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke((Object)new datalog$push_preds$fn__18585$fn__18597$fn__18598(), this_.ctors));
    }
}

