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

public final class catalog$delete_database$condition__11133
extends AFunction {
    Object db_name;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Keyword const__2 = RT.keyword(null, (String)"does-not-exist");

    public catalog$delete_database$condition__11133(Object object) {
        this.db_name = object;
    }

    public Object invoke(Object p1__11131_SHARP_) {
        Object object = p1__11131_SHARP_;
        p1__11131_SHARP_ = null;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(RT.get((Object)object, (Object)this.db_name));
        return object2 != null && object2 != Boolean.FALSE ? RT.mapUniqueKeys((Object[])new Object[]{const__2, this.db_name}) : null;
    }
}

