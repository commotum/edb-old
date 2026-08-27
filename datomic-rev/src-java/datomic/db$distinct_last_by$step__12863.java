/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$distinct_last_by$step__12863$fn__12867;

public final class db$distinct_last_by$step__12863
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"cons");

    public Object invoke(Object f, Object prior, Object coll) {
        Object object;
        block2: {
            block1: {
                Object more;
                Object cur;
                while (true) {
                    Object vec__12864;
                    Object temp__5455__auto__12870;
                    Object object2 = coll;
                    coll = null;
                    Object object3 = temp__5455__auto__12870 = ((IFn)const__0.getRawRoot()).invoke(object2);
                    if (object3 == null || object3 == Boolean.FALSE) break block1;
                    Object object4 = temp__5455__auto__12870;
                    temp__5455__auto__12870 = null;
                    Object object5 = vec__12864 = object4;
                    vec__12864 = null;
                    Object seq__12865 = ((IFn)const__0.getRawRoot()).invoke(object5);
                    Object first__12866 = ((IFn)const__1.getRawRoot()).invoke(seq__12865);
                    Object object6 = seq__12865;
                    seq__12865 = null;
                    Object seq__128652 = ((IFn)const__2.getRawRoot()).invoke(object6);
                    Object object7 = first__12866;
                    first__12866 = null;
                    cur = object7;
                    Object object8 = seq__128652;
                    seq__128652 = null;
                    more = object8;
                    if (!Util.equiv((Object)((IFn)f).invoke(prior), (Object)((IFn)f).invoke(cur))) break;
                    Object object9 = f;
                    f = null;
                    Object object10 = cur;
                    cur = null;
                    Object object11 = more;
                    more = null;
                    coll = object11;
                    prior = object10;
                    f = object9;
                }
                cur = null;
                f = null;
                more = null;
                prior = null;
                object = new LazySeq((IFn)new db$distinct_last_by$step__12863$fn__12867((Object)this_, cur, f, more, prior));
                break block2;
            }
            Object object12 = prior;
            prior = null;
            db$distinct_last_by$step__12863 this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object12, null);
        }
        return object;
    }
}

