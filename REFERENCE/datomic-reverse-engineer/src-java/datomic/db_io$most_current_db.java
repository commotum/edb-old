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
import datomic.db_io$most_current_db$basis__17069;
import datomic.db_io$most_current_db$fn__17071;

public final class db_io$most_current_db
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(ISeq dbs) {
        db_io$most_current_db$basis__17069 basis;
        db_io$most_current_db$basis__17069 db_io$most_current_db$basis__17069 = basis = new db_io$most_current_db$basis__17069();
        basis = null;
        ISeq iSeq = dbs;
        dbs = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new db_io$most_current_db$fn__17071((Object)db_io$most_current_db$basis__17069), null, (Object)iSeq);
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return db_io$most_current_db.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

