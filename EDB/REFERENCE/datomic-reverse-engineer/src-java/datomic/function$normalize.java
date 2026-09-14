/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.function$normalize$fn__11978;
import datomic.function$normalize$fn__11982;
import datomic.function$normalize$vectorize__11980;
import java.util.Arrays;

public final class function$normalize
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"force-map-keywords");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"lang");
    public static final Keyword const__5 = RT.keyword(null, (String)"imports");
    public static final Keyword const__6 = RT.keyword(null, (String)"requires");
    public static final Keyword const__7 = RT.keyword(null, (String)"params");
    public static final Keyword const__8 = RT.keyword(null, (String)"code");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"*print-length*");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"*print-level*");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"*print-meta*");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__20 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"lang"), Symbol.intern(null, (String)"params"), Symbol.intern(null, (String)"code")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 13}));

    public static Object invokeStatic(Object m) {
        Object object;
        Object and__5236__auto__11986;
        Object object2;
        Object object3;
        Object object4 = m;
        m = null;
        Object map__11977 = ((IFn)const__0.getRawRoot()).invoke(object4);
        Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__11977);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__11977;
            map__11977 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__11977;
            map__11977 = null;
        }
        Object map__119772 = object3;
        Object lang = RT.get((Object)map__119772, (Object)const__4);
        Object imports = RT.get((Object)map__119772, (Object)const__5);
        Object requires = RT.get((Object)map__119772, (Object)const__6);
        Object params = RT.get((Object)map__119772, (Object)const__7);
        Object object7 = map__119772;
        map__119772 = null;
        Object code = RT.get((Object)object7, (Object)const__8);
        Object object8 = lang;
        lang = null;
        Object lang2 = ((IFn)const__9.getRawRoot()).invoke(object8);
        Object object9 = params;
        params = null;
        Object params2 = ((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), object9);
        Object object10 = ((IFn)const__12.getRawRoot()).invoke(code);
        if (object10 != null && object10 != Boolean.FALSE) {
            object2 = code;
            code = null;
        } else {
            ((IFn)const__13.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke((Object)const__15, null, (Object)const__16, null, (Object)const__17, (Object)Boolean.TRUE));
            Object object11 = code;
            code = null;
            object2 = ((IFn)new function$normalize$fn__11978(object11)).invoke();
        }
        Object code2 = object2;
        function$normalize$vectorize__11980 vectorize = new function$normalize$vectorize__11980();
        Object object12 = and__5236__auto__11986 = lang2;
        if (object12 != null && object12 != Boolean.FALSE) {
            Object and__5236__auto__11985;
            Object object13 = and__5236__auto__11985 = params2;
            if (object13 != null && object13 != Boolean.FALSE) {
                object = code2;
            } else {
                object = and__5236__auto__11985;
                and__5236__auto__11985 = null;
            }
        } else {
            object = and__5236__auto__11986;
            and__5236__auto__11986 = null;
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__18.getRawRoot()).invoke((Object)"Assert failed: ", (Object)"Must supply lang, params and code", (Object)"\n", ((IFn)const__19.getRawRoot()).invoke(const__20))));
        }
        Object[] objectArray = new Object[10];
        objectArray[0] = const__4;
        Object object14 = lang2;
        lang2 = null;
        objectArray[1] = object14;
        objectArray[2] = const__5;
        Object object15 = imports;
        imports = null;
        objectArray[3] = ((IFn)const__10.getRawRoot()).invoke((Object)vectorize, object15);
        objectArray[4] = const__6;
        function$normalize$vectorize__11980 function$normalize$vectorize__11980 = vectorize;
        function$normalize$vectorize__11980 function$normalize$vectorize__119802 = vectorize;
        vectorize = null;
        Object object16 = requires;
        requires = null;
        objectArray[5] = ((IFn)const__10.getRawRoot()).invoke((Object)function$normalize$vectorize__11980, ((IFn)const__10.getRawRoot()).invoke((Object)new function$normalize$fn__11982((Object)function$normalize$vectorize__119802), object16));
        objectArray[6] = const__7;
        Object object17 = params2;
        params2 = null;
        objectArray[7] = object17;
        objectArray[8] = const__8;
        Object object18 = code2;
        code2 = null;
        objectArray[9] = object18;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return function$normalize.invokeStatic(object2);
    }
}

