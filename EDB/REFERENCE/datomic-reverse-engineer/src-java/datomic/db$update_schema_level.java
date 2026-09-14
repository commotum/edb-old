/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;

public final class db$update_schema_level
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"schema-level");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"MIN_SCHEMA_LEVEL");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"bootstrap-txes");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"kw");
    public static final Var const__14 = RT.var((String)"datomic.db", (String)"on-schema-level");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"ident"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2) {
        Object object;
        block4: {
            Object level = RT.get((Object)db2, (Object)const__1, (Object)const__2.getRawRoot());
            Object object2 = db2;
            db2 = null;
            Object db3 = object2;
            while (true) {
                Number nlevel;
                Object kw;
                if (Util.equiv((Object)level, (long)RT.count((Object)const__5.getRawRoot()))) {
                    Object object3 = db3;
                    db3 = null;
                    Object object4 = level;
                    level = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(object3, (Object)const__1, object4);
                    break block4;
                }
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object5 = ((IFn)const__8.getRawRoot()).invoke(RT.nth((Object)const__5.getRawRoot(), (int)RT.uncheckedIntCast((Object)((Number)level))));
                Object object6 = iLookupThunk.get(object5);
                if (iLookupThunk == object6) {
                    __thunk__0__ = __site__0__.fault(object5);
                    object6 = __thunk__0__.get(object5);
                }
                Object object7 = kw = object6;
                if (object7 == null || object7 == Boolean.FALSE) {
                    throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke((Object)const__12))));
                }
                Object object8 = kw;
                kw = null;
                Object object9 = ((Database)db3).entid(object8);
                if (object9 == null || object9 == Boolean.FALSE) break;
                Object object10 = level;
                level = null;
                Number number = nlevel = Numbers.unchecked_inc((Object)object10);
                Object object11 = db3;
                db3 = null;
                Number number2 = nlevel;
                nlevel = null;
                db3 = ((IFn)const__14.getRawRoot()).invoke(object11, (Object)number2);
                level = number;
            }
            Object object12 = db3;
            db3 = null;
            Object object13 = level;
            level = null;
            object = ((IFn)const__6.getRawRoot()).invoke(object12, (Object)const__1, object13);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$update_schema_level.invokeStatic(object2);
    }
}

