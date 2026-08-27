/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class integrity$seq_diffs$fn__22457
extends AFunction {
    Object n;
    Object morea;
    Object b;
    Object a;
    Object moreb;
    Object progress;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");
    public static final Keyword const__1 = RT.keyword(null, (String)"a");
    public static final Keyword const__2 = RT.keyword(null, (String)"b");
    public static final Keyword const__3 = RT.keyword(null, (String)"n");
    public static final Var const__4 = RT.var((String)"datomic.integrity", (String)"seq-diffs");

    public integrity$seq_diffs$fn__22457(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.n = object;
        this.morea = object2;
        this.b = object3;
        this.a = object4;
        this.moreb = object5;
        this.progress = object6;
    }

    public Object invoke() {
        integrity$seq_diffs$fn__22457 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, this_.a, const__2, this_.b, const__3, this_.n}), ((IFn)const__4.getRawRoot()).invoke(this_.morea, this_.moreb, this_.progress, (Object)Numbers.inc((Object)this_.n)));
    }
}

