(ns app.webrtc.signaling)

(defprotocol IceHandler
  "Interface for communicating ICE candidates."
  (send-candidate [this candidate] "Send an ICE candidate to another user.")
  (watch-ice-candidate [this handler] "Attach a handler for listening to new ICE candidate."))

(defprotocol Caller
  "Interface for signaling with a callee."
  (send-offer [this offer] "Send an offer to a callee.")
  (watch-answer [this handler] "Attach a handler for listening for an answer from a callee."))


(defprotocol Callee
  "Interface for signaling with a caller."
  (send-answer [this answer] "Send an answer to a caller.")
  (watch-offer [this handler] "Attach a handler for listening for an answer from a callee."))