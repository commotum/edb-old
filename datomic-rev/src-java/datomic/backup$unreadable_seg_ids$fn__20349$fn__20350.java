/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class backup$unreadable_seg_ids$fn__20349$fn__20350
extends AFunction {
    Object unreadable_QMARK_;
    Object k;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"log-and-print");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"verify-backup", (String)"unreadable-segment");
    public static final Keyword const__3 = RT.keyword(null, (String)"k");

    public backup$unreadable_seg_ids$fn__20349$fn__20350(Object object, Object object2) {
        this.unreadable_QMARK_ = object;
        this.k = object2;
    }

    public Object invoke() {
        Object object;
        Object object2 = ((IFn)this.unreadable_QMARK_).invoke(this.k);
        if (object2 != null && object2 != Boolean.FALSE) {
            ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, this.k}));
            object = this.k;
        } else {
            object = null;
        }
        return object;
    }
}

