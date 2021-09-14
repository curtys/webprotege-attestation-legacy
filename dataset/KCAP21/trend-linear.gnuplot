set terminal pdf
set datafile separator ','
#set logscale x 10
set auto fix
set xlabel 'Time [ms]'
set ylabel 'Num. elements in ontology'
set key left top
#set offset 100, 100, 0, 0
set title "Hashing time per ontology and number of contained elements" #font "times,12"

stats 'hashing-results.csv' using 2:4 prefix 'A'

TITLE1_1 = "OWL API"
TITLE1_2 = "y = 560.5x + 5.901e+05"
TITLE1_3 = "Corr. r = 0.8951"

plot [2000:5000][1400000:3200000] 'hashing-results.csv' using 2:4 pt 1 notitle with points, '' using 2:4:1 with labels offset 0,1 font "arial,7" tc 'dark-violet' notitle, A_slope*x+A_intercept with lines lw 1 lc 'dark-spring-green' t TITLE1_2, NaN with lines lc 'white' t TITLE1_3
