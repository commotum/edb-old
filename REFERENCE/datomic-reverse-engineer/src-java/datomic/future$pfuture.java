/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IObj
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
import clojure.lang.IObj;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class future$pfuture
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"meta");
    public static final Keyword const__2 = RT.keyword(null, (String)"file");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*file*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__10 = (AFn)Symbol.intern(null, (String)"f__10267__auto__");
    public static final AFn const__11 = (AFn)Symbol.intern((String)"datomic.future", (String)"-future-with-channel-impl");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final AFn const__13 = (AFn)((IObj)Symbol.intern(null, (String)"fn*")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"once"), Boolean.TRUE}));
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Keyword const__15 = RT.keyword(null, (String)"once");
    public static final AFn const__16 = (AFn)Symbol.intern(null, (String)"ch__10268__auto__");
    public static final AFn const__17 = (AFn)Symbol.intern((String)"datomic.future", (String)"get-channel");
    public static final AFn const__18 = (AFn)Symbol.intern((String)"datomic.future", (String)"add-bounding-warning");
    public static final AFn const__19 = (AFn)Symbol.intern((String)"clojure.core", (String)"deref");
    public static final AFn const__20 = (AFn)Symbol.intern((String)"datomic.future", (String)"bounding-warn-seconds");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object exec, ISeq body) {
        Object object = _AMPERSAND_form;
        _AMPERSAND_form = null;
        Object context = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object), (Object)const__2, const__3.get());
        Object object2 = exec;
        exec = null;
        ISeq iSeq = body;
        body = null;
        Object object3 = context;
        context = null;
        return ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__7), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__10), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__11), ((IFn)const__6.getRawRoot()).invoke(object2), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke((Object)const__13, ((IFn)const__8.getRawRoot()).invoke(const__14.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__15), ((IFn)const__6.getRawRoot()).invoke((Object)Boolean.TRUE)))))), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke()))), (Object)iSeq)))))), ((IFn)const__6.getRawRoot()).invoke((Object)const__16), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__17), ((IFn)const__6.getRawRoot()).invoke((Object)const__10)))))))), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__18), ((IFn)const__6.getRawRoot()).invoke((Object)const__16), ((IFn)const__6.getRawRoot()).invoke(object3), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__19), ((IFn)const__6.getRawRoot()).invoke((Object)const__20))))))), ((IFn)const__6.getRawRoot()).invoke((Object)const__10)));
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return future$pfuture.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

