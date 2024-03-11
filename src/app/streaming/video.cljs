(ns app.streaming.video)

(defn reset-video-player!
  "Resets an HTML video element. 
   Dispatches an 'ended' event on the video element and removes the srcObjec and src attribute."
  [video-player]
  (.pause video-player)
  (.dispatchEvent video-player (js/Event. "ended"))
  (set! (.-srcObject video-player) nil)
  (.removeAttribute video-player "src"))

(defn request-stream!
  "Ask the user to select a streaming source.
   This will have to browser display a popup asking for permission to share their screen."
  ([on-fullfilled]
   (.then
    (.getDisplayMedia js/navigator.mediaDevices)
    on-fullfilled))

  ([on-fullfilled on-rejected]
   (.then
    (.getDisplayMedia js/navigator.mediaDevices)
    on-fullfilled
    on-rejected)))

(defn mute!
  "Mute the video element."
  [video-player]
  (.setAttribute video-player "muted" true))

(defn add-stream!
  "Set the srcObj of the video element."
  [video-player stream]
  (set! (.-srcObject video-player) stream))

(defn add-on-stream-ended!
  "Attach a handler when the js/MediaStream ends."
  [stream handler]
  (let [track (first (.getVideoTracks stream))]
    (.addEventListener track "ended" handler)))