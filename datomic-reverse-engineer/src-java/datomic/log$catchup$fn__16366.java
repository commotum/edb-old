/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.log.LogSegSeq;
import org.slf4j.LoggerFactory;

public final class log$catchup$fn__16366
extends AFunction {
    Object iter;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Keyword const__1;
    public static final Keyword const__2;
    public static final Keyword const__3;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;

    public log$catchup$fn__16366(Object object) {
        this.iter = object;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        block2: {
            logger = LoggerFactory.getLogger((String)"datomic.log");
            if (!logger.isInfoEnabled()) break block2;
            v0 = logger;
            logger = null;
            v1 = (IFn)log$catchup$fn__16366.const__0.getRawRoot();
            v2 = new Object[4];
            v2[0] = log$catchup$fn__16366.const__1;
            v2[1] = log$catchup$fn__16366.const__2;
            v2[2] = log$catchup$fn__16366.const__3;
            v3 = (IFn)log$catchup$fn__16366.const__5.getRawRoot();
            v4 = log$catchup$fn__16366.const__6.getRawRoot();
            v5 = this.iter;
            if (Util.classOf((Object)v5) == log$catchup$fn__16366.__cached_class__0) ** GOTO lbl17
            if (!(v5 instanceof LogSegSeq)) {
                v5 = v5;
                log$catchup$fn__16366.__cached_class__0 = Util.classOf((Object)v5);
lbl17:
                // 2 sources

                v6 = log$catchup$fn__16366.const__7.getRawRoot().invoke(v5);
            } else {
                v6 = ((LogSegSeq)v5).log_seg_seq();
            }
            v2[3] = RT.count((Object)v3.invoke(v4, v6));
            v0.info((String)v1.invoke((Object)RT.mapUniqueKeys((Object[])v2)));
        }
        return null;
    }

    static {
        const__0 = RT.var((String)"datomic.slf4j", (String)"process");
        const__1 = RT.keyword(null, (String)"event");
        const__2 = RT.keyword((String)"log", (String)"load-segments");
        const__3 = RT.keyword(null, (String)"count");
        const__5 = RT.var((String)"clojure.core", (String)"pmap");
        const__6 = RT.var((String)"clojure.core", (String)"identity");
        const__7 = RT.var((String)"datomic.log", (String)"log-seg-seq");
    }
}

