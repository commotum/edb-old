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
import datomic.backup.ValueBackup$fn__20107$fn__20108;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class ValueBackup$fn__20107
extends AFunction {
    Object throttle;
    Object progress;
    Object value_storage;
    Object this;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"pfuture");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"thread-pool");

    public ValueBackup$fn__20107(Object object, Object object2, Object object3, Object object4) {
        this.throttle = object;
        this.progress = object2;
        this.value_storage = object3;
        this.this = object4;
    }

    public Object invoke(Object leaf_id) {
        Object object = this_.throttle;
        if (object != null && object != Boolean.FALSE) {
            ((IFn)this_.throttle).invoke();
        }
        Object object2 = leaf_id;
        leaf_id = null;
        ValueBackup$fn__20107 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), (Object)new ValueBackup$fn__20107$fn__20108(this_.progress, this_.value_storage, object2, this_.this));
    }
}

