(ns app.html)

(def head
  [:head
   [:meta {:charset "UTF-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:link {:rel  "stylesheet" :href "./css/pico.min.css"}]
   [:link {:rel  "stylesheet" :href "./css/styles.css"}]
   [:title "Movie Night"]])

(def index-page
  [:html
   head
   [:body
    [:main.container
     [:section.center
      [:h1 "Welcome to Movie Night!"]
      [:p "Throwing a virtual watch party? It's simple! Create a room to host
           your friends, or join the fun with a room ID you've received."]

      [:div.grid
       [:button {:id "create-room-button" :onclick "app.core.handle_create_room()"}
        "Create a room"]
       [:button.outline {:id "join-room-button"
                         :data-target "join-room-modal"
                         :onclick "app.modal.toggle_modal(event)"}
        "Join a room"]]]]

    [:script {:src "./js/shared.js"}]
    [:script {:src "./js/main.js"}]]

   [:dialog {:id "join-room-modal"}
    [:article
     [:header
      [:button {:aria-label "Close"
                :rel "prev"
                :data-target "join-room-modal"
                :onclick "app.modal.toggle_modal(event)"}]
      [:p [:strong "Join a Room!"]]]
     [:form {:action "watch"}
      [:fieldset
       [:label "Room ID"
        [:input {:name "room-id"
                 :placeholder "Room ID"
                 :autocomplete "off"}]]]
      [:input {:type "submit" :value "Join Room"}]]]]])

(def watch-page
  [:html
   head
   [:body
    [:main.container.center
     [:section.center {:id "loading"}
      [:h1
       [:span {:aria-busy "true"}
        "Joining the room..."]]]

     [:section.center {:id "invalid-room" :hidden true}
      [:article
       [:header
        [:p [:strong "Invalid Room ID"]]]

       [:p "The room that you tried to join does not exist. Please check the
            room ID and try again."]
       [:footer
        [:a {:href "./"}
         "Return to home page"]]]]

     [:div {:id "room-wrapper" :hidden true}
      [:section.center {:id "status-section"}
       [:h1 "The broadcast has not started yet"]]
      [:div.center
       [:video.video-player {:id "video-player"
                             :playsinline true
                             :autoplay true
                             :controls true}]]]

     [:script {:src "./js/shared.js"}]
     [:script {:src "./js/streaming.js"}]]]])