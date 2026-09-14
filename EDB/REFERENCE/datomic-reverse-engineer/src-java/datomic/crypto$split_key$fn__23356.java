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
import java.security.Key;

public final class crypto$split_key$fn__23356
extends AFunction {
    Object k;
    public static final Keyword const__0 = RT.keyword(null, (String)"fragment");
    public static final Var const__1 = RT.var((String)"datomic.codec", (String)"bytes->string");
    public static final Var const__2 = RT.var((String)"datomic.codec", (String)"encode-64");
    public static final Keyword const__3 = RT.keyword(null, (String)"algorithm");

    public crypto$split_key$fn__23356(Object object) {
        this.k = object;
    }

    public Object invoke(Object b) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__0;
        Object object = b;
        b = null;
        objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object));
        objectArray[2] = const__3;
        objectArray[3] = ((Key)this.k).getAlgorithm();
        return RT.mapUniqueKeys((Object[])objectArray);
    }
}

