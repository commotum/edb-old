/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.io.Coercions;
import java.net.URI;

public final class backup$backup_concurrency
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final String const__1;
    public static final Var const__2;
    public static final String const__3;
    public static final Var const__4;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object uri) {
        v0 = uri;
        uri = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == backup$backup_concurrency.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof Coercions)) {
            v1 = v1;
            backup$backup_concurrency.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = backup$backup_concurrency.const__0.getRawRoot().invoke(v1);
        } else {
            v2 = ((Coercions)v1).as_uri();
        }
        v3 = scheme = ((URI)v2).getScheme();
        scheme = null;
        G__20311 = v3;
        switch (Util.hash((Object)G__20311) >> 2 & 1) {
            case 0: {
                if (Util.equiv((Object)G__20311, (Object)backup$backup_concurrency.const__1)) {
                    v4 = ((IFn)backup$backup_concurrency.const__2.getRawRoot()).invoke((Object)"datomic.s3BackupConcurrency");
                    break;
                }
                ** GOTO lbl24
            }
            case 1: {
                if (Util.equiv((Object)G__20311, (Object)backup$backup_concurrency.const__3)) {
                    v4 = ((IFn)backup$backup_concurrency.const__2.getRawRoot()).invoke((Object)"datomic.fileBackupConcurrency");
                    break;
                }
            }
lbl24:
            // 4 sources

            default: {
                v5 = G__20311;
                G__20311 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)backup$backup_concurrency.const__4.getRawRoot()).invoke((Object)"No matching clause: ", (Object)v5));
            }
        }
        return v4;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$backup_concurrency.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.io", (String)"as-uri");
        const__1 = "s3";
        const__2 = RT.var((String)"datomic.config", (String)"property");
        const__3 = "file";
        const__4 = RT.var((String)"clojure.core", (String)"str");
    }
}

