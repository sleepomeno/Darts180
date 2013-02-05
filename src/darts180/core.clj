(ns darts180.core
  (:gen-class)
  (:use [seesaw [core :as s]
                [graphics :as g]]))

(defn draw-arc "Draws a pizza segment" [graphics position radius angle]
  (.fillArc graphics position position radius radius angle 18))

(defn draw-board "Draws the dart board" [c graphics]
  (let [w (.getWidth c) w2 (/ w 2)
        h (.getHeight c) h2 (/ h 2)
        min-w2-h2 (min w2 h2)
        doubles-radius (min w h)
        outer-singles-radius (-> doubles-radius (/ 100) (* 95))
        outer-singles-center (-> doubles-radius (- outer-singles-radius) (/ 2))
        triples-radius (-> doubles-radius (/ 100) (* 60))
        triples-center (-> doubles-radius (- triples-radius) (/ 2))
        inner-singles-radius (-> doubles-radius (/ 100) (* 55))
        inner-singles-center (-> doubles-radius (- inner-singles-radius) (/ 2))
        semi-radius (-> doubles-radius (/ 100) (* 5))
        bull-radius (-> doubles-radius (/ 100) (* 2)) 
        black (java.awt.Color/BLACK)
        bright (java.awt.Color. 247 236 170)
        red (java.awt.Color/RED)
        green (java.awt.Color/GREEN)
        semi-style (g/style :background green :foreground black)
        bull-style (g/style :background red :foreground black)]
    (doseq [x (range 1 21) :let [angle (-> x (* 18) (- 10))
                                 singles-color (if (even? x) black bright)
                                 doubles-color (if (even? x) red green)]]
      (doto graphics
        (.setColor doubles-color)
        (draw-arc 0 doubles-radius angle)
        (.setColor singles-color) 
        (draw-arc outer-singles-center outer-singles-radius angle)
        (.setColor doubles-color)
        (draw-arc triples-center triples-radius angle)
        (.setColor singles-color) 
        (draw-arc inner-singles-center inner-singles-radius angle)
        ))
    (g/draw graphics (g/circle min-w2-h2 min-w2-h2 semi-radius) semi-style)
    (g/draw graphics (g/circle min-w2-h2 min-w2-h2 bull-radius) bull-style)))

(def board-canvas (s/canvas :id :board :paint draw-board))
(def my-frame (s/frame :on-close :exit :title "Darts180" :content board-canvas))

(defn start-app "Starts the Swing application" []
  (s/invoke-later
    (-> my-frame pack! show!)))

(defn -main
  "Run the application"
  [& args]
  (start-app))
