(ns mobile-patient.routes
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.screen.login :refer [LoginScreen]]
            [mobile-patient.screen.demographics :refer [DemographicsScreen]]
            [mobile-patient.screen.vitals :refer [VitalsScreen]]
            [mobile-patient.screen.meds :refer [MedsScreen]]
            [mobile-patient.screen.chat :refer [ChatsScreen ChatScreen]]
            [mobile-patient.screen.settings :refer [SettingsScreen]]
            [mobile-patient.screen.contacts :refer [ContactsScreen]]))


(def react-navigation (js/require "react-navigation"))

(def StackNavigator (.-StackNavigator react-navigation))
(def TabNavigator (.-TabNavigator react-navigation))
(def DrawerNavigator (.-DrawerNavigator react-navigation))
(def DrawerItems (.-DrawerItems react-navigation))

;; NAVIGATION BUTTONS

(defn logout-button [navigation]
  [ui/link {:title "Log Out"
              :on-press #(rf/dispatch [:initialize-db])}])

(defn add-chat-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Contacts")}
   [ui/icon {:name "add-circle" :size 36 :color color/pink :margin-right 8}]])

(defn settings-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Settings")}
   [ui/icon {:name "settings" :size 36 :color color/grey :margin-right 8}]])

(defn chat-buttons [navigation]
  [ui/view {:flex-direction :row :justify-content :space-around :width 86}
   [settings-button navigation]
   [add-chat-button navigation]])


;; NAVIGATION ROUTES

(defn menu-button [navigation]
  [ui/touchable-highlight {:style {:margin-left 10}
                           :on-press #(navigation.navigate "DrawerOpen")}
   [ui/icon {:name "menu" :size 36 :color color/grey}]])

(defn content-comp [props]
  (let [user-id @(subscribe [:user-id])]
    [ui/view {:style {:background-color "#f4f4f4"
                      :flex 1}}
     [ui/view {:style {:background-color "white"
                       :height 100
                       :flex-direction :row
                       :justify-content :center}}
      [ui/view {:style {:width 48
                        :height 48
                        :border-radius 24
                        :background-color "#9e9e9e"
                        :align-self :center}}]
      [ui/text {:style {:font-size 20
                        :margin-left 10
                        :color "black"
                        :align-self :center}} user-id]
      ]
     [DrawerItems props]
     ]))
      

(def drawer-routes
  (DrawerNavigator
   (clj->js
    {"Route 1" {:screen (r/reactify-component (fn [_] [ui/text "Route 1"]))
                :navigationOptions (fn [props]
                                     #js{:title "Route 1"
                                         :headerLeft (r/as-element [menu-button props.navigation])})}

     "Route 2" {:screen (r/reactify-component (fn [_] [ui/text "Route 2"]))
                :navigationOptions (fn [props]
                                     #js{:title "Route 2"
                                         :headerLeft (r/as-element [menu-button props.navigation])})}
     })
   (clj->js
    {:drawerWidth 300
     :contentComponent (fn [props]
                         (r/as-element [content-comp props]))})))

(def tab-routes
  (TabNavigator
   (clj->js
    {"Vitals" {:screen (r/reactify-component VitalsScreen)
               :navigationOptions (fn [props]
                                    #js {:title "Vitals"
                                         :headerRight (r/as-element [logout-button props.navigation])})}

     "Meds"   {:screen (r/reactify-component MedsScreen)
               :navigationOptions (fn [props]
                                    #js {:title "Meds"
                                         :headerRight (r/as-element [logout-button props.navigation])})}

     "Chats"   {:screen (r/reactify-component ChatsScreen)
               :navigationOptions (fn [props]
                                    #js {:title "Chats"
                                         :headerRight (r/as-element [chat-buttons props.navigation])})}})
   (clj->js
    {:tabBarPosition "bottom"
     :tabBarOptions {:activeTintColor "#fff"
                     :inactiveTintColor color/grey
                     :style {:backgroundColor color/light-grey}
                     :tabStyle {:backgroundColor color/grey}
                     :labelStyle {}}
     :initialRouteName "Chats" })))      ;delete

(def routes
  (StackNavigator
   (clj->js
    {"Tabs" {:screen tab-routes}

     "Drawer" {:screen drawer-routes}

     "Settings" {:screen (r/reactify-component SettingsScreen)
                 :headerTintColor color/grey
                 :navigationOptions {:title "Settings"}}

     "Chat" {:screen (r/reactify-component ChatScreen)
             :navigationOptions
             (fn [props]
               (let [chat-name (-> props .-navigation .-state .-params (aget "chat-name"))]
                 #js {:title chat-name}))}

     "Contacts" {:screen (r/reactify-component ContactsScreen)
                 :navigationOptions {:title "Contacts"}}})
   (clj->js
    {:initialRouteName "Tabs"
     :navigationOptions {:headerTintColor color/grey}
     :headerMode :screen})))
