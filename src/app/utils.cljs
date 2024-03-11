(ns app.utils
  (:require [clojure.string :refer [join]]))

(goog-define BASE_URL "/")

(defn- map->query-string [query-params]
  (join "&" (map (fn [[k v]] (str (name k) "=" v)) query-params)))

(defn navigate
  "Navigate the browser to the given path."
  ([path]
   (if (= path "/")
     (.assign js/document.location BASE_URL)
     (.assign js/document.location (if (= BASE_URL "/") path (str BASE_URL path)))))
  ([path query-params]
   (let [query-string (map->query-string query-params)]
     (if query-string
       (navigate (str path "?" query-string))
       (navigate path)))))

(defn get-query-params
  "Get current url search parameters as a map."
  []
  (let [url-search-params (js/URLSearchParams. js/document.location.search)]
    (js->clj (.fromEntries js/Object url-search-params) :keywordize-keys true)))

(defn create-button
  "Create a button with the given attributes and attach the handler to on-click/"
  [attributes handler]
  (let [button (.createElement js/document "button")]
    (doseq [[k v] attributes]  (aset button (name k) v))
    (.addEventListener button "click" handler)
    button))