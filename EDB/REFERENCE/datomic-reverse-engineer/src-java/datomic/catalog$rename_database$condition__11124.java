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

public final class catalog$rename_database$condition__11124
extends AFunction {
    Object db_name;
    Object new_name;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__1 = RT.var((String)"datomic.catalog", (String)"valid-db-name?");
    public static final Keyword const__2 = RT.keyword(null, (String)"invalid-db-name");
    public static final Keyword const__4 = RT.keyword(null, (String)"does-not-exist");
    public static final Keyword const__5 = RT.keyword(null, (String)"exists");

    public catalog$rename_database$condition__11124(Object object, Object object2) {
        this.db_name = object;
        this.new_name = object2;
    }

    public Object invoke(Object p1__11122_SHARP_) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this.new_name));
        if (object2 != null && object2 != Boolean.FALSE) {
            object = RT.mapUniqueKeys((Object[])new Object[]{const__2, this.new_name});
        } else {
            Object object3 = ((IFn)const__0.getRawRoot()).invoke(RT.get((Object)p1__11122_SHARP_, (Object)this.db_name));
            if (object3 != null && object3 != Boolean.FALSE) {
                object = RT.mapUniqueKeys((Object[])new Object[]{const__4, this.db_name});
            } else {
                Object object4 = p1__11122_SHARP_;
                p1__11122_SHARP_ = null;
                Object object5 = RT.get((Object)object4, (Object)this.new_name);
                object = object5 != null && object5 != Boolean.FALSE ? RT.mapUniqueKeys((Object[])new Object[]{const__5, this.db_name}) : null;
            }
        }
        return object;
    }
}

