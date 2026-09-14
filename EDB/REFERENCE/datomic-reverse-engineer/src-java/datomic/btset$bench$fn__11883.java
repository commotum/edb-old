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

public final class btset$bench$fn__11883
extends AFunction {
    Object bt;
    public static final Var const__0 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"inext");
    public static final Var const__4 = RT.var((String)"datomic.iter", (String)"iget");

    public btset$bench$fn__11883(Object object) {
        this.bt = object;
    }

    public Object invoke() {
        Object s = ((IFn)const__0.getRawRoot()).invoke(this.bt);
        Object ret = Numbers.num((long)0L);
        while (true) {
            Object object = s;
            if (object == null || object == Boolean.FALSE) break;
            Object object2 = ((IFn)const__3.getRawRoot()).invoke(s);
            Object object3 = s;
            s = null;
            ret = ((IFn)const__4.getRawRoot()).invoke(object3);
            s = object2;
        }
        Object var2_2 = null;
        return ret;
    }
}

