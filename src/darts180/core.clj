(ns darts180.core
  (:gen-class)
  (:use [seesaw [core :as s]
                [graphics :as g]]))

(def board-center (atom [0 0 ]))

(defn draw-arc "Draws a pizza segment" [graphics position radius x color]
  (doto graphics 
    (.setColor color)
    (.fillArc position position radius radius (-> x (* 18) (- 10)) 18)))

(defn draw-board "Draws the dart board" [widget graphics]
  (let [w (.getWidth widget) w2 (/ w 2)
        h (.getHeight widget) h2 (/ h 2)
        min-w2-h2 (min w2 h2)
        doubles-diameter (min w h)
        _ (reset! board-center [(/ doubles-diameter 2) (/ doubles-diameter 2)])
        outer-singles-diameter (-> doubles-diameter (/ 100) (* 95))
        outer-singles-center (-> doubles-diameter (- outer-singles-diameter) (/ 2))
        triples-diameter (-> doubles-diameter (/ 100) (* 60))
        triples-center (-> doubles-diameter (- triples-diameter) (/ 2))
        inner-singles-diameter (-> doubles-diameter (/ 100) (* 55))
        inner-singles-center (-> doubles-diameter (- inner-singles-diameter) (/ 2))
        semi-diameter (-> doubles-diameter (/ 100) (* 5))
        bull-diameter (-> doubles-diameter (/ 100) (* 2)) 
        black (java.awt.Color/BLACK)
        bright (java.awt.Color. 247 236 170)
        red (java.awt.Color/RED)
        green (java.awt.Color/GREEN)
        semi-style (g/style :background green :foreground black)
        bull-style (g/style :background red :foreground black)]
    (doseq [x (range 1 21) :let [singles-color (if (even? x) black bright)
                                 doubles-color (if (even? x) red green)]]
      (doto graphics
        (draw-arc 0 doubles-diameter x doubles-color)
        (draw-arc outer-singles-center outer-singles-diameter x singles-color)
        (draw-arc triples-center triples-diameter x doubles-color)
        (draw-arc inner-singles-center inner-singles-diameter x singles-color)))
    (g/draw graphics (g/circle min-w2-h2 min-w2-h2 semi-diameter) semi-style)
    (g/draw graphics (g/circle min-w2-h2 min-w2-h2 bull-diameter) bull-style)))

(defn log-score "Log the on-mouse-over score of the board in the label" [board a-label]
  (listen board :mouse-moved 
          (fn [e] (let [x (.getX e)
                        y (.getY e)
                        [center-x center-y] @board-center
                        distance-x (- x center-x) distance-y (- y center-y)
                        distance (Math/sqrt (+ (Math/pow distance-x 2) (Math/pow distance-y 2)))]
                    (text! a-label (str "X: " (.getX e) ", Y: " (.getY e) ", Distance: " distance))))))

(def board-canvas (s/canvas :background "#BBBBDD" :id :board :paint draw-board))

(defn redraw-board "Redraws the board canvas" []
  (config! board-canvas :paint draw-board))

(def score-label (label :text "SCORE"))
(def content-panel (s/border-panel :hgap 10 :vgap 10 :center board-canvas :east score-label ))

(def my-frame (s/frame :width 500 :height 500 :title "Darts180" :content content-panel))

(defn log "Log" []
  (def my-log (log-score board-canvas score-label)))

(defn start-app "Starts the Swing application" []
  (s/native!)
  (s/invoke-later (-> my-frame pack! show!)))

(defn -main
  "Run the application"
  [& args]
  (start-app))
