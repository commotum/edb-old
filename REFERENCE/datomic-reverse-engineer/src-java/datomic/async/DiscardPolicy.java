/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.async;

import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public final class DiscardPolicy
implements RejectedExecutionHandler,
IType {
    public final Object f;

    public DiscardPolicy(Object object) {
        this.f = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"f"));
    }

    public void rejectedExecution(Runnable _, ThreadPoolExecutor _2) {
        DiscardPolicy this_ = null;
        ((IFn)this_.f).invoke();
    }
}

