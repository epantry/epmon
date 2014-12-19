(defproject epmon "1.0.0-SNAPSHOT"
  :description "Monitoring ePantry.com"
  :url "https://shrouded-dawn-5605.herokuapp.com/"
  :license {:name "Property of ePantry LLC"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.2.0"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [environ "1.0.0"]
                 [clj-http "1.0.1"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [schejulure "1.0.1"]
                 [com.taoensso/carmine "2.9.0"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]
            [lein-ring "0.8.13"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "epmon.jar"
  :profiles {:production {:env {:production true}}}
  :ring {:handler epmon.web/app})
