(ns app.firebase.core
  (:require ["firebase/app" :refer [initializeApp]]
            ["firebase/auth" :refer [getAuth signInAnonymously]]
            ["firebase/firestore" :refer [getFirestore]]))

(def configuration
  #js {:apiKey            "AIzaSyCKtJVUHHDRF8OYbE8onqJJI3nsAXvsP7A"
       :authDomain        "movie-night-2928a.firebaseapp.com"
       :projectId         "movie-night-2928a"
       :storageBucket     "movie-night-2928a.appspot.com"
       :messagingSenderId "170551763745"
       :appId             "1:170551763745:web:a86648ec2bdfa5bff35460"})

(defonce app (initializeApp configuration))
(defonce auth (getAuth app))
(defonce firestore (getFirestore app))

(defonce user (atom nil))

(defn initialize-firebase
  "Initialize firebase and call the handler after the user has signed in."
  [callback]
  (->
   (signInAnonymously auth)
   (.then #(reset! user (.. % -user -uid)))
   (.then callback)))

(defn doc->map
  "Returns the data of a document as a map or nil if the document doesn't exist."
  [doc]
  (when (.exists doc)
    (js->clj (.data doc) :keywordize-keys true)))

(defn filter-snapshot
  "Filter viewer snapshots based on the change type.
   Acceptable values are 'added' 'modified' 'removed'.
   Returns a lazy-list of firebase documents."
  [snapshot type]
  (->> (.docChanges snapshot)
       (filter #(= (.-type %) type))
       (map #(.-doc %))))