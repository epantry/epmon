; Copied with love from
; https://github.com/aphyr/clj-librato/

(ns epmon.librato
  (:use [slingshot.slingshot :only [try+]])
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [clj-http.conn-mgr :as conn-mgr]
            [clojure.string  :as string]
            [clojure.tools.logging :as log]
            clj-http.util))

(def uri-base "https://metrics-api.librato.com/v1/")

(defn uri
  "The full URI of a particular resource, by path fragments."
  [& path-fragments]
  (apply str uri-base
         (interpose "/" (map (comp clj-http.util/url-encode str)
                             path-fragments))))

(defn unparse-kw
  "Convert a clojure-style dashed keyword map into string underscores.
  Recursive."
  [m]
  (cond
    (map? m) (into {} (map (fn [[k v]]
                             [(string/replace (name k) "-" "_")
                              (unparse-kw v)])
                           m))
    (coll? m) (map unparse-kw m)
    true m))

(defn parse-kw
  "Parse a response map into dashed, keyword keys. Not recursive: some librato
  API functions return arbitrary string keys in maps."
  [m]
  (into {} (map (fn [[k v]] [(keyword (string/replace k "_" "-")) v]) m)))

(defn connection-manager
  "Return a connection manager that can be passed as :connection-manager in
  a request."
  [{:keys [timeout threads] :or {timeout 10 threads 2} :as options}]
  (conn-mgr/make-reusable-conn-manager
   (merge {:timeout timeout :threads threads :default-per-route threads}
          options)))

(defn request
  "Constructs the HTTP client request map.
  options will be merged verbatim into the request map."
  ([user api-key params]
   {:basic-auth [user api-key]
    :content-type :json
    :accept :json
    :throw-entire-message? true
    :query-params (unparse-kw params)})
  ([user api-key params body]
   (assoc (request user api-key params)
     :body (json/write-str (unparse-kw body)))))

(defn collate
  "Posts a set of gauges and counters. options is a map of clj-http options."
  ([user api-key gauges counters]
     (collate user api-key gauges counters nil))
  ([user api-key gauges counters options]
     (assert (every? :name gauges))
     (assert (every? :name counters))
     (assert (every? :value gauges))
     (assert (every? :value counters))
     (client/post (uri "metrics")
                  (merge
                   options
                   (request user api-key {}
                            {:gauges gauges :counters counters})))))
