clear
reset
set key off
set border 3
set auto
 
set xrange[0:1]
set xtics auto
set ytics auto
#set yrange[0:32]
# Make some suitable labels.
set title "Edge Ratio plot for 100 random graphs and the given graph"
set xlabel "Edge Ratio"
set ylabel "Frequency"
 
set terminal png enhanced font arial 14 size 2020, 1024
ft="png"
# Set the output-file name.
set output "EdgeRatioPlot.".ft
 
set style histogram clustered gap 1
set style fill solid border -1
set xtics border in scale 0,0 nomirror rotate by 0  offset character 0, 0, 0
binwidth=0.005
set boxwidth binwidth
bin(x,width)=width*floor(x/width) + binwidth/2.0
set autoscale ymax
#set autoscale x	

plot 'EdgeRatioPlot.dat' using (bin($1,binwidth)):(1.0) smooth freq with boxes,\
'EdgeRatioPlot.dat' using (bin($1,binwidth)):(1.0) smooth freq axis x1y1 with l lt 2 lw 2


