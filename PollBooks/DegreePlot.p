clear
reset
set key off
set border 3
set auto
 
#set xrange[0:30]
set xtics auto
set ytics auto
#set yrange[0:50]
# Make some suitable labels.
set title "Frequency Distribution for Degrees in the given graph"
set xlabel "Degree"
set ylabel "Frequency"
 
set terminal png enhanced font arial 14 size 2020, 1024
ft="png"
# Set the output-file name.
set output "Degree.".ft
 
set style histogram clustered gap 1
set style fill solid border -1
set xtics border in scale 0,0 nomirror rotate by 0  offset character 0, 0, 0
binwidth=1
set boxwidth binwidth
bin(x,width)=width*floor(x/width) + binwidth/2.0
set autoscale
plot 'DegreePlot.dat' using (bin($1,binwidth)):(1.0) smooth freq with boxes
