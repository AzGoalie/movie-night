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
  "Initialize firebase and return a js/Promise after the user has signed in."
  []
  (->
   (signInAnonymously auth)
   (.then #(reset! user (.. % -user -uid)))))