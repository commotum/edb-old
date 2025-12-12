/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookup
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class AttrInfo
implements ILookup,
datomic.Attribute,
IType {
    public final Object attr;
    public final Object vtype_kw;
    public static final Keyword const__0 = RT.keyword(null, (String)"cardinality");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"=");
    public static final Object const__2 = 35L;
    public static final Keyword const__3 = RT.keyword((String)"db.cardinality", (String)"one");
    public static final Object const__4 = 36L;
    public static final Keyword const__5 = RT.keyword((String)"db.cardinality", (String)"many");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__7 = RT.keyword(null, (String)"no-history");
    public static final Keyword const__9 = RT.keyword(null, (String)"noHistory");
    public static final Keyword const__10 = RT.keyword(null, (String)"id");
    public static final Keyword const__11 = RT.keyword(null, (String)"is-component");
    public static final Keyword const__12 = RT.keyword(null, (String)"isComponent");
    public static final Keyword const__13 = RT.keyword(null, (String)"value-type");
    public static final Keyword const__14 = RT.keyword(null, (String)"fulltext");
    public static final Keyword const__15 = RT.keyword(null, (String)"unique");
    public static final Object const__16 = 38L;
    public static final Keyword const__17 = RT.keyword((String)"db.unique", (String)"identity");
    public static final Object const__18 = 37L;
    public static final Keyword const__19 = RT.keyword((String)"db.unique", (String)"value");
    public static final Keyword const__20 = RT.keyword(null, (String)"has-avet");
    public static final Keyword const__21 = RT.keyword(null, (String)"indexed");
    public static final Keyword const__22 = RT.keyword(null, (String)"index");
    public static final Keyword const__23 = RT.keyword(null, (String)"ident");
    public static final Keyword const__25 = RT.keyword(null, (String)"valueType");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"cardinality"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"unique"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"kw"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"unique"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"kw"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"cardinality"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"ident"));
    static ILookupThunk __thunk__9__ = __site__9__;
    static final KeywordLookupSite __site__10__ = new KeywordLookupSite(RT.keyword(null, (String)"value-type"));
    static ILookupThunk __thunk__10__ = __site__10__;
    static final KeywordLookupSite __site__11__ = new KeywordLookupSite(RT.keyword(null, (String)"cardinality"));
    static ILookupThunk __thunk__11__ = __site__11__;
    static final KeywordLookupSite __site__12__ = new KeywordLookupSite(RT.keyword(null, (String)"is-component"));
    static ILookupThunk __thunk__12__ = __site__12__;
    static final KeywordLookupSite __site__13__ = new KeywordLookupSite(RT.keyword(null, (String)"unique"));
    static ILookupThunk __thunk__13__ = __site__13__;
    static final KeywordLookupSite __site__14__ = new KeywordLookupSite(RT.keyword(null, (String)"indexed"));
    static ILookupThunk __thunk__14__ = __site__14__;
    static final KeywordLookupSite __site__15__ = new KeywordLookupSite(RT.keyword(null, (String)"has-avet"));
    static ILookupThunk __thunk__15__ = __site__15__;
    static final KeywordLookupSite __site__16__ = new KeywordLookupSite(RT.keyword(null, (String)"no-history"));
    static ILookupThunk __thunk__16__ = __site__16__;
    static final KeywordLookupSite __site__17__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext"));
    static ILookupThunk __thunk__17__ = __site__17__;

    public AttrInfo(Object object, Object object2) {
        this.attr = object;
        this.vtype_kw = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"attr")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Attribute")})), (Object)Symbol.intern(null, (String)"vtype-kw"));
    }

    public boolean hasFulltext() {
        ILookupThunk iLookupThunk = __thunk__17__;
        AttrInfo attrInfo = this;
        Object object = iLookupThunk.get((Object)attrInfo);
        if (iLookupThunk == object) {
            __thunk__17__ = __site__17__.fault((Object)attrInfo);
            object = __thunk__17__.get((Object)attrInfo);
        }
        return (Boolean)object;
    }

    public boolean hasNoHistory() {
        ILookupThunk iLookupThunk = __thunk__16__;
        AttrInfo attrInfo = this;
        Object object = iLookupThunk.get((Object)attrInfo);
        if (iLookupThunk == object) {
            __thunk__16__ = __site__16__.fault((Object)attrInfo);
            object = __thunk__16__.get((Object)attrInfo);
        }
        return (Boolean)object;
    }

    public boolean hasAVET() {
        ILookupThunk iLookupThunk = __thunk__15__;
        AttrInfo attrInfo = this;
        Object object = iLookupThunk.get((Object)attrInfo);
        if (iLookupThunk == object) {
            __thunk__15__ = __site__15__.fault((Object)attrInfo);
            object = __thunk__15__.get((Object)attrInfo);
        }
        return (Boolean)object;
    }

    public boolean isIndexed() {
        ILookupThunk iLookupThunk = __thunk__14__;
        AttrInfo attrInfo = this;
        Object object = iLookupThunk.get((Object)attrInfo);
        if (iLookupThunk == object) {
            __thunk__14__ = __site__14__.fault((Object)attrInfo);
            object = __thunk__14__.get((Object)attrInfo);
        }
        return (Boolean)object;
    }

    public Object unique() {
        ILookupThunk iLookupThunk = __thunk__13__;
        AttrInfo attrInfo = this;
        Object object = iLookupThunk.get((Object)attrInfo);
        if (iLookupThunk == object) {
            __thunk__13__ = __site__13__.fault((Object)attrInfo);
            object = __thunk__13__.get((Object)attrInfo);
        }
        return object;
    }

    public boolean isComponent() {
        ILookupThunk iLookupThunk = __thunk__12__;
        AttrInfo attrInfo = this;
        Object object = iLookupThunk.get((Object)attrInfo);
        if (iLookupThunk == object) {
            __thunk__12__ = __site__12__.fault((Object)attrInfo);
            object = __thunk__12__.get((Object)attrInfo);
        }
        return (Boolean)object;
    }

    public Object cardinality() {
        ILookupThunk iLookupThunk = __thunk__11__;
        AttrInfo attrInfo = this;
        Object object = iLookupThunk.get((Object)attrInfo);
        if (iLookupThunk == object) {
            __thunk__11__ = __site__11__.fault((Object)attrInfo);
            object = __thunk__11__.get((Object)attrInfo);
        }
        return object;
    }

    public Object valueType() {
        ILookupThunk iLookupThunk = __thunk__10__;
        AttrInfo attrInfo = this;
        Object object = iLookupThunk.get((Object)attrInfo);
        if (iLookupThunk == object) {
            __thunk__10__ = __site__10__.fault((Object)attrInfo);
            object = __thunk__10__.get((Object)attrInfo);
        }
        return object;
    }

    public Object ident() {
        ILookupThunk iLookupThunk = __thunk__9__;
        AttrInfo attrInfo = this;
        Object object = iLookupThunk.get((Object)attrInfo);
        if (iLookupThunk == object) {
            __thunk__9__ = __site__9__.fault((Object)attrInfo);
            object = __thunk__9__.get((Object)attrInfo);
        }
        return object;
    }

    public Object id() {
        ILookupThunk iLookupThunk = __thunk__8__;
        AttrInfo attrInfo = this;
        Object object = iLookupThunk.get((Object)attrInfo);
        if (iLookupThunk == object) {
            __thunk__8__ = __site__8__.fault((Object)attrInfo);
            object = __thunk__8__.get((Object)attrInfo);
        }
        return object;
    }

    /*
     * Unable to fully structure code
     */
    public Object valAt(Object k, Object not_found) {
        v0 = k;
        k = null;
        G__12720 = v0;
        switch (Util.hash((Object)G__12720) >> 8 & 15) {
            case 1: {
                if (G__12720 == AttrInfo.const__11) {
                    this = null;
                    v1 = RT.get((Object)this.attr, (Object)AttrInfo.const__12, (Object)Boolean.FALSE);
                    break;
                }
                ** GOTO lbl114
            }
            case 2: {
                if (G__12720 == AttrInfo.const__7) {
                    this = null;
                    v1 = RT.get((Object)this.attr, (Object)AttrInfo.const__9, (Object)Boolean.FALSE);
                    break;
                }
                ** GOTO lbl114
            }
            case 3: {
                if (G__12720 == AttrInfo.const__20) {
                    v1 = ((Attribute)this.attr).hasAVET();
                    break;
                }
                ** GOTO lbl114
            }
            case 4: {
                if (G__12720 == AttrInfo.const__15) {
                    pred__12721 = AttrInfo.const__1.getRawRoot();
                    v2 = AttrInfo.__thunk__4__;
                    v3 = this.attr;
                    v4 = v2.get(v3);
                    if (v2 == v4) {
                        AttrInfo.__thunk__4__ = AttrInfo.__site__4__.fault(v3);
                        v4 = AttrInfo.__thunk__4__.get(v3);
                    }
                    expr__12722 = v4;
                    v5 = ((IFn)pred__12721).invoke(AttrInfo.const__16, expr__12722);
                    if (v5 != null && v5 != Boolean.FALSE) {
                        v1 = AttrInfo.const__17;
                        break;
                    }
                    v6 = ((IFn)pred__12721).invoke(AttrInfo.const__18, expr__12722);
                    if (v6 != null && v6 != Boolean.FALSE) {
                        v1 = AttrInfo.const__19;
                        break;
                    }
                    v7 = pred__12721;
                    pred__12721 = null;
                    v8 = ((IFn)v7).invoke(null, expr__12722);
                    if (v8 != null && v8 != Boolean.FALSE) {
                        v1 = null;
                        break;
                    }
                    v9 = expr__12722;
                    expr__12722 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)AttrInfo.const__6.getRawRoot()).invoke((Object)"No matching clause: ", v9));
                }
                ** GOTO lbl114
            }
            case 7: {
                if (G__12720 == AttrInfo.const__25) {
                    v1 = this.vtype_kw;
                    break;
                }
                ** GOTO lbl114
            }
            case 8: {
                if (G__12720 == AttrInfo.const__10) {
                    v10 = AttrInfo.__thunk__5__;
                    v11 = this.attr;
                    v1 = v10.get(v11);
                    if (v10 != v1) break;
                    AttrInfo.__thunk__5__ = AttrInfo.__site__5__.fault(v11);
                    v1 = AttrInfo.__thunk__5__.get(v11);
                    break;
                }
                ** GOTO lbl114
            }
            case 9: {
                if (G__12720 == AttrInfo.const__23) {
                    v12 = AttrInfo.__thunk__6__;
                    v13 = this.attr;
                    v1 = v12.get(v13);
                    if (v12 != v1) break;
                    AttrInfo.__thunk__6__ = AttrInfo.__site__6__.fault(v13);
                    v1 = AttrInfo.__thunk__6__.get(v13);
                    break;
                }
                ** GOTO lbl114
            }
            case 10: {
                if (G__12720 == AttrInfo.const__21) {
                    this = null;
                    v1 = RT.get((Object)this.attr, (Object)AttrInfo.const__22, (Object)Boolean.FALSE);
                    break;
                }
                ** GOTO lbl114
            }
            case 12: {
                if (G__12720 == AttrInfo.const__14) {
                    this = null;
                    v1 = RT.get((Object)this.attr, (Object)AttrInfo.const__14, (Object)Boolean.FALSE);
                    break;
                }
                ** GOTO lbl114
            }
            case 14: {
                if (G__12720 == AttrInfo.const__0) {
                    pred__12723 = AttrInfo.const__1.getRawRoot();
                    v14 = AttrInfo.__thunk__7__;
                    v15 = this.attr;
                    v16 = v14.get(v15);
                    if (v14 == v16) {
                        AttrInfo.__thunk__7__ = AttrInfo.__site__7__.fault(v15);
                        v16 = AttrInfo.__thunk__7__.get(v15);
                    }
                    expr__12724 = v16;
                    v17 = ((IFn)pred__12723).invoke(AttrInfo.const__2, expr__12724);
                    if (v17 != null && v17 != Boolean.FALSE) {
                        v1 = AttrInfo.const__3;
                        break;
                    }
                    v18 = pred__12723;
                    pred__12723 = null;
                    v19 = ((IFn)v18).invoke(AttrInfo.const__4, expr__12724);
                    if (v19 != null && v19 != Boolean.FALSE) {
                        v1 = AttrInfo.const__5;
                        break;
                    }
                    v20 = expr__12724;
                    expr__12724 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)AttrInfo.const__6.getRawRoot()).invoke((Object)"No matching clause: ", v20));
                }
            }
