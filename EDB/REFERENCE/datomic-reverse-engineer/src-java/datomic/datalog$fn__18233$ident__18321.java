/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog.DbRel;

public final class datalog$fn__18233$ident__18321
extends AFunction {
    Object dbrel;
    Object db;
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"normalize-kw");
    public static final Keyword const__5 = RT.keyword(null, (String)"else");

    public datalog$fn__18233$ident__18321(Object object, Object object2) {
        this.dbrel = object;
        this.db = object2;
    }

    public Object invoke(Object i, Object v) {
        Object object;
        datalog$fn__18233$ident__18321 this_;
        boolean and__5236__auto__18323;
        boolean or__5238__auto__18324 = Numbers.lt((Object)i, (long)2L);
        Object object2 = or__5238__auto__18324 ? (or__5238__auto__18324 ? Boolean.TRUE : Boolean.FALSE) : ((and__5236__auto__18323 = Util.equiv((Object)i, (long)2L)) ? ((DbRel)this_.dbrel).isref : (and__5236__auto__18323 ? Boolean.TRUE : Boolean.FALSE));
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = v;
            v = null;
            this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(this_.db, object3);
        } else {
            Object object4 = i;
            i = null;
            boolean and__5236__auto__18325 = Util.equiv((Object)object4, (long)2L);
            Object object5 = and__5236__auto__18325 ? ((DbRel)this_.dbrel).iskey : (and__5236__auto__18325 ? Boolean.TRUE : Boolean.FALSE);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = v;
                v = null;
                this_ = null;
                object = ((IFn)const__4.getRawRoot()).invoke(object6);
            } else {
                Keyword keyword = const__5;
                if (keyword != null && keyword != Boolean.FALSE) {
                    object = v;
                    v = null;
                } else {
                    object = null;
                }
            }
        }
        return object;
    }
}

