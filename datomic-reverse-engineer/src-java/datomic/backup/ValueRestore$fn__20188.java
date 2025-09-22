/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.backup;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.backup.ValueRestore$fn__20188$fn__20189;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class ValueRestore$fn__20188
extends AFunction {
    Object this;
    Object progress;
    Object to_cluster;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"pfuture");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"thread-pool");

    public ValueRestore$fn__20188(Object object, Object object2, Object object3) {
        this.this = object;
        this.progress = object2;
        this.to_cluster = object3;
    }

    public Object invoke(Object leaf_id) {
        Object object = leaf_id;
        leaf_id = null;
        ValueRestore$fn__20188 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), (Object)new ValueRestore$fn__20188$fn__20189(this_.this, this_.progress, this_.to_cluster, object));
    }
}

