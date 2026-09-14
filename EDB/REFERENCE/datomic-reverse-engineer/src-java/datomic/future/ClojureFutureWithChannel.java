/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IBlockingDeref
 *  clojure.lang.IDeref
 *  clojure.lang.IFn
 *  clojure.lang.IPending
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.future;

import clojure.lang.IBlockingDeref;
import clojure.lang.IDeref;
import clojure.lang.IFn;
import clojure.lang.IPending;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.future.GetChannel;
import java.util.concurrent.Future;

public final class ClojureFutureWithChannel
implements GetChannel,
IPending,
IBlockingDeref,
IDeref,
IType {
    public final Object fut;
    public final Object ch;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");

    public ClojureFutureWithChannel(Object object, Object object2) {
        this.fut = object;
        this.ch = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"fut"), (Object)Symbol.intern(null, (String)"ch"));
    }

    public Object deref(long timeout_ms, Object object) {
        Object object2 = object;
        object = null;
        ClojureFutureWithChannel this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.fut, (Object)Numbers.num((long)timeout_ms), object2);
    }

    public Object deref() {
        return ((Future)this.fut).get();
    }

    public boolean isRealized() {
        ClojureFutureWithChannel this_ = null;
        return ((IPending)this_.fut).isRealized();
    }

    public Object get_channel() {
        return this.ch;
    }
}

