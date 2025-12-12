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
package datomic.common.proxy$java.util;

import clojure.lang.IFn;
import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentMap;
import clojure.lang.IProxy;
import clojure.lang.RT;
import java.util.TimerTask;

public class TimerTask$ff19274a
extends TimerTask
implements IProxy {
    private volatile IPersistentMap __clojureFnMap;

    public TimerTask$ff19274a() {
        TimerTask$ff19274a timerTask$ff19274a = this;
        TimerTask$ff19274a timerTask$ff19274a2 = timerTask$ff19274a;
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

    public long scheduledExecutionTime() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"scheduledExecutionTime");
        return object != null ? ((Number)((IFn)object).invoke((Object)this)).longValue() : super.scheduledExecutionTime();
    }

    public boolean cancel() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"cancel");
        return object != null ? ((Boolean)((IFn)object).invoke((Object)this)).booleanValue() : super.cancel();
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

    public void run() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"run");
        if (object == null) {
            throw new UnsupportedOperationException("run");
        }
        ((IFn)object).invoke((Object)this);
    }
}

