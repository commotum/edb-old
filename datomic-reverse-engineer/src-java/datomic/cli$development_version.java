/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class cli$development_version
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"read-string");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"slurp");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"version-prefix");
    public static final Var const__6 = RT.var((String)"clojure.string", (String)"trimr");
    public static final Var const__7 = RT.var((String)"datomic.cli", (String)"shell");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(ISeq _) {
        Object map__20731;
        Object object;
        Object map__207312 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)"pom.clj"));
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(map__207312);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = map__207312;
            map__207312 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__3.getRawRoot()).invoke(object3)));
        } else {
            object = map__207312;
            map__207312 = null;
        }
        Object object4 = map__20731 = object;
        map__20731 = null;
        Object version_prefix = RT.get((Object)object4, (Object)const__5);
        Object revision = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)"build/revision"));
        Object object5 = version_prefix;
        version_prefix = null;
        Object object6 = revision;
        revision = null;
        return ((IFn)const__8.getRawRoot()).invoke(object5, (Object)".", object6);
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return cli$development_version.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

