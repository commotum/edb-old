/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.backup;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.IValueBackup;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class ValueBackup$fn__20099$fn__20100
extends AFunction {
    Object branch;
    Object this;
    private static Class __cached_class__0;
    public static final Var const__0;

    public ValueBackup$fn__20099$fn__20100(Object object, Object object2) {
        this.branch = object;
        this.this = object2;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        Object object2 = this_.this;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof IValueBackup) {
                object = ((IValueBackup)object2).backup_node(this_.branch);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        ValueBackup$fn__20099$fn__20100 this_ = null;
        object = const__0.getRawRoot().invoke(object2, this_.branch);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"backup-node");
    }
}

