/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$safe_compile_function$fn__13242;
import datomic.db$safe_compile_function$fn__13244;
import datomic.db$safe_compile_function$try_compile__13238;

public final class db$safe_compile_function
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Keyword const__2 = RT.keyword((String)"db.lang", (String)"clojure");
    public static final Keyword const__3 = RT.keyword((String)"db.lang", (String)"java");
    public static final Keyword const__4 = RT.keyword(null, (String)"else");
    public static final Var const__5 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__6 = RT.keyword((String)"db.error", (String)"source-lang-not-supported");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"resolve-kw");

    public static Object invokeStatic(Object db2, Object lang, Object code) {
        Object object;
        db$safe_compile_function$try_compile__13238 try_compile = new db$safe_compile_function$try_compile__13238();
        if (Util.equiv((Object)lang, (Object)((IFn)const__1.getRawRoot()).invoke(db2, (Object)const__2))) {
            db$safe_compile_function$try_compile__13238 db$safe_compile_function$try_compile__13238 = try_compile;
            try_compile = null;
            Object object2 = code;
            code = null;
            object = ((IFn)db$safe_compile_function$try_compile__13238).invoke((Object)new db$safe_compile_function$fn__13242(object2));
        } else if (Util.equiv((Object)lang, (Object)((IFn)const__1.getRawRoot()).invoke(db2, (Object)const__3))) {
            db$safe_compile_function$try_compile__13238 db$safe_compile_function$try_compile__13238 = try_compile;
            try_compile = null;
            Object object3 = code;
            code = null;
            object = ((IFn)db$safe_compile_function$try_compile__13238).invoke((Object)new db$safe_compile_function$fn__13244(object3));
        } else {
            Keyword keyword = const__4;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object4 = db2;
                db2 = null;
                Object object5 = lang;
                lang = null;
                object = ((IFn)const__5.getRawRoot()).invoke((Object)const__6, ((IFn)const__7.getRawRoot()).invoke((Object)"Source lang: ", ((IFn)const__8.getRawRoot()).invoke(object4, object5), (Object)" not supported yet"));
            } else {
                object = null;
            }
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
        return db$safe_compile_function.invokeStatic(object4, object5, object6);
    }
}

