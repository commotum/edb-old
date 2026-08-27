/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.function$construct$fn__11989;
import datomic.function$construct$fn__11991;
import datomic.function.Function;

public final class function$construct
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.function", (String)"normalize");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"lang");
    public static final Keyword const__5 = RT.keyword(null, (String)"imports");
    public static final Keyword const__6 = RT.keyword(null, (String)"requires");
    public static final Keyword const__7 = RT.keyword(null, (String)"params");
    public static final Keyword const__8 = RT.keyword(null, (String)"code");
    public static final Keyword const__9 = RT.keyword(null, (String)"java");
    public static final Keyword const__10 = RT.keyword(null, (String)"clojure");
    public static final Var const__11 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__12 = RT.keyword((String)"db.error", (String)"source-lang-not-supported");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"str");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object m) {
        Object object;
        Object code;
        Object params;
        Object requires;
        Object imports;
        Object lang;
        block6: {
            Object object2;
            Object object3 = m;
            m = null;
            Object map__11987 = ((IFn)const__0.getRawRoot()).invoke(object3);
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__11987);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = map__11987;
                map__11987 = null;
                object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object5)));
            } else {
                object2 = map__11987;
                map__11987 = null;
            }
            Object map__119872 = object2;
            lang = RT.get((Object)map__119872, (Object)const__4);
            imports = RT.get((Object)map__119872, (Object)const__5);
            requires = RT.get((Object)map__119872, (Object)const__6);
            params = RT.get((Object)map__119872, (Object)const__7);
            Object object6 = map__119872;
            map__119872 = null;
            code = RT.get((Object)object6, (Object)const__8);
            Object object7 = lang;
            switch (Util.hash((Object)object7) >> 0 & 1) {
                case 0: {
                    if (object7 != const__9) break;
                    object = new Delay((IFn)new function$construct$fn__11989(params, code));
                    break block6;
                }
                case 1: {
                    if (object7 != const__10) break;
                    object = new Delay((IFn)new function$construct$fn__11991(params, requires, code, imports));
                    break block6;
                }
            }
            object = ((IFn)const__11.getRawRoot()).invoke((Object)const__12, ((IFn)const__13.getRawRoot()).invoke((Object)"Source lang: ", lang, (Object)" not supported yet."));
        }
        Object object8 = object;
        Object object9 = lang;
        lang = null;
        Object object10 = imports;
        imports = null;
        Object object11 = requires;
        requires = null;
        Object object12 = params;
        params = null;
        return new Function(object9, object10, object11, object12, code, object8);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return function$construct.invokeStatic(object2);
    }
}

