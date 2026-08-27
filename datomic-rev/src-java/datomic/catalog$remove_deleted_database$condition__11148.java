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

public final class catalog$remove_deleted_database$condition__11148
extends AFunction {
    Object db_id;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__2 = RT.keyword((String)"datomic", (String)"deleted");
    public static final Keyword const__3 = RT.keyword(null, (String)"does-not-exist");

    public catalog$remove_deleted_database$condition__11148(Object object) {
        this.db_id = object;
    }

    public Object invoke(Object p1__11146_SHARP_) {
        Object object = p1__11146_SHARP_;
        p1__11146_SHARP_ = null;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(RT.get((Object)object, (Object)const__2), this.db_id);
        return object2 != null && object2 != Boolean.FALSE ? null : RT.mapUniqueKeys((Object[])new Object[]{const__3, this.db_id});
    }
}

