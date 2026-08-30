/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IExceptionInfo
 *  clojure.lang.IPersistentMap
 */
package datomic.impl;

import clojure.lang.IExceptionInfo;
import clojure.lang.IPersistentMap;

public class Exceptions {

    public static class IllegalStateExceptionInfo
    extends IllegalStateException
    implements IExceptionInfo {
        public final IPersistentMap data;

        public IllegalStateExceptionInfo(String s, IPersistentMap data2) {
            super(s);
            this.data = data2;
        }

        public IllegalStateExceptionInfo(String s, IPersistentMap data2, Throwable throwable) {
            super(s, throwable);
            this.data = data2;
        }

        public IPersistentMap getData() {
            return this.data;
        }

        @Override
        public String toString() {
            return "java.lang.IllegalStateException: " + this.getMessage();
        }
    }

    public static class IllegalArgumentExceptionInfo
    extends IllegalArgumentException
    implements IExceptionInfo {
        public final IPersistentMap data;

        public IllegalArgumentExceptionInfo(String s, IPersistentMap data2) {
            super(s);
            this.data = data2;
        }

        public IllegalArgumentExceptionInfo(String s, IPersistentMap data2, Throwable throwable) {
            super(s, throwable);
            this.data = data2;
        }

        public IPersistentMap getData() {
            return this.data;
        }

        @Override
        public String toString() {
            return "java.lang.IllegalArgumentException: " + this.getMessage();
        }
    }
}

