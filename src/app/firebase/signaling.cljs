(ns app.firebase.signaling
  (:require [app.firebase.firestore :refer [create-document! filter-change remove-document! update-document! watch-collection! watch-document!]]
            [app.webrtc.signaling :refer [Caller Callee IceHandler]]))

(defn- create-response-handler
  ([path handler]
   #(do
      (handler %)
      (remove-document! (str path "/" (:id %)))))
  ([path k handler]
   #(when-let [response (k %)]
      (handler response)
      (update-document! path (dissoc % k)))))

(defn- filter-added-candidates [handler]
  #(doseq [candidate (filter-change % "added")]
     (handler candidate)))

(defn- send-ice-candidate [path candidate]
  (when candidate
    (create-document! path (js->clj (.toJSON candidate)))))

(deftype FirebaseCaller [path]
  Caller
  (send-offer [_ offer]
    (update-document! path {:offer offer}))
  (watch-answer [_ handler]
    (watch-document! path (create-response-handler path :answer handler)))

  IceHandler
  (send-candidate [_ candidate]
    (send-ice-candidate (str path "/caller-candidates") candidate))
  (watch-ice-candidate [_ handler]
    (let [callee-path (str path "/callee-candidates")]
      (watch-collection!
       callee-path
       (filter-added-candidates (create-response-handler callee-path handler))))))

(deftype FirebaseCallee [path]
  Callee
  (send-answer [_ answer]
    (update-document! path {:answer answer}))
  (watch-offer [_ handler]
    (watch-document! path (create-response-handler path :offer handler)))

  IceHandler
  (send-candidate [_ candidate]
    (send-ice-candidate (str path "/callee-candidates") candidate))
  (watch-ice-candidate [_ handler]
    (let [caller-path (str path "/caller-candidates")]
      (watch-collection!
       caller-path
       (filter-added-candidates (create-response-handler caller-path handler))))))