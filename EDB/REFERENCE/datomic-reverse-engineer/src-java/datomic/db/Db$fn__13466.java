/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class Db$fn__13466
extends AFunction {
    Object attrid;
    Object this;
    Object end;
    Object start;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attr-index-range");

    public Db$fn__13466(Object object, Object object2, Object object3, Object object4) {
        this.attrid = object;
        this.this = object2;
        this.end = object3;
        this.start = object4;
    }

    public Object invoke() {
        Db$fn__13466 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.this, this_.attrid, this_.start, this_.end);
    }
}

