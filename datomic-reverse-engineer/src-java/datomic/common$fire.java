/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class common$fire
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"clojure.core", (String)"future");
    public static final AFn const__4 = (AFn)Symbol.intern(null, (String)"try");
    public static final AFn const__5 = (AFn)Symbol.intern(null, (String)"catch");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"java.lang.Throwable");
    public static final AFn const__7 = (AFn)Symbol.intern(null, (String)"t__9147__auto__");
    public static final AFn const__8 = (AFn)Symbol.intern((String)"datomic.slf4j", (String)"warn");
    public static final AFn const__9 = (AFn)Symbol.intern((String)"datomic.monitor", (String)"alarm");
    public static final Keyword const__10 = RT.keyword(null, (String)"UnhandledException");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"throw");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, ISeq body) {
        ISeq iSeq = body;
        body = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__4), (Object)iSeq, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__5), ((IFn)const__2.getRawRoot()).invoke((Object)const__6), ((IFn)const__2.getRawRoot()).invoke((Object)const__7), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__8), ((IFn)const__2.getRawRoot()).invoke((Object)"error executing future"), ((IFn)const__2.getRawRoot()).invoke((Object)const__7)))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__9), ((IFn)const__2.getRawRoot()).invoke((Object)const__10)))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__11), ((IFn)const__2.getRawRoot()).invoke((Object)const__7))))))))))));
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return common$fire.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

