(ns build.core
  (:require [clojure.java.io :as io]
            [hiccup2.core :as h]
            [clj-reload.core :as reload]
            [shadow.html :refer [replace-script-names]]))

(reload/init {:dirs ["src/app"]})

(defn- hiccup->html
  [{:shadow.build/keys [config mode] :as build-state} hiccup target-file]
  (io/make-parents target-file)

  (let [html (str "<!DOCTYPE html>\n" (h/html hiccup))]
    (if (and (= :release mode)
             (:module-hash-names config))
      (spit target-file (replace-script-names
                         html
                         (:asset-path config "/js")
                         (or (:shadow.build.closure/modules build-state)
                             (:build-modules build-state))))
      (spit target-file html))))

(defn render-hiccup
  {:shadow.build/stage :flush}
  [build-state hiccup-sym destination]

  (reload/reload)
  (-> hiccup-sym namespace symbol (require :reload))

  (let [hiccup-var  (find-var hiccup-sym)
        target-file (io/file destination)]
    (hiccup->html build-state (var-get hiccup-var) target-file)
    build-state))