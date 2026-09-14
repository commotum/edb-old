/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.val_store.fs;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.channels.FileChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public final class FS$doit__21325
extends AFunction {
    Object new_file;
    Object v;
    public static final Var const__0 = RT.var((String)"datomic.core2.val-store.fs", (String)"SYNC_PUT_OPEN_OPTIONS");
    public static final Var const__1 = RT.var((String)"datomic.java.io.bbuf", (String)"write-buffer");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public FS$doit__21325(Object object, Object object2) {
        this.new_file = object;
        this.v = object2;
    }

    public Object invoke() {
        Object var2_2;
        FileChannel fc = FileChannel.open((Path)this.new_file, (OpenOption[])const__0.getRawRoot());
        try {
            IFn iFn = (IFn)const__1.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object = this.v;
            Object object2 = iLookupThunk.get(object);
            if (iLookupThunk == object2) {
                __thunk__0__ = __site__0__.fault(object);
                object2 = __thunk__0__.get(object);
            }
            iFn.invoke(object2, (Object)fc);
            fc.force(Boolean.TRUE);
            var2_2 = null;
        }
        finally {
            FileChannel fileChannel = fc;
            fc = null;
            ((AbstractInterruptibleChannel)fileChannel).close();
        }
        return var2_2;
    }
}

