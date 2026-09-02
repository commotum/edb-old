(do
  (clojure.core/in-ns 'datomic.slf4j.bridge)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.slf4j.bridge)
    {:doc
     "Installs the JUL-to-SLF4J bridge once so Java utility logging follows the process's configured SLF4J backend."})
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/import 'org.slf4j.LoggerFactory)))
  (when-not (.equals 'datomic.slf4j.bridge 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.slf4j.bridge))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/import 'org.slf4j.LoggerFactory))))
  (.setMeta (clojure.lang.RT/var "datomic.slf4j.bridge" "bridge") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.slf4j.bridge" "bridge")
    (delay
      (.reset (java.util.logging.LogManager/getLogManager))
      (org.slf4j.bridge.SLF4JBridgeHandler/install)
      (.info (LoggerFactory/getLogger "datomic.slf4j.bridge") "SLF4J Bridge installed")
      :installed))
  (defn install ([] (deref bridge)))
  (reset-meta!
    #'install
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'install :ns *ns*)))
