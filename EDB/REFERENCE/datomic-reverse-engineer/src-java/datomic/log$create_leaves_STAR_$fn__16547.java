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

public final class log$create_leaves_STAR_$fn__16547
extends AFunction {
    Object weigh;
    Object target_size;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");

    public log$create_leaves_STAR_$fn__16547(Object object, Object object2) {
        this.weigh = object;
        this.target_size = object2;
    }

    public Object invoke(Object ftxes) {
        Object object = ftxes;
        ftxes = null;
        log$create_leaves_STAR_$fn__16547 this_ = null;
        return Numbers.lt((Object)((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(this_.weigh, object)), (Object)Numbers.quotient((Object)this_.target_size, (long)2L)) ? Boolean.TRUE : Boolean.FALSE;
    }
}

