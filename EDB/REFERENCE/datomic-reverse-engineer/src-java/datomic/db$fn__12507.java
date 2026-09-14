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
import datomic.db.Symbolish;

public final class db$fn__12507
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Object const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Object const__7;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object x) {
        block9: {
            block10: {
                block8: {
                    v0 = ((IFn)db$fn__12507.const__0.getRawRoot()).invoke(x);
                    if (v0 == null || v0 == Boolean.FALSE) break block8;
                    v1 = and__5236__auto__12509 = ((IFn)db$fn__12507.const__1.getRawRoot()).invoke(x);
                    if (v1 != null && v1 != Boolean.FALSE) {
                        v2 = Util.equiv((char)((String)x).charAt(RT.uncheckedIntCast((long)0L)), (char)((Character)db$fn__12507.const__4).charValue()) ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        v2 = and__5236__auto__12509;
                        and__5236__auto__12509 = null;
                    }
                    if (v2 != null && v2 != Boolean.FALSE) {
                        v3 = x;
                        x = null;
                        v4 = ((IFn)db$fn__12507.const__5.getRawRoot()).invoke(((IFn)db$fn__12507.const__6.getRawRoot()).invoke(v3, db$fn__12507.const__7));
                    } else {
                        v5 = x;
                        x = null;
                        v4 = ((IFn)db$fn__12507.const__8.getRawRoot()).invoke((Object)db$fn__12507.const__9, ((IFn)db$fn__12507.const__10.getRawRoot()).invoke((Object)"Cannot interpret as a keyword: ", v5, (Object)", no leading :"));
                    }
                    break block9;
                }
                v6 = x;
                if (Util.classOf((Object)v6) == db$fn__12507.__cached_class__0) ** GOTO lbl24
                if (!(v6 instanceof Symbolish)) {
                    v6 = v6;
                    db$fn__12507.__cached_class__0 = Util.classOf((Object)v6);
lbl24:
                    // 2 sources

                    v7 = db$fn__12507.const__11.getRawRoot().invoke(v6);
                } else {
                    v7 = ((Symbolish)v6).sym_name();
                }
                v8 = temp__5455__auto__12510 = v7;
                if (v8 == null || v8 == Boolean.FALSE) break block10;
                v9 = temp__5455__auto__12510;
                temp__5455__auto__12510 = null;
                name = v9;
                v10 = (IFn)db$fn__12507.const__5.getRawRoot();
                v11 = x;
                x = null;
                v12 = v11;
                if (Util.classOf((Object)v11) == db$fn__12507.__cached_class__1) ** GOTO lbl40
                if (!(v12 instanceof Symbolish)) {
                    v12 = v12;
                    db$fn__12507.__cached_class__1 = Util.classOf((Object)v12);
lbl40:
                    // 2 sources

                    v13 = db$fn__12507.const__12.getRawRoot().invoke(v12);
                } else {
                    v13 = ((Symbolish)v12).sym_namespace();
                }
                v14 = name;
                name = null;
                v4 = v10.invoke(v13, v14);
                break block9;
            }
            v4 = x;
            var0 = null;
        }
        return v4;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$fn__12507.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"string?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__4 = Character.valueOf(':');
        const__5 = RT.var((String)"clojure.core", (String)"keyword");
        const__6 = RT.var((String)"clojure.core", (String)"subs");
        const__7 = 1L;
        const__8 = RT.var((String)"datomic.error", (String)"arg");
        const__9 = RT.keyword((String)"db.error", (String)"not-a-keyword");
        const__10 = RT.var((String)"clojure.core", (String)"str");
        const__11 = RT.var((String)"datomic.db", (String)"sym-name");
        const__12 = RT.var((String)"datomic.db", (String)"sym-namespace");
    }
}

