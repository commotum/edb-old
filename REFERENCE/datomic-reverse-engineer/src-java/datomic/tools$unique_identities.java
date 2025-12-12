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
import datomic.tools$unique_identities$fn__21779;
import datomic.tools$unique_identities$fn__21781;

public final class tools$unique_identities
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Keyword const__1 = RT.keyword(null, (String)"id");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"datomic.api", (String)"datoms");
    public static final Keyword const__5 = RT.keyword(null, (String)"aevt");
    public static final Keyword const__6 = RT.keyword((String)"db.install", (String)"attribute");

    public static Object invokeStatic(Object db2) {
        tools$unique_identities$fn__21781 tools$unique_identities$fn__21781 = new tools$unique_identities$fn__21781(db2);
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, ((IFn)const__2.getRawRoot()).invoke((Object)new tools$unique_identities$fn__21779(), ((IFn)const__3.getRawRoot()).invoke((Object)tools$unique_identities$fn__21781, ((IFn)const__4.getRawRoot()).invoke(object, (Object)const__5, (Object)const__6))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$unique_identities.invokeStatic(object2);
    }
}

