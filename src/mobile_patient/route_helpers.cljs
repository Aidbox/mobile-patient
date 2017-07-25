(ns mobile-patient.route-helpers
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]))

;; NAVIGATION BUTTONS

(defn add-chat-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Contacts")
                           :style {:margin-right 20}
                           :underlay-color color/grey}
   [ui/icon {:name "add-circle" :size 36 :color color/pink}]])

;; NAVIGATION ROUTES

(defn menu-button [navigation]
  [ui/touchable-highlight {:style {:margin-left 10}
                           :on-press #(navigation.navigate "DrawerOpen")
                           :underlay-color color/grey}
   [ui/icon {:name "menu" :size 24 :color :black}]])


(defn stack-navigator [routes]
  (r/reactify-component
   (ui/StackNavigator (clj->js routes)
                      (clj->js {;;:initialRouteName "Contacts" ;;for dev
                                :headerTitleStyle {:fontWeight :normal}}))))

(defn stack-navigator-back-button [props]
  (r/as-element
   [ui/touchable-highlight {:on-press #(props.navigation.goBack nil)
                            :style {:margin-left 5}
                            :underlay-color color/grey}
    [ui/icon {:name "chevron-left" :size 36 :color color/pink}]]))


(defn header [props]
  (print (js/Object.keys props))
  [ui/view
   ])

(defn drawer-nav-opts
  ([title]
   (drawer-nav-opts title nil))
  ([title header-right]
   (fn [props]
     #js{:title title
         ;;:header (fn [props]  (r/as-element (header props)))
         :headerLeft (r/as-element [menu-button props.navigation])
         :headerStyle #js{:padding 20
                          :elevation 0
                          :shadowOffset #js {:height 0}
                          :shadowColor "red"
                          :height 100
                          }
         :headerTitleStyle #js{:textAlign "center"
                               :paddingLeft 10
                               :fontSize 19
                               :fontWeight "bold"
                               :color color/pink}
         :headerRight (when header-right (r/as-element [header-right props.navigation]))})))
