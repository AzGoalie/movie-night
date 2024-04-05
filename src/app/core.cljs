(ns app.core
  (:require [app.firebase.core :refer [initialize-firebase user]]
            [app.domain.rooms :as rooms]
            [app.utils :refer [navigate]]))

(def modal-id "join-room-modal")
(def join-room-button-id "join-room-button")
(def modal-transition-time 400)

(defn- set-loading [element loading & {:keys [message]}]
  (.setAttribute element "aria-busy" loading)
  (.setAttribute element "aria-label" message))

(defn- navigate-to-new-room  []
  (-> (rooms/create-room! @user)
      (.then #(navigate "/watch" {:room-id %}))))

(defn ^:export handle-create-room []
  (let [create-room-button (.getElementById js/document "create-room-button")]
    (set-loading create-room-button true :message "Creating room...")
    (-> (initialize-firebase navigate-to-new-room)
        (.catch #(set-loading create-room-button false)))))

(defn ^:export open-modal []
  (let [modal (.getElementById js/document modal-id)
        html  (.-documentElement js/document)]
    (.add (.-classList html) "modal-is-open" "modal-is-opening")
    (js/setTimeout #(.remove (.-classList html) "modal-is-opening") modal-transition-time)
    (.showModal modal)))

(defn ^:export close-modal []
  (let [modal (.getElementById js/document modal-id)
        html  (.-documentElement js/document)]
    (.add (.-classList html) "modal-is-closing")
    (js/setTimeout #(do
                      (.remove (.-classList html) "modal-is-closing" "modal-is-open")
                      (.close modal))
                   modal-transition-time)))

(defn- handle-document-click [event]
  (let [modal (.getElementById js/document modal-id)
        modal-content (.querySelector modal "article")
        modal-button (.getElementById js/document join-room-button-id)
        target (.-target event)]
    (when (and (.-open modal)
               (not (.contains modal-content target))
               (not= target modal-button))
      (close-modal))))

(defn- handle-escape [event]
  (let [modal (.getElementById js/document modal-id)]
    (when (and (= "Escape" (.-key event)) (.-open modal))
      (close-modal))))

(defn ^:dev/after-load init []
  ;; Close modal when clicking outside
  (.addEventListener js/document "click" handle-document-click)

  ;; Close modal with escape
  (.addEventListener js/document "keydown" handle-escape))
