/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datalog$fn__18233$fn__18242
extends AFunction {
    Object bindings;
    Object consts;
    Object bound;
    long n__5742__auto__;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not");

    public datalog$fn__18233$fn__18242(Object object, Object object2, Object object3, long l) {
        this.bindings = object;
        this.consts = object2;
        this.bound = object3;
        this.n__5742__auto__ = l;
    }

    public Object invoke() {
        for (long i = 0L; i < this.n__5742__auto__; ++i) {
            Object object;
            Object or__5238__auto__18244;
            Object object2 = or__5238__auto__18244 = RT.aget((Object[])((Object[])this.bindings), (int)((int)i));
            if (object2 != null && object2 != Boolean.FALSE) {
                object = or__5238__auto__18244;
                or__5238__auto__18244 = null;
            } else {
                object = ((IFn)const__4.getRawRoot()).invoke((Object)(Util.identical((Object)RT.get((Object)this.consts, (Object)Numbers.num((long)i)), null) ? Boolean.TRUE : Boolean.FALSE));
            }
            if (object == null || object == Boolean.FALSE) continue;
            RT.aset((Object[])((Object[])this.bound), (int)((int)i), (Object)Boolean.TRUE);
        }
        return null;
    }
}

