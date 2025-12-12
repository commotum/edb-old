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
import datomic.backup.ValueBackup$fn__20099$fn__20100;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class ValueBackup$fn__20099
extends AFunction {
    Object this;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"pfuture");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"backup-branch-pool");

    public ValueBackup$fn__20099(Object object) {
        this.this = object;
    }

    public Object invoke(Object branch) {
        Object object = branch;
        branch = null;
        ValueBackup$fn__20099 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), (Object)new ValueBackup$fn__20099$fn__20100(object, this_.this));
    }
}

