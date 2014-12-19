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

(def redis-uri
  (env :rediscloud-url))

(def librato-user
  (env :librato-user))

(def librato-token
  (env :librato-token))

(def redis-conn {:pool {}
                 :spec {:uri redis-uri}})

(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(defn log-redis []
  (let [qlen (wcar* (car/llen "celery"))]
    (librato/collate librato-user
                     librato-token
                     [{:name "celery_depth" :value qlen }] [])))

(defroutes app
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (def my-running-scheduler
    (call-every-minute log-redis))
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
