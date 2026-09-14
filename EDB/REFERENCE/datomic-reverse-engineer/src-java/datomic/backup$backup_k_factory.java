/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class backup$backup_k_factory
extends AFunction
implements IFn.LO {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__4 = RT.var((String)"datomic.backup", (String)"add-key-prefix");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static Object invokeStatic(long backup_version) {
        long G__20009 = backup_version;
        switch ((int)G__20009) {
            case 1: {
                if (1L != G__20009) throw (Throwable)new IllegalArgumentException((String)((IFn)const__5.getRawRoot()).invoke((Object)"No matching clause: ", (Object)Numbers.num((long)G__20009)));
                Object object = const__1.getRawRoot();
                return object;
            }
            case 2: {
                if (2L != G__20009) throw (Throwable)new IllegalArgumentException((String)((IFn)const__5.getRawRoot()).invoke((Object)"No matching clause: ", (Object)Numbers.num((long)G__20009)));
                Object object = const__1.getRawRoot();
                return object;
            }
            case 3: {
                if (3L != G__20009) throw (Throwable)new IllegalArgumentException((String)((IFn)const__5.getRawRoot()).invoke((Object)"No matching clause: ", (Object)Numbers.num((long)G__20009)));
                Object object = const__4.getRawRoot();
                return object;
            }
            default: {
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__5.getRawRoot()).invoke((Object)"No matching clause: ", (Object)Numbers.num((long)G__20009)));
            }
        }
    }

    public Object invoke(Object object) {
        return backup$backup_k_factory.invokeStatic(RT.longCast((Object)((Number)object)));
    }

    public final Object invokePrim(long l) {
        return backup$backup_k_factory.invokeStatic(l);
    }
}

