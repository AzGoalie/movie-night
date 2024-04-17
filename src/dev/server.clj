(ns dev.server
  (:require [shadow.http.push-state :as shadow]
            [clojure.string :refer [blank? ends-with? split]]))

(defn handler
  "Simple wrapper around shadow-cljs' default dev server to serve html files.
   Ex: /hello maps to public/hello.html or /test/file maps to public/test/file.html"
  [{:keys [uri] :as req}]
  (let [file (last (split uri #"/"))
        ignored-types [".html" "/"]]
    (if-not (or (blank? file) (some #(ends-with? file %) ignored-types))
      (shadow/handle (assoc-in req [:http-config :push-state/index] (str uri ".html")))
      (shadow/handle req))))