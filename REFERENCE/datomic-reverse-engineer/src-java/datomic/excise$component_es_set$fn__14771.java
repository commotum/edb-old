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

public final class excise$component_es_set$fn__14771
extends AFunction {
    Object db;
    Object via;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__2 = RT.var((String)"datomic.excise", (String)"component-attr?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");

    public excise$component_es_set$fn__14771(Object object, Object object2) {
        this.db = object;
        this.via = object2;
    }

    public Object invoke(Object s, Object d) {
        Object object;
        Object object2;
        Object and__5236__auto__14774;
        Object object3;
        Object or__5238__auto__14773;
        Object object4 = or__5238__auto__14773 = ((IFn)const__0.getRawRoot()).invoke(this_.via);
        if (object4 != null && object4 != Boolean.FALSE) {
            object3 = or__5238__auto__14773;
            or__5238__auto__14773 = null;
        } else {
            object3 = ((IFn)const__1.getRawRoot()).invoke(this_.via, (Object)((IDatum)d).getA());
        }
        Object object5 = and__5236__auto__14774 = object3;
        if (object5 != null && object5 != Boolean.FALSE) {
            object2 = ((IFn)const__2.getRawRoot()).invoke(this_.db, (Object)((IDatum)d).getA());
        } else {
            object2 = and__5236__auto__14774;
            Object var3_3 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object6 = s;
            s = null;
            Object object7 = d;
            d = null;
            excise$component_es_set$fn__14771 this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object6, ((IDatum)object7).getV());
        } else {
            object = s;
            Object var1_1 = null;
        }
        return object;
    }
}

