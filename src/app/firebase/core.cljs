(ns app.firebase.core
  (:require ["firebase/app" :refer [initializeApp]]
            ["firebase/auth" :refer [getAuth signInAnonymously]]
            ["firebase/firestore" :refer [addDoc collection deleteDoc doc getDoc getDocs getFirestore onSnapshot setDoc]]))

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

(defn document->map
  "Returns the data of a document as a map or nil if the document doesn't exist."
  [document]
  (when (.exists document)
    (-> (js->clj (.data document) :keywordize-keys true)
        (assoc :id (.-id document)))))

(defn get-collection
  "Returns a js/Promise containing a lazy-sequence of the documents as maps."
  [path]
  (-> (collection firestore path)
      (getDocs)
      (.then #(map document->map (.-docs %)))))

(defn get-document
  [path]
  (-> (doc firestore path)
      (getDoc)
      (.then document->map)))

(defn create-document!
  "Create a new documet with a generated ID at the given path and data.
   Returns a js/Promise of the generated ID."
  [path data]
  (-> (collection firestore path)
      (addDoc (clj->js data))
      (.then #(.-id %))))

(defn update-document!
  "Updates the document at the given path with the data. If the document doesn't exist, it gets created."
  [path data]
  (-> (doc firestore path)
      (setDoc (clj->js data))))

(defn remove-document!
  "Removes the document from the given path."
  [path]
  (deleteDoc (doc firestore path)))

(defn filter-change
  "Filter a snapshot's documents based on the change type.
   Acceptable values are 'added' 'modified' 'removed'.
   Returns a lazy-list of documents."
  [changes type]
  (->> changes
       (filter #(= type (:change %)))
       (map :document)))

(defn- snapshot->documents
  "Transform a snapshot into a lazy-sequence of maps that contain the change type and document."
  [snapshot]
  (->> (.docChanges snapshot)
       (map #(hash-map :change (.-type %) :document (document->map (.-doc %))))))

(defn watch-collection!
  "Attach a handler for changes to a collection at the path.
   Change data for the snapshot is added as meta data."
  [path handler]
  (onSnapshot
   (collection firestore path)
   #(handler (snapshot->documents %))))