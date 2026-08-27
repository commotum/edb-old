/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class backup$fn__20032
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__1 = RT.keyword((String)"storage", (String)"sse-not-available");
    public static final Var const__2 = RT.var((String)"datomic.require", (String)"require-and-run");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"datomic.fsbackup", (String)"storage-from-uri");

    public static Object invokeStatic(Object uri2, Object sse_QMARK_) {
        Object object = sse_QMARK_;
        sse_QMARK_ = null;
        if (object != null && object != Boolean.FALSE) {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)"Server side encryption not available for file storage");
        }
        Object object2 = uri2;
        uri2 = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)const__3, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$fn__20032.invokeStatic(object3, object4);
    }
}

