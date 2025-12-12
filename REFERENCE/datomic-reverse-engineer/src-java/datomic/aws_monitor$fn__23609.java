/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.aws_monitor.Qn;

public final class aws_monitor$fn__23609
extends AFunction {
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Var const__2;
    public static final Keyword const__3;
    public static final Var const__4;
    public static final Keyword const__5;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object v, Object k) {
        v0 = new Object[6];
        v0[0] = aws_monitor$fn__23609.const__0;
        v0[1] = RT.get((Object)aws_monitor$fn__23609.const__2.getRawRoot(), (Object)k, (Object)"None");
        v0[2] = aws_monitor$fn__23609.const__3;
        v1 = k;
        k = null;
        v2 = v1;
        if (Util.classOf((Object)v1) == aws_monitor$fn__23609.__cached_class__0) ** GOTO lbl12
        if (!(v2 instanceof Qn)) {
            v2 = v2;
            aws_monitor$fn__23609.__cached_class__0 = Util.classOf((Object)v2);
lbl12:
            // 2 sources

            v3 = aws_monitor$fn__23609.const__4.getRawRoot().invoke(v2);
        } else {
            v3 = ((Qn)v2).qualified_name();
        }
        v0[3] = v3;
        v0[4] = aws_monitor$fn__23609.const__5;
        v4 = v;
        v = null;
        v0[5] = RT.doubleCast((Object)v4);
        return RT.mapUniqueKeys((Object[])v0);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aws_monitor$fn__23609.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.keyword(null, (String)"unit");
        const__2 = RT.var((String)"datomic.aws-monitor", (String)"units");
        const__3 = RT.keyword(null, (String)"metricName");
        const__4 = RT.var((String)"datomic.aws-monitor", (String)"qualified-name");
        const__5 = RT.keyword(null, (String)"value");
    }
}

