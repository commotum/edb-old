/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class integrity$_main
extends RestFn {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"-main*");
    public static final Var const__1 = RT.var((String)"datomic.cli", (String)"parse-or-exit!");
    public static final AFn const__12 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.map((Object[])new Object[]{RT.keyword(null, (String)"long-name"), RT.keyword(null, (String)"validate"), RT.keyword(null, (String)"short-name"), RT.keyword(null, (String)"v"), RT.keyword(null, (String)"doc"), "Validate all segments (pulls entire db through memory)", RT.keyword(null, (String)"default"), null}), RT.map((Object[])new Object[]{RT.keyword(null, (String)"long-name"), RT.keyword(null, (String)"uri"), RT.keyword(null, (String)"required"), Boolean.TRUE, RT.keyword(null, (String)"doc"), "Database URI"})});
    public static final AFn const__13 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"uri"));
    public static final Var const__14 = RT.var((String)"datomic.api", (String)"shutdown");

    public static Object invokeStatic(ISeq args) {
        Object object;
        try {
            ISeq iSeq = args;
            args = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)"datomic.integrity", (Object)iSeq, (Object)const__12, (Object)const__13));
        }
        finally {
            ((IFn)const__14.getRawRoot()).invoke((Object)Boolean.TRUE);
        }
        return object;
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return integrity$_main.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

