(ns epmon.web
  (:use schejulure.core)
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log]
            [taoensso.carmine :as car :refer (wcar)]
            [epmon.librato :as librato]))

; Environmental variables
(def redis-uri (env :redis-uri))
(def librato-user (env :librato-user))
(def librato-token (env :librato-token))
(def queue-name (env :queue-name))
(def metric-name (env :metric-name))

; Redis setup
(def redis-conn {:pool {}
                 :spec {:uri redis-uri}})
(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

; Get the queue length and log it to Librato as a gauge
(defn log-redis []
  (let [qlen (wcar* (car/llen queue-name))]
    (log/info (str "Sending " qlen))
    (librato/collate librato-user
                     librato-token
                     [{:name metric-name :value qlen }] [])))

; Return a 404 if anyone comes to the web app
(defroutes app
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  ; Launch the periodic task
  (def running-scheduler
    (call-every-minute log-redis))
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
