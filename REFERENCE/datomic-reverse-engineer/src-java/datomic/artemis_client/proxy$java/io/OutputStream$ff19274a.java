/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentCollection
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IProxy
 *  clojure.lang.RT
 */
package datomic.artemis_client.proxy$java.io;

import clojure.lang.IFn;
import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentMap;
import clojure.lang.IProxy;
import clojure.lang.RT;
import java.io.OutputStream;

public class OutputStream$ff19274a
extends OutputStream
implements IProxy {
    private volatile IPersistentMap __clojureFnMap;

    public OutputStream$ff19274a() {
        OutputStream$ff19274a outputStream$ff19274a = this;
        OutputStream$ff19274a outputStream$ff19274a2 = outputStream$ff19274a;
    }

    public void __initClojureFnMappings(IPersistentMap iPersistentMap) {
        this.__clojureFnMap = iPersistentMap;
    }

    public void __updateClojureFnMappings(IPersistentMap iPersistentMap) {
        this.__clojureFnMap = (IPersistentMap)((IPersistentCollection)this.__clojureFnMap).cons((Object)iPersistentMap);
    }

    public IPersistentMap __getClojureFnMappings() {
        return this.__clojureFnMap;
    }

    public void write(byte[] byArray, int n, int n2) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"write");
        if (object != null) {
            ((IFn)object).invoke((Object)this, (Object)byArray, (Object)n, (Object)n2);
        } else {
            super.write(byArray, n, n2);
        }
    }

    public void write(byte[] byArray) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"write");
        if (object != null) {
            ((IFn)object).invoke((Object)this, (Object)byArray);
        } else {
            super.write(byArray);
        }
    }

    public void flush() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"flush");
        if (object != null) {
            ((IFn)object).invoke((Object)this);
        } else {
            super.flush();
        }
    }

    public void close() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"close");
        if (object != null) {
            ((IFn)object).invoke((Object)this);
        } else {
            super.close();
        }
    }

    public boolean equals(Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"equals");
        return object2 != null ? ((Boolean)((IFn)object2).invoke((Object)this, object)).booleanValue() : super.equals(object);
    }

    public String toString() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"toString");
        return object != null ? (String)((IFn)object).invoke((Object)this) : super.toString();
    }

    public int hashCode() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"hashCode");
        return object != null ? ((Number)((IFn)object).invoke((Object)this)).intValue() : super.hashCode();
    }

    public Object clone() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"clone");
        return object != null ? ((IFn)object).invoke((Object)this) : super.clone();
    }

    public void write(int n) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"write");
        if (object == null) {
            throw new UnsupportedOperationException("write");
        }
        ((IFn)object).invoke((Object)this, (Object)n);
    }
}

