/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class thread$daemon_factory$reify__21010
implements ThreadFactory,
IObj {
    final IPersistentMap __meta;
    Object name_prefix;
    Object idx;
    Object group;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"inc");

    public thread$daemon_factory$reify__21010(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.name_prefix = object;
        this.idx = object2;
        this.group = object3;
    }

    public thread$daemon_factory$reify__21010(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new thread$daemon_factory$reify__21010(iPersistentMap, this.name_prefix, this.idx, this.group);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread2;
        Object object = this.group;
        if (object != null && object != Boolean.FALSE) {
            runnable = null;
            thread2 = new Thread((ThreadGroup)this.group, runnable);
        } else {
            Runnable runnable2 = runnable;
            runnable = null;
            thread2 = Executors.defaultThreadFactory().newThread(runnable2);
        }
        Thread G__21011 = thread2;
        G__21011.setName((String)((IFn)const__0.getRawRoot()).invoke(this.name_prefix, ((IFn)const__1.getRawRoot()).invoke(this.idx, const__2.getRawRoot())));
        G__21011.setDaemon(Boolean.TRUE);
        Object var2_2 = null;
        return G__21011;
    }
}

