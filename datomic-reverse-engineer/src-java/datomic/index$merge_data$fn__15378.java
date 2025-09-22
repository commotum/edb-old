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
import java.util.Comparator;

public final class index$merge_data$fn__15378
extends AFunction {
    Object ds2;
    Object ds1;
    Object cmp;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"merge-data");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"next");
    public static final Keyword const__6 = RT.keyword(null, (String)"else");

    public index$merge_data$fn__15378(Object object, Object object2, Object object3) {
        this.ds2 = object;
        this.ds1 = object2;
        this.cmp = object3;
    }

    public Object invoke() {
        Object object;
        index$merge_data$fn__15378 this_;
        Object d2;
        Object d1 = ((IFn)const__0.getRawRoot()).invoke(this_.ds1);
        int c = ((Comparator)this_.cmp).compare(d1, d2 = ((IFn)const__0.getRawRoot()).invoke(this_.ds2));
        if ((long)c < 0L) {
            Object object2 = d1;
            d1 = null;
            this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2, ((IFn)const__3.getRawRoot()).invoke(this_.cmp, ((IFn)const__4.getRawRoot()).invoke(this_.ds1), this_.ds2));
        } else if ((long)c > 0L) {
            Object object3 = d2;
            d2 = null;
            this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object3, ((IFn)const__3.getRawRoot()).invoke(this_.cmp, this_.ds1, ((IFn)const__4.getRawRoot()).invoke(this_.ds2)));
        } else {
            Keyword keyword = const__6;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object4 = d1;
                d1 = null;
                this_ = null;
                object = ((IFn)const__2.getRawRoot()).invoke(object4, ((IFn)const__3.getRawRoot()).invoke(this_.cmp, ((IFn)const__4.getRawRoot()).invoke(this_.ds1), ((IFn)const__4.getRawRoot()).invoke(this_.ds2)));
            } else {
                object = null;
            }
        }
        return object;
    }
}

