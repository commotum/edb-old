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

public final class TreeIter$iter__15121__15127$fn__15128$iter__15123__15129$fn__15130$fn__15131
extends AFunction {
    Object c__6023__auto__;
    Object ri;
    Object lookup;
    int size__6024__auto__;
    Object b__15126;
    Object d;
    int ridx;
    int didx;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"not=");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-append");
    public static final Var const__6 = RT.var((String)"datomic.common", (String)"getx");

    public TreeIter$iter__15121__15127$fn__15128$iter__15123__15129$fn__15130$fn__15131(Object object, Object object2, Object object3, int n, Object object4, Object object5, int n2, int n3) {
        this.c__6023__auto__ = object;
        this.ri = object2;
        this.lookup = object3;
        this.size__6024__auto__ = n;
        this.b__15126 = object4;
        this.d = object5;
        this.ridx = n2;
        this.didx = n3;
    }

    public Object invoke() {
        long i__15125 = (int)0L;
        while (i__15125 < (long)this.size__6024__auto__) {
            Object object;
            Object or__5238__auto__15133;
            Object di = ((Indexed)this.c__6023__auto__).nth(RT.uncheckedIntCast((long)i__15125));
            Object object2 = or__5238__auto__15133 = ((IFn)const__3.getRawRoot()).invoke(this.ri, (Object)this.ridx);
            if (object2 != null && object2 != Boolean.FALSE) {
                object = or__5238__auto__15133;
                or__5238__auto__15133 = null;
            } else {
                object = Numbers.gte((Object)di, (long)this.didx) ? Boolean.TRUE : Boolean.FALSE;
            }
            if (object != null && object != Boolean.FALSE) {
                IFn iFn = (IFn)const__5.getRawRoot();
                ((IFn)const__6.getRawRoot()).invoke(this.lookup, RT.nth((Object)((DirNode)this.d).segids, (int)RT.uncheckedIntCast((Object)((Number)di))));
                Object object3 = di;
                di = null;
                iFn.invoke(this.b__15126, RT.nth((Object)((DirNode)this.d).keydata, (int)RT.uncheckedIntCast((Object)((Number)object3))));
                ++i__15125;
                continue;
            }
            ++i__15125;
        }
        return Boolean.TRUE;
    }
}

