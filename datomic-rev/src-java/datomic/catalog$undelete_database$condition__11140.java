/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class catalog$undelete_database$condition__11140
extends AFunction {
    Object db_name;
    Object db_id;
    public static final Keyword const__1 = RT.keyword(null, (String)"name-already-taken");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"get-in");
    public static final Keyword const__4 = RT.keyword((String)"datomic", (String)"deleted");
    public static final Keyword const__5 = RT.keyword(null, (String)"id-not-deleted");

    public catalog$undelete_database$condition__11140(Object object, Object object2) {
        this.db_name = object;
        this.db_id = object2;
    }

    public Object invoke(Object p1__11138_SHARP_) {
        Object object;
        Object object2 = RT.get((Object)p1__11138_SHARP_, (Object)this.db_name);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = RT.mapUniqueKeys((Object[])new Object[]{const__1, this.db_name});
        } else {
            Object object3 = p1__11138_SHARP_;
            p1__11138_SHARP_ = null;
            Object object4 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object3, (Object)Tuple.create((Object)const__4, (Object)this.db_id)));
            object = object4 != null && object4 != Boolean.FALSE ? RT.mapUniqueKeys((Object[])new Object[]{const__5, this.db_id}) : null;
        }
        return object;
    }
}

