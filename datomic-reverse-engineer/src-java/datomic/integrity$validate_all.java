/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.peer.RemoteConnection;
import java.util.Arrays;

public final class integrity$validate_all
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__6;
    public static final Object const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final AFn const__17;
    public static final Var const__19;
    public static final Var const__20;
    public static final Object const__21;
    public static final Var const__22;
    public static final Keyword const__23;
    public static final AFn const__24;
    public static final AFn const__25;
    public static final AFn const__26;
    public static final AFn const__27;
    public static final Keyword const__28;
    public static final Var const__29;
    public static final Var const__30;
    public static final Var const__31;
    public static final Var const__32;
    public static final Var const__33;
    public static final Var const__34;
    public static final Var const__35;
    public static final Var const__36;
    public static final Var const__37;
    public static final Var const__39;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object uri) {
        v0 = uri;
        uri = null;
        uri = ((IFn)integrity$validate_all.const__0.getRawRoot()).invoke(v0);
        cr = ((IFn)integrity$validate_all.const__1.getRawRoot()).invoke(uri);
        conn = ((IFn)integrity$validate_all.const__2.getRawRoot()).invoke(uri);
        db = ((IFn)integrity$validate_all.const__3.getRawRoot()).invoke(conn);
        ((IFn)integrity$validate_all.const__4.getRawRoot()).invoke((Object)"Log dir count", (Object)RT.count((Object)((IFn)integrity$validate_all.const__6.getRawRoot()).invoke(cr, integrity$validate_all.const__7)));
        ((IFn)integrity$validate_all.const__8.getRawRoot()).invoke(db);
        ((IFn)integrity$validate_all.const__9.getRawRoot()).invoke(db);
        ((IFn)integrity$validate_all.const__10.getRawRoot()).invoke(uri);
        ((IFn)integrity$validate_all.const__11.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{integrity$validate_all.const__12, uri}));
        ((IFn)integrity$validate_all.const__4.getRawRoot()).invoke((Object)"\nCrosschecking log representations");
        ((IFn)integrity$validate_all.const__4.getRawRoot()).invoke(((IFn)integrity$validate_all.const__13.getRawRoot()).invoke(((IFn)integrity$validate_all.const__14.getRawRoot()).invoke(uri), (Object)integrity$validate_all.const__17));
        if (Util.identical((Object)((IFn)integrity$validate_all.const__19.getRawRoot()).invoke(db, integrity$validate_all.const__20.getRawRoot()), null)) {
        } else {
            form__20659__auto__22498 = integrity$validate_all.const__21;
            v1 = new Object[4];
            v1[0] = integrity$validate_all.const__23;
            v2 = new Object[8];
            v2[0] = integrity$validate_all.const__24;
            v3 = uri;
            uri = null;
            v2[1] = v3;
            v2[2] = integrity$validate_all.const__25;
            v2[3] = cr;
            v2[4] = integrity$validate_all.const__26;
            v2[5] = conn;
            v2[6] = integrity$validate_all.const__27;
            v2[7] = db;
            v1[1] = RT.mapUniqueKeys((Object[])v2);
            v1[2] = integrity$validate_all.const__28;
            v4 = form__20659__auto__22498;
            form__20659__auto__22498 = null;
            v1[3] = v4;
            error__20660__auto__22499 = ((IFn)integrity$validate_all.const__22.getRawRoot()).invoke((Object)"Assertion failed, see ex-data for details", (Object)RT.mapUniqueKeys((Object[])v1));
            v5 = integrity$validate_all.const__29.get();
            if (v5 != null && v5 != Boolean.FALSE) {
                v6 = error__20660__auto__22499;
                error__20660__auto__22499 = null;
                ((IFn)integrity$validate_all.const__29.get()).invoke(v6);
            } else {
                v7 = error__20660__auto__22499;
                error__20660__auto__22499 = null;
                throw (Throwable)v7;
            }
        }
        ((IFn)integrity$validate_all.const__30.getRawRoot()).invoke(db);
        ((IFn)integrity$validate_all.const__31.getRawRoot()).invoke((Object)"\nValidating fulltext trees");
        v8 = (IFn)integrity$validate_all.const__32.getRawRoot();
        v9 = conn;
        if (Util.classOf((Object)v9) == integrity$validate_all.__cached_class__0) ** GOTO lbl64
        if (!(v9 instanceof RemoteConnection)) {
            v9 = v9;
            integrity$validate_all.__cached_class__0 = Util.classOf((Object)v9);
lbl64:
            // 2 sources

            v10 = integrity$validate_all.const__33.getRawRoot().invoke(v9);
        } else {
            v10 = ((RemoteConnection)v9).get_olookup();
        }
        v8.invoke(db, v10);
        ((IFn)integrity$validate_all.const__31.getRawRoot()).invoke((Object)"\nValidating excisions");
        v11 = db;
        db = null;
        ((IFn)integrity$validate_all.const__34.getRawRoot()).invoke(v11);
        ((IFn)integrity$validate_all.const__31.getRawRoot()).invoke((Object)"\nValidating tx-range");
        v12 = conn;
        conn = null;
        ((IFn)integrity$validate_all.const__35.getRawRoot()).invoke(v12);
        ((IFn)integrity$validate_all.const__31.getRawRoot()).invoke((Object)"\nValidating garbage");
        v13 = cr;
        cr = null;
        gval = ((IFn)integrity$validate_all.const__36.getRawRoot()).invoke(v13, integrity$validate_all.const__37.getRawRoot());
        v14 = integrity$validate_all.__thunk__0__;
        v15 = gval;
        v16 = v14.get(v15);
        if (v14 == v16) {
            integrity$validate_all.__thunk__0__ = integrity$validate_all.__site__0__.fault(v15);
            v16 = integrity$validate_all.__thunk__0__.get(v15);
        }
        if (v16 == null || v16 == Boolean.FALSE) {
            v17 = gval;
            gval = null;
            throw (Throwable)((IFn)integrity$validate_all.const__22.getRawRoot()).invoke((Object)"Invalid garbage seq", v17);
        }
        v18 = gval;
        gval = null;
        ((IFn)integrity$validate_all.const__4.getRawRoot()).invoke(v18);
        return ((IFn)integrity$validate_all.const__39.getRawRoot()).invoke();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$validate_all.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
        const__1 = RT.var((String)"datomic.tools", (String)"connection-resources");
        const__2 = RT.var((String)"datomic.api", (String)"connect");
        const__3 = RT.var((String)"datomic.api", (String)"db");
        const__4 = RT.var((String)"clojure.core", (String)"println");
        const__6 = RT.var((String)"datomic.integrity", (String)"log-dir-entry-seq");
        const__7 = 0L;
        const__8 = RT.var((String)"datomic.integrity", (String)"report-aevt-avet-stats");
        const__9 = RT.var((String)"datomic.integrity", (String)"validate-dir-sorts");
        const__10 = RT.var((String)"datomic.integrity", (String)"validate-log-cli");
        const__11 = RT.var((String)"datomic.integrity", (String)"crosscheck-log-cli");
        const__12 = RT.keyword(null, (String)"uri");
        const__13 = RT.var((String)"clojure.core", (String)"select-keys");
        const__14 = RT.var((String)"datomic.integrity", (String)"crosscheck-log-representations");
        const__17 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"basis-t"), (Object)RT.keyword(null, (String)"index-basis-t"));
        const__19 = RT.var((String)"datomic.tools.index-checks", (String)"unique-collisions");
        const__20 = RT.var((String)"clojure.core", (String)"identity");
        const__21 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"nil?"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern((String)"ic", (String)"unique-collisions"), Symbol.intern(null, (String)"db"), Symbol.intern(null, (String)"identity")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 19}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 13}));
        const__22 = RT.var((String)"clojure.core", (String)"ex-info");
        const__23 = RT.keyword(null, (String)"bindings");
        const__24 = (AFn)Symbol.intern(null, (String)"uri");
        const__25 = (AFn)Symbol.intern(null, (String)"cr");
        const__26 = (AFn)Symbol.intern(null, (String)"conn");
        const__27 = (AFn)Symbol.intern(null, (String)"db");
        const__28 = RT.keyword(null, (String)"form");
        const__29 = RT.var((String)"datomic.assert", (String)"*assert-handler*");
        const__30 = RT.var((String)"datomic.integrity", (String)"crosscheck-indexes");
        const__31 = RT.var((String)"clojure.core", (String)"print");
        const__32 = RT.var((String)"datomic.integrity", (String)"validate-fulltext");
        const__33 = RT.var((String)"datomic.peer", (String)"get-olookup");
        const__34 = RT.var((String)"datomic.integrity", (String)"validate-indexed-excisions");
        const__35 = RT.var((String)"datomic.integrity", (String)"crosscheck-tx-range-with-tx-instant");
        const__36 = RT.var((String)"datomic.integrity", (String)"validate-garbage");
        const__37 = RT.var((String)"datomic.integrity", (String)"progress-dot");
        const__39 = RT.var((String)"clojure.core", (String)"prn");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"valid"));
        __thunk__0__ = __site__0__;
    }
}

