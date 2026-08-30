(do
  (clojure.core/in-ns 'datomic.tools.read-segment)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.java.io :as 'io]
        ['datomic.api :as 'd]
        ['datomic.cluster :as 'cluster]
        ['datomic.fressian :as 'fressian]
        ['datomic.tools :as 'tools])
      (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.tools.read-segment 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.read-segment))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.java.io :as 'io]
          ['datomic.api :as 'd]
          ['datomic.cluster :as 'cluster]
          ['datomic.fressian :as 'fressian]
          ['datomic.tools :as 'tools])
        (clojure.core/import 'java.nio.ByteBuffer))))
  (set! *warn-on-reflection* true)
  (defmethod
    print-method
    org.fressian.TaggedObject
    fn__33244
    ([o w]
      (.write ^java.io.Writer w "#fressian/tagged")
      ((deref #'clojure.core/print-sequential)
        "["
        #'clojure.core/pr-on
        " "
        "]"
        [(.getTag ^org.fressian.TaggedObject o)
         (map
           (fn fn__33245
             ([p1__33243#] (try (seq p1__33243#) (catch java.lang.Throwable t p1__33243#))))
           (.getValue ^org.fressian.TaggedObject o))]
        w)))
  (defn make-file
    ([name bs]
      (with-open [w (io/output-stream name)] (do (.write ^java.io.OutputStream w ^bytes bs) nil))))
  (reset-meta!
    #'make-file
    (assoc {:arglists (clojure.core/list ['name 'bs]), :column (int 1)} :name 'make-file :ns *ns*))
  (def read-segment
   (fn read_segment
     ([uri val_key fname]
       (let [cr (tools/connection-resources uri)
             cluster (:cluster cr)
             value (deref (cluster/get-val cluster val_key))]
         (when (:buf value)
           (if fname
             (make-file fname (.array (:buf value)))
             ((fressian/val->obj fressian/clojure-read-handlers) (:buf value))))))))
  (reset-meta!
    #'read-segment
    (assoc
      {:arglists (clojure.core/list ['uri 'val-key 'fname]), :column (int 1)}
      :name
      'read-segment
      :ns
      *ns*))
  (defn -main
    ([& args]
      (try
        (cond
          (= (count args) 2) (let [vec__33250 args
                                   uri (nth vec__33250 (int 0) nil)
                                   val_key (nth vec__33250 (int 1) nil)]
                               (prn (read-segment uri val_key nil))
                               (java.lang.System/exit (int 0))
                               nil)
          (= (count args) 3) (let [vec__33253 args
                                   uri (nth vec__33253 (int 0) nil)
                                   val_key (nth vec__33253 (int 1) nil)
                                   fname (nth vec__33253 (int 2) nil)]
                               (read-segment uri val_key fname)
                               (java.lang.System/exit (int 0))
                               nil)
          :default (do
                     (println
                       "Usage: bin/run -m datomic.tools.read-segment <db uri> <value key> <optional filename>")
                     (java.lang.System/exit (int 1))
                     nil))
        (catch
          java.lang.Throwable
          t
          (do (.printStackTrace ^java.lang.Throwable t) (java.lang.System/exit (int 1)) nil)))))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name '-main :ns *ns*)))