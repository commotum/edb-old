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
import datomic.db.Attribute;
import datomic.impl.db.IDatum;

public final class index$dropped_avet_aids$fadd__15588$fn__15589
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"conj!");

    public index$dropped_avet_aids$fadd__15588$fn__15589(Object object) {
        this.db = object;
    }

    public Object invoke(Object aids, Object d) {
        Object object;
        Object object2 = ((Attribute)((IFn)index$dropped_avet_aids$fadd__15588$fn__15589.const__0.getRawRoot()).invoke((Object)this_.db, (Object)Numbers.num((long)((IDatum)d).getE()))).needsAVET;
        if (object2 != null && object2 != Boolean.FALSE) {
            object = aids;
            aids = null;
        } else {
            Object object3 = aids;
            aids = null;
            Object object4 = d;
            d = null;
            index$dropped_avet_aids$fadd__15588$fn__15589 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, (Object)Numbers.num((long)((IDatum)object4).getE()));
        }
        return object;
    }
}

