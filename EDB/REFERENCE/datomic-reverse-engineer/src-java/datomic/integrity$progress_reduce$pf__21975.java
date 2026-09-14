/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class integrity$progress_reduce$pf__21975
extends AFunction {
    Object f;
    Object progress;
    Object n;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"mod");

    public integrity$progress_reduce$pf__21975(Object object, Object object2, Object object3) {
        this.f = object;
        this.progress = object2;
        this.n = object3;
    }

    public Object invoke(Object p__21974, Object item) {
        Object object = p__21974;
        p__21974 = null;
        Object vec__21976 = object;
        Object c = RT.nth((Object)vec__21976, (int)RT.intCast((long)0L), null);
        Object object2 = vec__21976;
        vec__21976 = null;
        Object acc = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        if (Numbers.isZero((Object)((IFn)const__4.getRawRoot()).invoke(c, this.n))) {
            ((IFn)this.progress).invoke(acc, this.n);
        }
        Object object3 = c;
        c = null;
        Object object4 = acc;
        acc = null;
        Object object5 = item;
        item = null;
        return Tuple.create((Object)Numbers.inc((Object)object3), (Object)((IFn)this.f).invoke(object4, object5));
    }
}

