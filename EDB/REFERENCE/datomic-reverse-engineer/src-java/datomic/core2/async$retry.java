/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.core2.async$retry$fn__19535;

public final class async$retry
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"f");
    public static final Keyword const__7 = RT.keyword(null, (String)"pred");
    public static final Keyword const__8 = RT.keyword(null, (String)"backoff");
    public static final Keyword const__9 = RT.keyword(null, (String)"ch");
    public static final Var const__10 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__11 = 1L;
    public static final Keyword const__12 = RT.keyword(null, (String)"fail");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__14 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public static Object invokeStatic(ISeq p__19478) {
        Object object;
        ISeq map__19479 = p__19478;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)map__19479);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__1.getRawRoot()).invoke((Object)map__19479);
            if (object3 != null && object3 != Boolean.FALSE) {
                ISeq iSeq = map__19479;
                map__19479 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke((Object)iSeq)));
            } else {
                Object object4 = ((IFn)const__3.getRawRoot()).invoke((Object)map__19479);
                if (object4 != null && object4 != Boolean.FALSE) {
                    ISeq iSeq = map__19479;
                    map__19479 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke((Object)iSeq);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__19479;
            map__19479 = null;
        }
        ISeq map__194792 = object;
        Object f = RT.get((Object)map__194792, (Object)const__6);
        Object pred2 = RT.get((Object)map__194792, (Object)const__7);
        Object backoff = RT.get((Object)map__194792, (Object)const__8);
        Object ch = RT.get((Object)map__194792, (Object)const__9, (Object)((IFn)const__10.getRawRoot()).invoke(const__11));
        Object fail2 = RT.get((Object)map__194792, (Object)const__12, (Object)const__13.getRawRoot());
        Object c__10230__auto__19581 = ((IFn)const__10.getRawRoot()).invoke(const__11);
        Object captured_bindings__10231__auto__19582 = Var.getThreadBindingFrame();
        Object object5 = backoff;
        backoff = null;
        Object object6 = fail2;
        fail2 = null;
        Object object7 = f;
        f = null;
        Object object8 = captured_bindings__10231__auto__19582;
        captured_bindings__10231__auto__19582 = null;
        Object object9 = pred2;
        pred2 = null;
        ISeq iSeq = p__19478;
        p__19478 = null;
        ISeq iSeq2 = map__194792;
        map__194792 = null;
        ((IFn)const__14.getRawRoot()).invoke((Object)new async$retry$fn__19535(object5, object6, object7, ch, c__10230__auto__19581, object8, object9, iSeq, iSeq2));
        Object object10 = ch;
        ch = null;
        return object10;
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return async$retry.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

