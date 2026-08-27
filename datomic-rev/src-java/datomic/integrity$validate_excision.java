/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.integrity$validate_excision$id__22395;

public final class integrity$validate_excision
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.api", (String)"entity-db");
    public static final Var const__2 = RT.var((String)"datomic.excise", (String)"get-before-t");
    public static final Var const__4 = RT.var((String)"datomic.api", (String)"basis-t");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__6 = RT.var((String)"datomic.integrity", (String)"e-ts");
    public static final Var const__10 = RT.var((String)"datomic.api", (String)"as-of");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Keyword const__12 = RT.keyword(null, (String)"a");
    public static final Keyword const__13 = RT.keyword(null, (String)"e");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__15 = RT.var((String)"datomic.api", (String)"datoms");
    public static final Keyword const__16 = RT.keyword(null, (String)"aevt");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__19 = RT.keyword(null, (String)"spec");
    public static final Keyword const__20 = RT.keyword(null, (String)"datoms");
    public static final Keyword const__21 = RT.keyword(null, (String)"t");
    public static final Var const__22 = RT.var((String)"datomic.api", (String)"entity");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"keys");
    public static final Keyword const__27 = RT.keyword(null, (String)"attrs");
    public static final Keyword const__28 = RT.keyword(null, (String)"entity");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"excise"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"id"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword((String)"db.excise", (String)"attrs"));
    static ILookupThunk __thunk__3__ = __site__3__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object spec) {
        db = ((IFn)integrity$validate_excision.const__0.getRawRoot()).invoke(spec);
        id = new integrity$validate_excision$id__22395(db);
        v0 = integrity$validate_excision.__thunk__0__;
        v1 = spec;
        v2 = v0.get(v1);
        if (v0 == v2) {
            integrity$validate_excision.__thunk__0__ = integrity$validate_excision.__site__0__.fault(v1);
            v2 = integrity$validate_excision.__thunk__0__.get(v1);
        }
        target = v2;
        v3 = temp__5455__auto__22400 = ((IFn)integrity$validate_excision.const__2.getRawRoot()).invoke(db, spec);
        if (v3 != null && v3 != Boolean.FALSE) {
            v4 = temp__5455__auto__22400;
            temp__5455__auto__22400 = null;
            v5 = bt = v4;
            bt = null;
            v6 = Numbers.dec((Object)v5);
        } else {
            v6 = ((IFn)integrity$validate_excision.const__4.getRawRoot()).invoke(db);
        }
        before_t = v6;
        v7 = (IFn)integrity$validate_excision.const__5.getRawRoot();
        v8 = (IFn)integrity$validate_excision.const__6.getRawRoot();
        v9 = integrity$validate_excision.__thunk__1__;
        v10 = spec;
        v11 = v9.get(v10);
        if (v9 == v11) {
            integrity$validate_excision.__thunk__1__ = integrity$validate_excision.__site__1__.fault(v10);
            v11 = integrity$validate_excision.__thunk__1__.get(v10);
        }
        spec_t = v7.invoke(v8.invoke(db, v11));
        v12 = before_t;
        before_t = null;
        v13 = spec_t;
        spec_t = null;
        v14 = Numbers.min((Object)v12, (Object)v13);
        v15 = integrity$validate_excision.__thunk__2__;
        v16 = db;
        v17 = v15.get(v16);
        if (v15 == v17) {
            integrity$validate_excision.__thunk__2__ = integrity$validate_excision.__site__2__.fault(v16);
            v17 = integrity$validate_excision.__thunk__2__.get(v16);
        }
        t = Numbers.min((Object)v14, (Object)v17);
        valdb = ((IFn)integrity$validate_excision.const__10.getRawRoot()).invoke(db, t);
        v18 = db;
        db = null;
        v19 = ((IFn)integrity$validate_excision.const__11.getRawRoot()).invoke(v18, ((IFn)id).invoke(target));
        v20 = type = v19 != null && v19 != Boolean.FALSE ? integrity$validate_excision.const__12 : integrity$validate_excision.const__13;
        type = null;
        G__22398 = v20;
        switch (Util.hash((Object)G__22398)) {
            case 1013910569: {
                if (G__22398 == integrity$validate_excision.const__12) {
                    v21 = valdb;
                    valdb = null;
                    v22 = id;
                    id = null;
                    v23 = target;
                    target = null;
                    datoms = ((IFn)integrity$validate_excision.const__14.getRawRoot()).invoke(((IFn)integrity$validate_excision.const__15.getRawRoot()).invoke(v21, (Object)integrity$validate_excision.const__16, ((IFn)v22).invoke(v23)));
                    if (Util.identical((Object)datoms, null)) {
                        v24 = null;
                        break;
                    }
                    v25 = new Object[6];
                    v25[0] = integrity$validate_excision.const__19;
                    v25[1] = spec;
                    v25[2] = integrity$validate_excision.const__20;
                    v26 = datoms;
                    datoms = null;
                    v25[3] = v26;
                    v25[4] = integrity$validate_excision.const__21;
                    v27 = t;
                    t = null;
                    v25[5] = v27;
                    throw (Throwable)((IFn)integrity$validate_excision.const__18.getRawRoot()).invoke((Object)"Found datoms that should have been excised", (Object)RT.mapUniqueKeys((Object[])v25));
                }
                ** GOTO lbl133
            }
            case 1013910832: {
                if (G__22398 == integrity$validate_excision.const__13) {
                    v28 = valdb;
                    valdb = null;
                    v29 = id;
                    id = null;
                    v30 = target;
                    target = null;
                    ent = ((IFn)integrity$validate_excision.const__22.getRawRoot()).invoke(v28, ((IFn)v29).invoke(v30));
                    v31 = (IFn)integrity$validate_excision.const__23.getRawRoot();
                    v32 = integrity$validate_excision.__thunk__3__;
                    v33 = spec;
                    v34 = v32.get(v33);
                    if (v32 == v34) {
                        integrity$validate_excision.__thunk__3__ = integrity$validate_excision.__site__3__.fault(v33);
                        v34 = integrity$validate_excision.__thunk__3__.get(v33);
                    }
                    attrs = v31.invoke((Object)PersistentHashSet.EMPTY, v34);
                    v35 = ((IFn)integrity$validate_excision.const__14.getRawRoot()).invoke(attrs);
                    if (v35 != null && v35 != Boolean.FALSE) {
                        v36 = ((IFn)integrity$validate_excision.const__25.getRawRoot()).invoke(attrs, ((IFn)integrity$validate_excision.const__26.getRawRoot()).invoke(ent));
                        if (v36 != null && v36 != Boolean.FALSE) {
                            v37 = new Object[8];
                            v37[0] = integrity$validate_excision.const__19;
                            v37[1] = spec;
                            v37[2] = integrity$validate_excision.const__27;
                            v38 = attrs;
                            attrs = null;
                            v37[3] = v38;
                            v37[4] = integrity$validate_excision.const__28;
                            v39 = ent;
                            ent = null;
                            v37[5] = v39;
                            v37[6] = integrity$validate_excision.const__21;
                            v40 = t;
                            t = null;
                            v37[7] = v40;
                            throw (Throwable)((IFn)integrity$validate_excision.const__18.getRawRoot()).invoke((Object)"Found entity attributes that should have been excised", (Object)RT.mapUniqueKeys((Object[])v37));
                        }
                        v24 = null;
                        break;
                    }
                    v41 = ((IFn)integrity$validate_excision.const__14.getRawRoot()).invoke(((IFn)integrity$validate_excision.const__26.getRawRoot()).invoke(ent));
                    if (v41 != null && v41 != Boolean.FALSE) {
                        v42 = new Object[6];
                        v42[0] = integrity$validate_excision.const__19;
                        v42[1] = spec;
                        v42[2] = integrity$validate_excision.const__28;
                        v43 = ent;
                        ent = null;
                        v42[3] = v43;
                        v42[4] = integrity$validate_excision.const__21;
                        v44 = t;
                        t = null;
                        v42[5] = v44;
                        throw (Throwable)((IFn)integrity$validate_excision.const__18.getRawRoot()).invoke((Object)"Found entity that should have been excised", (Object)RT.mapUniqueKeys((Object[])v42));
                    }
                    v24 = null;
                    break;
                }
            }
lbl133:
            // 4 sources

            default: {
                v45 = G__22398;
                G__22398 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)integrity$validate_excision.const__29.getRawRoot()).invoke((Object)"No matching clause: ", (Object)v45));
            }
        }
        var0 = null;
        return spec;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$validate_excision.invokeStatic(object2);
    }
}

