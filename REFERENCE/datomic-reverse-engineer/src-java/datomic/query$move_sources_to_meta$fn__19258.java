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

public final class query$move_sources_to_meta$fn__19258
extends AFunction {
    Object sset;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Keyword const__3 = RT.keyword(null, (String)"tag");
    public static final Var const__4 = RT.var((String)"datomic.datalog", (String)"source?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__6 = RT.keyword(null, (String)"else");

    public query$move_sources_to_meta$fn__19258(Object object) {
        this.sset = object;
    }

    public Object invoke(Object p1__19255_SHARP_) {
        Object object;
        Object object2 = ((IFn)this_.sset).invoke(((IFn)const__0.getRawRoot()).invoke(p1__19255_SHARP_));
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__2.getRawRoot()).invoke(p1__19255_SHARP_);
            Object[] objectArray = new Object[2];
            objectArray[0] = const__3;
            Object object4 = p1__19255_SHARP_;
            p1__19255_SHARP_ = null;
            objectArray[1] = ((IFn)const__0.getRawRoot()).invoke(object4);
            query$move_sources_to_meta$fn__19258 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            Object object5 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(p1__19255_SHARP_));
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = p1__19255_SHARP_;
                p1__19255_SHARP_ = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__5.getRawRoot()).invoke((Object)"Data source not supplied: ", ((IFn)const__0.getRawRoot()).invoke(object6)));
            }
            Keyword keyword = const__6;
            if (keyword != null && keyword != Boolean.FALSE) {
                object = p1__19255_SHARP_;
                p1__19255_SHARP_ = null;
            } else {
                object = null;
            }
        }
        return object;
    }
}

