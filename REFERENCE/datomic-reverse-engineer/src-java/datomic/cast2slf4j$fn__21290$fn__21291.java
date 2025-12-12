/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  cognitect.caster.Impl
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import cognitect.caster.Impl;
import datomic.cast2slf4j$fn__21290$fn__21291$fn__21292;
import datomic.cast2slf4j$fn__21290$fn__21291$fn__21295;
import datomic.cast2slf4j$fn__21290$fn__21291$fn__21298;
import datomic.cast2slf4j$fn__21290$fn__21291$fn__21301;

public final class cast2slf4j$fn__21290$fn__21291
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Keyword const__5;

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        v0 = cast2slf4j$fn__21290$fn__21291.const__1.getRawRoot();
        if (Util.classOf((Object)v0) == cast2slf4j$fn__21290$fn__21291.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof Impl)) {
            v0 = v0;
            cast2slf4j$fn__21290$fn__21291.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = cast2slf4j$fn__21290$fn__21291.const__0.getRawRoot().invoke(v0, (Object)new cast2slf4j$fn__21290$fn__21291$fn__21292(), (Object)cast2slf4j$fn__21290$fn__21291.const__2);
        } else {
            v1 = ((Impl)v0).register_STAR_((Object)new cast2slf4j$fn__21290$fn__21291$fn__21292(), (Object)cast2slf4j$fn__21290$fn__21291.const__2);
        }
        if (Util.classOf((Object)(v2 = cast2slf4j$fn__21290$fn__21291.const__1.getRawRoot())) == cast2slf4j$fn__21290$fn__21291.__cached_class__1) ** GOTO lbl13
        if (!(v2 instanceof Impl)) {
            v2 = v2;
            cast2slf4j$fn__21290$fn__21291.__cached_class__1 = Util.classOf((Object)v2);
lbl13:
            // 2 sources

            v3 = cast2slf4j$fn__21290$fn__21291.const__0.getRawRoot().invoke(v2, (Object)new cast2slf4j$fn__21290$fn__21291$fn__21295(), (Object)cast2slf4j$fn__21290$fn__21291.const__3);
        } else {
            v3 = ((Impl)v2).register_STAR_((Object)new cast2slf4j$fn__21290$fn__21291$fn__21295(), (Object)cast2slf4j$fn__21290$fn__21291.const__3);
        }
        if (Util.classOf((Object)(v4 = cast2slf4j$fn__21290$fn__21291.const__1.getRawRoot())) == cast2slf4j$fn__21290$fn__21291.__cached_class__2) ** GOTO lbl20
        if (!(v4 instanceof Impl)) {
            v4 = v4;
            cast2slf4j$fn__21290$fn__21291.__cached_class__2 = Util.classOf((Object)v4);
lbl20:
            // 2 sources

            v5 = cast2slf4j$fn__21290$fn__21291.const__0.getRawRoot().invoke(v4, (Object)new cast2slf4j$fn__21290$fn__21291$fn__21298(), (Object)cast2slf4j$fn__21290$fn__21291.const__4);
        } else {
            v5 = ((Impl)v4).register_STAR_((Object)new cast2slf4j$fn__21290$fn__21291$fn__21298(), (Object)cast2slf4j$fn__21290$fn__21291.const__4);
        }
        if (Util.classOf((Object)(v6 = cast2slf4j$fn__21290$fn__21291.const__1.getRawRoot())) == cast2slf4j$fn__21290$fn__21291.__cached_class__3) ** GOTO lbl27
        if (!(v6 instanceof Impl)) {
            v6 = v6;
            cast2slf4j$fn__21290$fn__21291.__cached_class__3 = Util.classOf((Object)v6);
lbl27:
            // 2 sources

            this = null;
            v7 = cast2slf4j$fn__21290$fn__21291.const__0.getRawRoot().invoke(v6, (Object)new cast2slf4j$fn__21290$fn__21291$fn__21301(), (Object)cast2slf4j$fn__21290$fn__21291.const__5);
        } else {
            v7 = ((Impl)v6).register_STAR_((Object)new cast2slf4j$fn__21290$fn__21291$fn__21301(), (Object)cast2slf4j$fn__21290$fn__21291.const__5);
        }
        return v7;
    }

    static {
        const__0 = RT.var((String)"cognitect.caster", (String)"register*");
        const__1 = RT.var((String)"cognitect.caster", (String)"instance");
        const__2 = RT.keyword(null, (String)"alert");
        const__3 = RT.keyword(null, (String)"event");
        const__4 = RT.keyword(null, (String)"metric");
        const__5 = RT.keyword(null, (String)"dev");
    }
}

