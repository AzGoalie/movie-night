(ns app.ui.theme-switch)

(def theme-key "picoPreferredColorScheme")
(def toggle-id "theme-toggle")

#?(:cljs
   (defn get-prefered-theme []
     (let [default-theme (if (.-matches (js/window.matchMedia "(prefers-color-scheme: light)")) "light" "dark")
           stored-theme (.getItem js/window.localStorage theme-key default-theme)]
       (or stored-theme default-theme))))
#?(:cljs
   (def state (atom (get-prefered-theme))))
#?(:cljs
   (defn set-theme! [theme]
     (.setAttribute (.-documentElement js/document) "data-theme" theme)
     (reset! state theme)
     (.setItem js/window.localStorage theme-key theme)))
#?(:cljs
   (defn ^:export toggle-theme []
     (let [current-theme @state]
       (if (= "dark" current-theme)
         (set-theme! "light")
         (set-theme! "dark")))))
#?(:cljs
   (when (= "light" @state)
     (println "init!")
     (set-theme! "light")
     (set! (.-checked (.getElementById js/document toggle-id)) true)))

(def theme-switch
  [:label.theme-toggle {:title "Toggle theme"}
   [:input {:id toggle-id :type "checkbox" :onclick "app.ui.theme_switch.toggle_theme()"}]
   [:span.theme-toggle-sr "Toggle theme"]
   [:svg.theme-toggle__classic {:xmlns "http://www.w3.org/2000/svg"
                                :aria-hidden "true"
                                :width "1em"
                                :height "1em"
                                :fill "currentColor"
                                :stroke-linecap "round"
                                :viewBox "0 0 32 32"}
    [:clippath#theme-toggle__classic__cutout
     [:path {:d "M0-5h30a1 1 0 0 0 9 13v24H0Z"}]]
    [:g {:clip-path "url(#theme-toggle__classic__cutout)"}
     [:circle {:cx "16" :cy "16" :r "9.34"}]
     [:g {:stroke "currentColor" :stroke-width "1.5"}
      [:path {:d "M16 5.5v-4"}]
      [:path {:d "M16 30.5v-4"}]
      [:path {:d "M1.5 16h4"}]
      [:path {:d "M26.5 16h4"}]
      [:path {:d "m23.4 8.6 2.8-2.8"}]
      [:path {:d "m5.7 26.3 2.9-2.9"}]
      [:path {:d "m5.8 5.8 2.8 2.8"}]
      [:path {:d "m23.4 23.4 2.9 2.9"}]]]]])
