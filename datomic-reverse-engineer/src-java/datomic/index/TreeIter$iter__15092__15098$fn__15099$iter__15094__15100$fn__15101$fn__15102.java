/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.DirNode;

public final class TreeIter$iter__15092__15098$fn__15099$iter__15094__15100$fn__15101$fn__15102
extends AFunction {
    int size__6024__auto__;
    Object d;
    Object ri;
    int ridx;
    int didx;
    Object c__6023__auto__;
    Object b__15097;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"not=");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-append");

    public TreeIter$iter__15092__15098$fn__15099$iter__15094__15100$fn__15101$fn__15102(int n, Object object, Object object2, int n2, int n3, Object object3, Object object4) {
        this.size__6024__auto__ = n;
        this.d = object;
        this.ri = object2;
        this.ridx = n2;
        this.didx = n3;
        this.c__6023__auto__ = object3;
        this.b__15097 = object4;
    }

    public Object invoke() {
        long i__15096 = (int)0L;
        while (i__15096 < (long)this.size__6024__auto__) {
            Object object;
            Object or__5238__auto__15104;
            Object di = ((Indexed)this.c__6023__auto__).nth(RT.uncheckedIntCast((long)i__15096));
            Object object2 = or__5238__auto__15104 = ((IFn)const__3.getRawRoot()).invoke(this.ri, (Object)this.ridx);
            if (object2 != null && object2 != Boolean.FALSE) {
                object = or__5238__auto__15104;
                or__5238__auto__15104 = null;
            } else {
                object = Numbers.gte((Object)di, (long)this.didx) ? Boolean.TRUE : Boolean.FALSE;
            }
            if (object != null && object != Boolean.FALSE) {
                Object object3 = di;
                di = null;
                ((IFn)const__5.getRawRoot()).invoke(this.b__15097, RT.nth((Object)((DirNode)this.d).segids, (int)RT.uncheckedIntCast((Object)((Number)object3))));
                ++i__15096;
                continue;
            }
            ++i__15096;
        }
        return Boolean.TRUE;
    }
}

