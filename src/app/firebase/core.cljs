(ns app.firebase.core
  (:require ["firebase/app" :refer [initializeApp]]
            ["firebase/auth" :refer [getAuth onAuthStateChanged signInAnonymously]]
            ["firebase/firestore" :refer [getFirestore]]))

(def configuration
  #js {:apiKey            "AIzaSyCKtJVUHHDRF8OYbE8onqJJI3nsAXvsP7A"
       :authDomain        "movie-night-2928a.firebaseapp.com"
       :projectId         "movie-night-2928a"
       :storageBucket     "movie-night-2928a.appspot.com"
       :messagingSenderId "170551763745"
       :appId             "1:170551763745:web:a86648ec2bdfa5bff35460"})

(def app (initializeApp configuration))
(def auth (getAuth app))
(def firestore (getFirestore app))

(def user (atom nil))

(onAuthStateChanged auth #(reset! user (.-uid %)))
(signInAnonymously auth)
