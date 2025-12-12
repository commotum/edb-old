/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$expand_map$fn__13734$fn__13739;
import datomic.db.Attribute;
import java.util.List;
import java.util.Set;

public final class db$expand_map$fn__13734
extends AFunction {
    Object dbid;
    Object db;
    Object local_tempids;
    Object part_reqs;
    public static final Keyword const__3 = RT.keyword((String)"db", (String)"match-partition");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"process-match-partition");
    public static final Keyword const__5 = RT.keyword((String)"db", (String)"force-partition");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"process-force-partition");
    public static final Keyword const__7 = RT.keyword((String)"db", (String)"id");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"forward-attr");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"require-attr");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__18 = RT.keyword(null, (String)"default");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__20 = RT.keyword((String)"db", (String)"add");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"cardinality"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public db$expand_map$fn__13734(Object object, Object object2, Object object3, Object object4) {
        this.dbid = object;
        this.db = object2;
        this.local_tempids = object3;
        this.part_reqs = object4;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object result2, Object p__13733) {
        db$expand_map$fn__13734 this_;
        Object object;
        Object object2 = p__13733;
        p__13733 = null;
        Object vec__13735 = object2;
        Object k = RT.nth((Object)vec__13735, (int)RT.uncheckedIntCast((long)0L), null);
        Object object3 = vec__13735;
        vec__13735 = null;
        Object v = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
        Object G__13738 = k;
        switch (Util.hash((Object)G__13738) >> 2 & 3) {
            case 1: {
                if (G__13738 != const__3) break;
                Object object4 = v;
                v = null;
                ((IFn)const__4.getRawRoot()).invoke(object4, this_.db, this_.part_reqs, this_.local_tempids);
                object = result2;
                return object;
            }
            case 2: {
                if (G__13738 != const__5) break;
                Object object5 = v;
                v = null;
                ((IFn)const__6.getRawRoot()).invoke(object5, this_.db, this_.part_reqs, this_.local_tempids);
                object = result2;
                return object;
            }
            case 3: {
                if (G__13738 != const__7) break;
                object = result2;
                return object;
            }
        }
        Object attr = ((IFn)const__8.getRawRoot()).invoke(k);
        Object attrib = ((IFn)const__9.getRawRoot()).invoke(this_.db, attr);
        Object attrid = ((Attribute)attrib).id();
        Object object6 = attr;
        attr = null;
        Object object7 = k;
        k = null;
        if (Util.equiv((Object)object6, (Object)object7)) {
            Object object8;
            boolean bl;
            IFn iFn = (IFn)const__11.getRawRoot();
            Object object9 = attrid;
            attrid = null;
            db$expand_map$fn__13734$fn__13739 db$expand_map$fn__13734$fn__13739 = new db$expand_map$fn__13734$fn__13739(this_.dbid, this_.db, this_.local_tempids, object9, this_.part_reqs);
            Object object10 = result2;
            result2 = null;
            boolean and__5236__auto__13742 = v instanceof List;
            if (and__5236__auto__13742) {
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object11 = attrib;
                attrib = null;
                Object object12 = iLookupThunk.get(object11);
                if (iLookupThunk == object12) {
                    __thunk__0__ = __site__0__.fault(object11);
                    object12 = __thunk__0__.get(object11);
                }
                bl = Util.equiv((long)36L, (Object)object12);
            } else {
                bl = and__5236__auto__13742;
            }
            if (bl) {
                object8 = v;
                v = null;
            } else if (v instanceof Set) {
                Object object13 = v;
                v = null;
                object8 = ((IFn)const__17.getRawRoot()).invoke(object13);
            } else {
                Keyword keyword = const__18;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object object14 = v;
                    v = null;
                    object8 = Tuple.create((Object)object14);
                } else {
                    object8 = null;
                }
            }
            this_ = null;
            object = iFn.invoke((Object)db$expand_map$fn__13734$fn__13739, object10, object8);
            return object;
        }
        Object object15 = result2;
        result2 = null;
        Object object16 = v;
        v = null;
        Object object17 = attrid;
        attrid = null;
        this_ = null;
        object = ((IFn)const__19.getRawRoot()).invoke(object15, (Object)Tuple.create((Object)const__20, (Object)object16, (Object)object17, (Object)this_.dbid));
        return object;
    }
}

