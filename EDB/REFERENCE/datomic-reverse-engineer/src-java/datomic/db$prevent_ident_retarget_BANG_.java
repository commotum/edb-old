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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Database;

public final class db$prevent_ident_retarget_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not=");
    public static final Var const__3 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__4 = RT.keyword((String)"db.error", (String)"cannot-retarget-ident");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"constituents"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object e, Object v) {
        Object object;
        Object object2;
        Object past_e;
        Object and__5236__auto__13038;
        Object object3 = and__5236__auto__13038 = (past_e = ((Database)db2).entid(v));
        if (object3 != null && object3 != Boolean.FALSE) {
            Object and__5236__auto__13037;
            Object object4 = and__5236__auto__13037 = ((IFn)const__0.getRawRoot()).invoke(past_e, e);
            if (object4 != null && object4 != Boolean.FALSE) {
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object5 = db2;
                Object object6 = iLookupThunk.get(object5);
                if (iLookupThunk == object6) {
                    __thunk__0__ = __site__0__.fault(object5);
                    object6 = __thunk__0__.get(object5);
                }
                object2 = RT.get((Object)object6, (Object)past_e);
            } else {
                object2 = and__5236__auto__13037;
                and__5236__auto__13037 = null;
            }
        } else {
            object2 = and__5236__auto__13038;
            and__5236__auto__13038 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object7 = v;
            v = null;
            Object object8 = e;
            e = null;
            Object object9 = ((IFn)const__6.getRawRoot()).invoke(db2, object8);
            Object object10 = db2;
            db2 = null;
            Object object11 = past_e;
            past_e = null;
            object = ((IFn)const__3.getRawRoot()).invoke((Object)const__4, ((IFn)const__5.getRawRoot()).invoke((Object)"Ident ", object7, (Object)" cannot be used for entity ", object9, (Object)", already used for ", ((IFn)const__6.getRawRoot()).invoke(object10, object11)));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$prevent_ident_retarget_BANG_.invokeStatic(object4, object5, object6);
    }
}