lbl114:
            // 12 sources

            default: {
                v1 = not_found;
                var2_2 = null;
            }
        }
        return v1;
    }

    /*
     * Unable to fully structure code
     */
    public Object valAt(Object k) {
        v0 = k;
        k = null;
        G__12715 = v0;
        switch (Util.hash((Object)G__12715) >> 1 & 31) {
            case 5: {
                if (G__12715 == AttrInfo.const__0) {
                    pred__12716 = AttrInfo.const__1.getRawRoot();
                    v1 = AttrInfo.__thunk__0__;
                    v2 = this.attr;
                    v3 = v1.get(v2);
                    if (v1 == v3) {
                        AttrInfo.__thunk__0__ = AttrInfo.__site__0__.fault(v2);
                        v3 = AttrInfo.__thunk__0__.get(v2);
                    }
                    expr__12717 = v3;
                    v4 = ((IFn)pred__12716).invoke(AttrInfo.const__2, expr__12717);
                    if (v4 != null && v4 != Boolean.FALSE) {
                        v5 = AttrInfo.const__3;
                        break;
                    }
                    v6 = pred__12716;
                    pred__12716 = null;
                    v7 = ((IFn)v6).invoke(AttrInfo.const__4, expr__12717);
                    if (v7 != null && v7 != Boolean.FALSE) {
                        v5 = AttrInfo.const__5;
                        break;
                    }
                    v8 = expr__12717;
                    expr__12717 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)AttrInfo.const__6.getRawRoot()).invoke((Object)"No matching clause: ", v8));
                }
                ** GOTO lbl114
            }
            case 6: {
                if (G__12715 == AttrInfo.const__7) {
                    this = null;
                    v5 = RT.get((Object)this.attr, (Object)AttrInfo.const__9, (Object)Boolean.FALSE);
                    break;
                }
                ** GOTO lbl114
            }
            case 14: {
                if (G__12715 == AttrInfo.const__10) {
                    v9 = AttrInfo.__thunk__1__;
                    v10 = this.attr;
                    v5 = v9.get(v10);
                    if (v9 != v5) break;
                    AttrInfo.__thunk__1__ = AttrInfo.__site__1__.fault(v10);
                    v5 = AttrInfo.__thunk__1__.get(v10);
                    break;
                }
                ** GOTO lbl114
            }
            case 15: {
                if (G__12715 == AttrInfo.const__11) {
                    this = null;
                    v5 = RT.get((Object)this.attr, (Object)AttrInfo.const__12, (Object)Boolean.FALSE);
                    break;
                }
                ** GOTO lbl114
            }
            case 16: {
                if (G__12715 == AttrInfo.const__13) {
                    v5 = this.vtype_kw;
                    break;
                }
                ** GOTO lbl114
            }
            case 22: {
                if (G__12715 == AttrInfo.const__14) {
                    this = null;
                    v5 = RT.get((Object)this.attr, (Object)AttrInfo.const__14, (Object)Boolean.FALSE);
                    break;
                }
                ** GOTO lbl114
            }
            case 26: {
                if (G__12715 == AttrInfo.const__15) {
                    pred__12718 = AttrInfo.const__1.getRawRoot();
                    v11 = AttrInfo.__thunk__2__;
                    v12 = this.attr;
                    v13 = v11.get(v12);
                    if (v11 == v13) {
                        AttrInfo.__thunk__2__ = AttrInfo.__site__2__.fault(v12);
                        v13 = AttrInfo.__thunk__2__.get(v12);
                    }
                    expr__12719 = v13;
                    v14 = ((IFn)pred__12718).invoke(AttrInfo.const__16, expr__12719);
                    if (v14 != null && v14 != Boolean.FALSE) {
                        v5 = AttrInfo.const__17;
                        break;
                    }
                    v15 = ((IFn)pred__12718).invoke(AttrInfo.const__18, expr__12719);
                    if (v15 != null && v15 != Boolean.FALSE) {
                        v5 = AttrInfo.const__19;
                        break;
                    }
                    v16 = pred__12718;
                    pred__12718 = null;
                    v17 = ((IFn)v16).invoke(null, expr__12719);
                    if (v17 != null && v17 != Boolean.FALSE) {
                        v5 = null;
                        break;
                    }
                    v18 = expr__12719;
                    expr__12719 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)AttrInfo.const__6.getRawRoot()).invoke((Object)"No matching clause: ", v18));
                }
                ** GOTO lbl114
            }
            case 29: {
                if (G__12715 == AttrInfo.const__20) {
                    v5 = ((Attribute)this.attr).hasAVET();
                    break;
                }
                ** GOTO lbl114
            }
            case 30: {
                if (G__12715 == AttrInfo.const__21) {
                    this = null;
                    v5 = RT.get((Object)this.attr, (Object)AttrInfo.const__22, (Object)Boolean.FALSE);
                    break;
                }
                ** GOTO lbl114
            }
            case 31: {
                if (G__12715 == AttrInfo.const__23) {
                    v19 = AttrInfo.__thunk__3__;
                    v20 = this.attr;
                    v5 = v19.get(v20);
                    if (v19 != v5) break;
                    AttrInfo.__thunk__3__ = AttrInfo.__site__3__.fault(v20);
                    v5 = AttrInfo.__thunk__3__.get(v20);
                    break;
                }
            }
lbl114:
            // 12 sources

            default: {
                v21 = G__12715;
                G__12715 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)AttrInfo.const__6.getRawRoot()).invoke((Object)"No matching clause: ", v21));
            }
        }
        return v5;
    }
}

