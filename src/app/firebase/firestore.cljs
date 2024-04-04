(ns app.firebase.firestore
  (:require [app.firebase.core :refer [firestore]]
            ["firebase/firestore" :refer [addDoc collection deleteDoc doc getDoc getDocs onSnapshot setDoc]]))

(defn- document->map
  "Returns the data of a document as a map or nil if the document doesn't exist."
  [^js document]
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
  (-> (getDoc (doc firestore path))
      (.then document->map)))

(defn create-document!
  "Create a new documet with a generated ID at the given path and data.
   Returns a js/Promise of the generated ID."
  [path data]
  (-> (collection firestore path)
      (addDoc (clj->js (dissoc data :id)))
      (.then #(.-id %))))

(defn update-document!
  "Updates the document at the given path with the data. If the document doesn't exist, it gets created."
  [path data]
  (-> (doc firestore path)
      (setDoc (clj->js (dissoc data :id)))))

(defn remove-document!
  "Removes the document from the given path."
  [path]
  (deleteDoc (doc firestore path)))

(defn filter-change
  "Filter a snapshot's documents based on the change type.
   Acceptable values are 'added' 'modified' 'removed'.
   Returns a lazy-list of documents."
  [documents type]
  (filter #(= type (get-in % [:firestore/meta :change])) documents))

(defn- snapshot->documents
  "Transform a snapshot into a lazy-sequence of maps that contain the change type and document."
  [snapshot]
  (->> (.docChanges snapshot)
       (map #(assoc-in (document->map (.-doc %)) [:firestore/meta :change] (.-type %)))))

(defn watch-collection!
  "Attach a handler for changes to a collection at the path.
   Change data for the snapshot is added as meta data."
  [path handler]
  (onSnapshot
   (collection firestore path)
   #(handler (snapshot->documents %))))

(defn watch-document!
  "Attach a handler for changes to a document at the path."
  [path handler]
  (onSnapshot
   (doc firestore path)
   #(handler (document->map %))))