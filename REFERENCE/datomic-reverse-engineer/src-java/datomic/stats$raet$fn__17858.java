/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Reflector
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Reflector;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class stats$raet$fn__17858
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"partition-eid");

    public stats$raet$fn__17858(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__17857_SHARP_) {
        Object object;
        Object or__5238__auto__17860;
        Object object2 = p1__17857_SHARP_;
        p1__17857_SHARP_ = null;
        long part2 = ((IFn.LL)const__0.getRawRoot()).invokePrim(((IDatum)object2).getE());
        Object object3 = or__5238__auto__17860 = Reflector.invokeInstanceMethod((Object)this.db, (String)"ident", (Object[])new Object[]{Numbers.num((long)part2)});
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__17860;
            or__5238__auto__17860 = null;
        } else {
            object = Numbers.num((long)part2);
        }
        return object;
    }
}

