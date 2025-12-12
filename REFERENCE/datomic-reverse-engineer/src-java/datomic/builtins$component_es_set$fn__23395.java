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
import datomic.impl.db.IDatum;

public final class builtins$component_es_set$fn__23395
extends AFunction {
    Object via;
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__2 = RT.var((String)"datomic.builtins", (String)"component-attr?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");

    public builtins$component_es_set$fn__23395(Object object, Object object2) {
        this.via = object;
        this.db = object2;
    }

    public Object invoke(Object s, Object d) {
        Object object;
        Object object2;
        Object and__5236__auto__23398;
        Object object3;
        Object or__5238__auto__23397;
        Object object4 = or__5238__auto__23397 = ((IFn)const__0.getRawRoot()).invoke(this_.via);
        if (object4 != null && object4 != Boolean.FALSE) {
            object3 = or__5238__auto__23397;
            or__5238__auto__23397 = null;
        } else {
            object3 = ((IFn)const__1.getRawRoot()).invoke(this_.via, (Object)((IDatum)d).getA());
        }
        Object object5 = and__5236__auto__23398 = object3;
        if (object5 != null && object5 != Boolean.FALSE) {
            object2 = ((IFn)const__2.getRawRoot()).invoke(this_.db, (Object)((IDatum)d).getA());
        } else {
            object2 = and__5236__auto__23398;
            Object var3_3 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object6 = s;
            s = null;
            Object object7 = d;
            d = null;
            builtins$component_es_set$fn__23395 this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object6, ((IDatum)object7).getV());
        } else {
            object = s;
            Object var1_1 = null;
        }
        return object;
    }
}

