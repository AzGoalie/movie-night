(ns app.streaming.peer-connection
  (:require [app.firebase.core :refer [firestore]]
            ["firebase/firestore" :refer [doc setDoc updateDoc]]))

(def ice-config
  {:iceServers           [{:urls ["stun:stun1.l.google.com:19302"
                                  "stun:stun2.l.google.com:19302"]}]
   :iceCandidatePoolSize 10})

(defn- create-ice-candidate-handler [room-id]
  (fn [event]
    (when-let [candidate (.-candidate event)]
      (setDoc (doc firestore room-id "offerCandidates") (.toJSON candidate)))))

(defn- create-negotiateion-needed-handler [room-id]
  (fn [event]
    (println "on-negotiation-needed")))

(defn create-peer-connection [room-id handle-add-track]
  (doto (js/RTCPeerConnection. (clj->js ice-config))
    (.addEventListener "icecandidate" (create-ice-candidate-handler room-id))
    (.addEventListener "negotiationneeded" (create-negotiateion-needed-handler room-id))
    (.addEventListener "track" handle-add-track)))

(defn create-offer [room-id peer-connection]
  (-> (.createOffer peer-connection)
      (.then #(.setLocalDescription peer-connection %))
      (.then #(let [description (.-localDescription peer-connection)
                    offer       {:type (.-type description)
                                 :sdp  (.-sdp description)}]
                (updateDoc (doc firestore "rooms" room-id) (clj->js offer))))))