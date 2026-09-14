/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.aws_monitor.Qn;

public final class aws_monitor$fn__23606
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Keyword const__7;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final Var const__10;
    public static final Keyword const__11;
    public static final Keyword const__12;
    public static final Keyword const__14;
    public static final Keyword const__15;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object v, Object k) {
        v0 = v;
        v = null;
        map__23607 = v0;
        v1 = ((IFn)aws_monitor$fn__23606.const__0.getRawRoot()).invoke(map__23607);
        if (v1 != null && v1 != Boolean.FALSE) {
            v2 = map__23607;
            map__23607 = null;
            v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)aws_monitor$fn__23606.const__1.getRawRoot()).invoke(v2)));
        } else {
            v3 = map__23607;
            map__23607 = null;
        }
        map__23607 = v3;
        lo = RT.get((Object)map__23607, (Object)aws_monitor$fn__23606.const__3);
        hi = RT.get((Object)map__23607, (Object)aws_monitor$fn__23606.const__4);
        sum = RT.get((Object)map__23607, (Object)aws_monitor$fn__23606.const__5);
        v4 = map__23607;
        map__23607 = null;
        count = RT.get((Object)v4, (Object)aws_monitor$fn__23606.const__6);
        v5 = new Object[6];
        v5[0] = aws_monitor$fn__23606.const__7;
        v5[1] = RT.get((Object)aws_monitor$fn__23606.const__8.getRawRoot(), (Object)k, (Object)"Count");
        v5[2] = aws_monitor$fn__23606.const__9;
        v6 = k;
        k = null;
        v7 = v6;
        if (Util.classOf((Object)v6) == aws_monitor$fn__23606.__cached_class__0) ** GOTO lbl30
        if (!(v7 instanceof Qn)) {
            v7 = v7;
            aws_monitor$fn__23606.__cached_class__0 = Util.classOf((Object)v7);
lbl30:
            // 2 sources

            v8 = aws_monitor$fn__23606.const__10.getRawRoot().invoke(v7);
        } else {
            v8 = ((Qn)v7).qualified_name();
        }
        v5[3] = v8;
        v5[4] = aws_monitor$fn__23606.const__11;
        v9 = new Object[8];
        v9[0] = aws_monitor$fn__23606.const__12;
        v10 = lo;
        lo = null;
        v9[1] = RT.doubleCast((Object)v10);
        v9[2] = aws_monitor$fn__23606.const__14;
        v11 = hi;
        hi = null;
        v9[3] = RT.doubleCast((Object)v11);
        v9[4] = aws_monitor$fn__23606.const__15;
        v12 = count;
        count = null;
        v9[5] = RT.doubleCast((Object)v12);
        v9[6] = aws_monitor$fn__23606.const__5;
        v13 = sum;
        sum = null;
        v9[7] = RT.doubleCast((Object)v13);
        v5[5] = RT.mapUniqueKeys((Object[])v9);
        return RT.mapUniqueKeys((Object[])v5);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aws_monitor$fn__23606.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"lo");
        const__4 = RT.keyword(null, (String)"hi");
        const__5 = RT.keyword(null, (String)"sum");
        const__6 = RT.keyword(null, (String)"count");
        const__7 = RT.keyword(null, (String)"unit");
        const__8 = RT.var((String)"datomic.aws-monitor", (String)"units");
        const__9 = RT.keyword(null, (String)"metricName");
        const__10 = RT.var((String)"datomic.aws-monitor", (String)"qualified-name");
        const__11 = RT.keyword(null, (String)"statisticValues");
        const__12 = RT.keyword(null, (String)"minimum");
        const__14 = RT.keyword(null, (String)"maximum");
        const__15 = RT.keyword(null, (String)"sampleCount");
    }
}

