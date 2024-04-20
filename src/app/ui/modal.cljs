(ns app.ui.modal)

(def is-open-class "modal-is-open")
(def is-opening-class "modal-is-opening")
(def is-closing-class "modal-is-closing")
(def transition-time 400)

(def visible-modal (atom nil))

(defn open-modal [modal]
  (let [html  (.-documentElement js/document)]
    (.add (.-classList html) is-open-class is-opening-class)
    (js/setTimeout
     #(do
        (.remove (.-classList html) is-opening-class)
        (reset! visible-modal modal))
     transition-time)
    (.showModal modal)))

(defn close-modal [modal]
  (let [html (.-documentElement js/document)]
    (.add (.-classList html) is-closing-class)
    (js/setTimeout
     #(do
        (.remove (.-classList html) is-closing-class is-open-class)
        (.close modal)
        (reset! visible-modal nil))
     transition-time)))

(defn- handle-click [event]
  (when-let [modal @visible-modal]
    (let [modal-content   (.querySelector @visible-modal "article")
          clicked-inside? (.contains modal-content (.-target event))]
      (when (not clicked-inside?)
        (close-modal modal)))))

(defn- handle-escape [event]
  (when-let [modal @visible-modal]
    (when (= (.-key event) "Escape")
      (close-modal modal))))

(defn ^:export toggle-modal
  "Toggle the visiblity of a modal.
   Buttons that toggle modals must have the 'data-target' attribute for the modal id."
  [event]
  (.preventDefault event)
  (when-let [modal (.getElementById js/document (.. event -currentTarget -dataset -target))]
    (if (.-open modal)
      (close-modal modal)
      (open-modal modal))))

;; Allow users to close modals by clicking outside them
(.addEventListener js/document "click" handle-click)

;; Allow users to close modals by pressing escape
(.addEventListener js/document "keydown" handle-escape)