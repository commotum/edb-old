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

public final class query$group_fv$fn__19472
extends AFunction {
    Object gns;
    Object list_QMARK_;
    Object smap;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"ns-resolve");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"last");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"butlast");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"rest");
    public static final Var const__7 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__8 = RT.keyword((String)"db.error", (String)"invalid-aggregate");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");

    public query$group_fv$fn__19472(Object object, Object object2, Object object3) {
        this.gns = object;
        this.list_QMARK_ = object2;
        this.smap = object3;
    }

    public Object invoke(Object p1__19469_SHARP_) {
        query$group_fv$fn__19472 this_;
        Object object;
        Object object2 = ((IFn)this_.list_QMARK_).invoke(p1__19469_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object temp__5455__auto__19474;
            Object object3 = temp__5455__auto__19474 = ((IFn)const__0.getRawRoot()).invoke(this_.gns, ((IFn)const__1.getRawRoot()).invoke(p1__19469_SHARP_));
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = temp__5455__auto__19474;
                temp__5455__auto__19474 = null;
                Object agg_fn = object4;
                Object object5 = ((IFn)this_.smap).invoke(((IFn)const__2.getRawRoot()).invoke(p1__19469_SHARP_));
                Object object6 = agg_fn;
                agg_fn = null;
                Object object7 = p1__19469_SHARP_;
                p1__19469_SHARP_ = null;
                object = Tuple.create((Object)object5, (Object)((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), object6, ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object7))));
            } else {
                Object object8 = p1__19469_SHARP_;
                p1__19469_SHARP_ = null;
                this_ = null;
                object = ((IFn)const__7.getRawRoot()).invoke((Object)const__8, ((IFn)const__9.getRawRoot()).invoke((Object)"Argument ", ((IFn)const__1.getRawRoot()).invoke(object8), (Object)" in :find is not an aggregate function"));
            }
        } else {
            Object object9 = p1__19469_SHARP_;
            p1__19469_SHARP_ = null;
            this_ = null;
            object = ((IFn)this_.smap).invoke(object9);
        }
        return object;
    }
}

