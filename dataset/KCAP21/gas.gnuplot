set terminal pdf
set datafile separator ','
set key left top
set title "Cost of ontology attestation in USD since Jan. 2020" #font "times,12"

stats 'result-gas.csv' using 1:2 prefix 'A'

set xdata time
set timefmt "%m/%d/%Y"
set format x "%m/%y"
set xrange ['1/1/2020':'10/1/2021']
set yrange [0.01:1000]

set logscale y
set style fill pattern 21

set ylabel 'Transaction Cost [USD] (log10)'
set xlabel 'Date [month/year]'

PRIORITY_FEE = 2*1e+9
vec(date_str) = strptime('%m/%d/%Y', date_str) == strptime('%m/%d/%Y', '8/5/2021') ? 0.001 : NaN
gas(date_str, gas, limit, price) = strptime('%m/%d/%Y', date_str) < strptime('%m/%d/%Y', '8/5/2021') ? (gas*limit)*1e-18*price : (limit*(gas+PRIORITY_FEE))*1e-18*price 

plot 'export-eth-merged.csv' using 1:(gas(strcol(1), $3, A_max_x, $4)) with lines t 'Mean cost for attestation', \
    '' using 1:(gas(strcol(1), $3, A_max_y, $4)) with lines t 'Baseline transaction cost', \
    '' using 1:(vec(strcol(1))):1:(1000) with vectors nohead lw 1 dt '...' lc rgb "red" t "London Upgrade"
