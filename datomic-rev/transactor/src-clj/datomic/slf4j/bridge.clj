(do
  (clojure.core/in-ns 'datomic.slf4j.bridge)
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/import 'org.slf4j.LoggerFactory)))
  (when-not (.equals 'datomic.slf4j.bridge 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.slf4j.bridge))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/import 'org.slf4j.LoggerFactory))))
  (def bridge
   (delay
     (.reset (java.util.logging.LogManager/getLogManager))
     (org.slf4j.bridge.SLF4JBridgeHandler/install)
     (.info (LoggerFactory/getLogger "datomic.slf4j.bridge") "SLF4J Bridge installed")
     :installed))
  (defn install ([] (deref bridge))))