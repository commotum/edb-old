/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.integrity$unfindable_datom_seq$fn__22024;
import datomic.integrity$unfindable_datom_seq$fn__22026;
import datomic.integrity$unfindable_datom_seq$fn__22028;
import datomic.integrity$unfindable_datom_seq$fn__22030;
import datomic.integrity$unfindable_datom_seq$fn__22032;

public final class integrity$unfindable_datom_seq
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"aevt");
    public static final Keyword const__1 = RT.keyword(null, (String)"avet");
    public static final Keyword const__2 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__3 = RT.keyword(null, (String)"vaet");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__6 = RT.var((String)"datomic.api", (String)"datoms");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object index2, Object progress) {
        AFunction op;
        AFunction aFunction;
        block6: {
            Object G__22023 = index2;
            switch (Util.hash((Object)G__22023) >> 5 & 3) {
                case 0: {
                    if (G__22023 != const__0) break;
                    aFunction = new integrity$unfindable_datom_seq$fn__22024();
                    break block6;
                }
                case 1: {
                    if (G__22023 != const__1) break;
                    aFunction = new integrity$unfindable_datom_seq$fn__22026();
                    break block6;
                }
                case 2: {
                    if (G__22023 != const__2) break;
                    aFunction = new integrity$unfindable_datom_seq$fn__22028();
                    break block6;
                }
                case 3: {
                    if (G__22023 != const__3) break;
                    aFunction = new integrity$unfindable_datom_seq$fn__22030();
                    break block6;
                }
            }
            Object object = G__22023;
            G__22023 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__4.getRawRoot()).invoke((Object)"No matching clause: ", object));
        }
        AFunction aFunction2 = op = aFunction;
        op = null;
        Object object = progress;
        progress = null;
        integrity$unfindable_datom_seq$fn__22032 integrity$unfindable_datom_seq$fn__22032 = new integrity$unfindable_datom_seq$fn__22032(aFunction2, object, db2);
        Object object2 = db2;
        db2 = null;
        return ((IFn)const__5.getRawRoot()).invoke((Object)integrity$unfindable_datom_seq$fn__22032, ((IFn)const__6.getRawRoot()).invoke(object2, index2));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return integrity$unfindable_datom_seq.invokeStatic(object4, object5, object6);
    }
}

