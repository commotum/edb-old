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

public final class db$create_attribute$fn__13123
extends AFunction {
    Object kw;
    Object attrPreds;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"create-attr-pred");

    public db$create_attribute$fn__13123(Object object, Object object2) {
        this.kw = object;
        this.attrPreds = object2;
    }

    public Object invoke() {
        db$create_attribute$fn__13123 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.kw, this_.attrPreds);
    }
}

