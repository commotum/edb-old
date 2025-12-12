/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLL
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Reader
 *  org.fressian.handlers.ReadHandler
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.IOException;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

public final class transaction$reify__15901
implements ReadHandler,
IObj {
    final IPersistentMap __meta;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"make-eid");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"ensure-vector");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"retracting-datum");

    public transaction$reify__15901(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public transaction$reify__15901() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new transaction$reify__15901(iPersistentMap);
    }

    public Object read(Reader rdr, Object tag, int component_count) throws IOException {
        Object object;
        boolean assert_QMARK_ = rdr.readBoolean();
        long part2 = rdr.readInt();
        long eidx = rdr.readInt();
        long eid = ((IFn.LLL)const__0.getRawRoot()).invokePrim(part2, eidx);
        long attrid = rdr.readInt();
        Object v = ((IFn)const__1.getRawRoot()).invoke(rdr.readObject());
        Reader reader2 = rdr;
        rdr = null;
        long t = reader2.readInt();
        if (assert_QMARK_) {
            Object object2 = v;
            v = null;
            object = ((IFn.LLOLO)const__2.getRawRoot()).invokePrim(eid, attrid, object2, t);
        } else {
            Object object3 = v;
            v = null;
            object = ((IFn.LLOLO)const__3.getRawRoot()).invokePrim(eid, attrid, object3, t);
        }
        return object;
    }
}

