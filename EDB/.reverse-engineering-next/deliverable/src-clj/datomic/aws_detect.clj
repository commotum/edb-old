(do
  (clojure.core/in-ns 'datomic.aws-detect)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.string :as 'str]
        ['clojure.java.io :as 'io]
        ['clojure.java.shell :as 'sh])))
  (when-not (.equals 'datomic.aws-detect 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws-detect))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.string :as 'str]
          ['clojure.java.io :as 'io]
          ['clojure.java.shell :as 'sh]))))
  (set! *warn-on-reflection* true)
  (defn quickstream
    ([path timeout]
      (let [url (io/as-url path)
            conn (doto
                   (.openConnection ^java.net.URL url)
                   (.setConnectTimeout (int ^java.lang.Number timeout))
                   (.setReadTimeout (int ^java.lang.Number timeout)))]
        (.getInputStream ^java.net.URLConnection conn))))
  (defn get-ec2-private-ip
    ([]
      (try
        (slurp (quickstream "http://169.254.169.254/latest/meta-data/local-ipv4" 1000))
        (catch java.lang.Throwable _ nil))))
  (defn get-ec2-public-ip
    ([]
      (try
        (slurp (quickstream "http://169.254.169.254/latest/meta-data/public-ipv4" 1000))
        (catch java.lang.Throwable _ nil))))
  (def running-in-ec2-ref (delay (boolean (get-ec2-public-ip))))
  (defn running-in-ec2? ([] (deref running-in-ec2-ref))))