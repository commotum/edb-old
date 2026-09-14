/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Util;
import clojure.lang.Var;

public final class cli$shell
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.java.shell", (String)"sh");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"exit");
    public static final Keyword const__6 = RT.keyword(null, (String)"err");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__10 = RT.keyword(null, (String)"args");
    public static final Keyword const__11 = RT.keyword(null, (String)"result");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"out"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(ISeq args) {
        Object object;
        Object map__20728;
        Object object2;
        Object map__207282 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), (Object)args);
        Object object3 = ((IFn)const__2.getRawRoot()).invoke(map__207282);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__207282;
            map__207282 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__3.getRawRoot()).invoke(object4)));
        } else {
            object2 = map__207282;
            map__207282 = null;
        }
        Object m = map__20728 = object2;
        Object exit = RT.get((Object)map__20728, (Object)const__5);
        Object object5 = map__20728;
        map__20728 = null;
        Object err = RT.get((Object)object5, (Object)const__6);
        Object object6 = exit;
        exit = null;
        boolean or__5238__auto__20730 = Util.equiv((long)0L, (Object)object6);
        if (or__5238__auto__20730) {
            object = or__5238__auto__20730 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object object7 = err;
            err = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object7);
        }
        if (object == null || object == Boolean.FALSE) {
            Object[] objectArray = new Object[4];
            objectArray[0] = const__10;
            ISeq iSeq = args;
            args = null;
            objectArray[1] = iSeq;
            objectArray[2] = const__11;
            objectArray[3] = m;
            throw (Throwable)((IFn)const__9.getRawRoot()).invoke((Object)"Shell command failed", (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object8 = m;
        m = null;
        Object object9 = iLookupThunk.get(object8);
        if (iLookupThunk == object9) {
            __thunk__0__ = __site__0__.fault(object8);
            object9 = __thunk__0__.get(object8);
        }
        return object9;
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return cli$shell.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

