/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$tuple_install_errors$fn__13063
extends AFunction {
    Object e;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Keyword const__1 = RT.keyword((String)"db", (String)"error");
    public static final Keyword const__2 = RT.keyword(null, (String)"entity");

    public db$tuple_install_errors$fn__13063(Object object) {
        this.e = object;
    }

    public Object invoke(Object kw_or_m) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(kw_or_m);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object[] objectArray = new Object[4];
            objectArray[0] = const__1;
            Object object3 = kw_or_m;
            kw_or_m = null;
            objectArray[1] = object3;
            objectArray[2] = const__2;
            objectArray[3] = this.e;
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            object = kw_or_m;
            Object var1_1 = null;
        }
        return object;
    }
}

