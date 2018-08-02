reset
set terminal pdf monochrome enhanced
set key right
set xlabel '{/Symbol x}' font ",18"; 
set yrange [0:0.05];
set xrange [0:360]
set grid xtics ytics;
set key font "Times,18";
set tics font "Times,18";
set output "src/main/resources/plots/faultTree/comparisonSteadyStateProbs.pdf"
plot "src/main/resources/plots/faultTree/repair.dat" using 1:2 title '{/Symbol p}_{repair}' with lines dt 1 lw 1,\
	 "src/main/resources/plots/faultTree/maintenance.dat" using 1:2 title '{/Symbol p}_{maintenance}' with lines dt 2 lw 1,\
	 "src/main/resources/plots/faultTree/sum.dat" using 1:2 title '{/Symbol p}_{repair} + {/Symbol p}_{maintenance}' with lines dt 4 lw 1
