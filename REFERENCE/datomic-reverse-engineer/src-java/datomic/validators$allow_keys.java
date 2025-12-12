/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class validators$allow_keys
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.set", (String)"difference");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"hash-set");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"*print-length*");
    public static final Object const__8 = 100L;
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"*print-level*");
    public static final Object const__10 = 10L;
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public static Object invokeStatic(Object m, ISeq ks) {
        ISeq iSeq = ks;
        ks = null;
        Object extras = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(m)), ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), (Object)iSeq));
        Object object = ((IFn)const__4.getRawRoot()).invoke(extras);
        if (object != null && object != Boolean.FALSE) {
            ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__7, const__8, (Object)const__9, const__10));
            try {
                Object object2 = extras;
                extras = null;
                Object object3 = m;
                m = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__11.getRawRoot()).invoke((Object)"Unexpected keys ", object2, (Object)" in ", object3));
            }
            catch (Throwable throwable) {
                ((IFn)const__12.getRawRoot()).invoke();
                throw throwable;
            }
        }
        return null;
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return validators$allow_keys.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

