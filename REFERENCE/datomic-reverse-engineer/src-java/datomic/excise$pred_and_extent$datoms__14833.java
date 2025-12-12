/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.excise$pred_and_extent$datoms__14833$fn__14835;
import datomic.excise$pred_and_extent$datoms__14833$fn__14837;

public final class excise$pred_and_extent$datoms__14833
extends AFunction {
    Object extent;
    Object db;
    Object remove_QMARK_;
    Object target;
    Object type;
    public static final Keyword const__0 = RT.keyword(null, (String)"a");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__3 = RT.keyword(null, (String)"aevt");
    public static final Keyword const__4 = RT.keyword(null, (String)"e");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");

    public excise$pred_and_extent$datoms__14833(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.extent = object;
        this.db = object2;
        this.remove_QMARK_ = object3;
        this.target = object4;
        this.type = object5;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object G__14834 = this_.type;
        switch (Util.hash((Object)G__14834)) {
            case 1013910569: {
                if (G__14834 != const__0) break;
                excise$pred_and_extent$datoms__14833 this_ = null;
                Object object = ((IFn)const__1.getRawRoot()).invoke(this_.remove_QMARK_, ((IFn)const__2.getRawRoot()).invoke((Object)((Database)this_.db).history(), (Object)const__3, (Object)Tuple.create((Object)this_.target)));
                return object;
            }
            case 1013910832: {
                if (G__14834 != const__4) break;
                excise$pred_and_extent$datoms__14833 this_ = null;
                Object object = ((IFn)const__1.getRawRoot()).invoke(this_.remove_QMARK_, ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)new excise$pred_and_extent$datoms__14833$fn__14835(this_.db), this_.extent), ((IFn)const__6.getRawRoot()).invoke((Object)new excise$pred_and_extent$datoms__14833$fn__14837(this_.db), this_.extent)));
                return object;
            }
        }
        Object object = G__14834;
        G__14834 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"No matching clause: ", object));
    }
}

