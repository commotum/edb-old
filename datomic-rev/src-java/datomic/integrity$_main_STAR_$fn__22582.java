/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class integrity$_main_STAR_$fn__22582
extends AFunction {
    Object uri;
    Object k;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"remove");
    public static final Keyword const__1 = RT.keyword(null, (String)"seg");
    public static final Var const__2 = RT.var((String)"datomic.integrity", (String)"fulltext-storage-seq");

    public integrity$_main_STAR_$fn__22582(Object object, Object object2) {
        this.uri = object;
        this.k = object2;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1, ((IFn)const__2.getRawRoot()).invoke(this.uri, this.k));
        }
        catch (Throwable t__22555__auto__2) {
            Object t__22555__auto__2 = null;
            t__22555__auto__2.printStackTrace();
            object = null;
        }
        return object;
    }
}

