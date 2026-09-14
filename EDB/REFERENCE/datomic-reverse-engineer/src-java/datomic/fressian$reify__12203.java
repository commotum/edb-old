/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.fressian.Writer
 *  org.fressian.handlers.WriteHandler
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.io.IOException;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class fressian$reify__12203
implements WriteHandler,
IObj {
    final IPersistentMap __meta;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"lang"), (Object)RT.keyword(null, (String)"imports"), (Object)RT.keyword(null, (String)"requires"), (Object)RT.keyword(null, (String)"params"), (Object)RT.keyword(null, (String)"code"));

    public fressian$reify__12203(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public fressian$reify__12203() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fressian$reify__12203(iPersistentMap);
    }

    public void write(Writer w, Object f) throws IOException {
        w.writeTag((Object)"datomic/fn", RT.intCast((long)1L));
        Writer writer2 = w;
        w = null;
        Object object = f;
        f = null;
        writer2.writeObject(((IFn)const__1.getRawRoot()).invoke(object, (Object)const__7));
    }
}

