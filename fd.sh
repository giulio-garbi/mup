for f in 1-1-1-1-1-1-1-1-1 2-1-1-1-1-1-1-1-1 4-1-1-1-1-1-1-1-1 8-1-1-1-1-1-1-1-1 16-1-1-1-1-1-1-1-1 32-1-1-1-1-1-1-1-1 64-1-1-1-1-1-1-1-1 128-1-1-1-1-1-1-1-1 256-1-1-1-1-1-1-1-1 512-1-1-1-1-1-1-1-1 32-2-1-1-1-1-1-1-1 64-2-1-1-1-1-1-1-1 64-4-1-1-1-1-1-1-1 128-4-1-1-1-1-1-1-1 128-8-1-1-1-1-1-1-1 128-16-1-1-1-1-1-1-1 128-16-1-1-1-2-1-1-1 128-16-1-2-1-2-1-1-1 256-16-1-2-1-2-1-1-1 256-32-1-2-1-2-1-1-1 256-64-1-2-1-2-1-1-1 256-128-1-2-1-2-1-1-1 256-128-1-2-1-4-1-1-1 256-128-1-4-1-4-1-2-1 32-1-2-1-1-1-1-1-1 64-1-2-1-1-1-1-1-1 64-1-4-1-1-1-1-1-1 128-1-4-1-1-1-1-1-1 128-1-8-1-1-1-1-1-1 128-1-8-1-1-1-2-1-1 128-1-8-1-2-1-2-1-1 256-1-8-1-2-1-2-1-1 256-1-16-1-2-1-2-1-1 256-1-32-1-2-1-2-1-1 256-1-32-1-2-1-4-1-1 256-1-32-1-4-1-4-1-1 256-1-32-1-4-1-4-1-2
do
	f1=`echo $f | cut -d '-' -f 1`
	f2=`echo $f | cut -d '-' -f 2`
	f3=`echo $f | cut -d '-' -f 3`
	f4=`echo $f | cut -d '-' -f 4`
	f5=`echo $f | cut -d '-' -f 5`
	f6=`echo $f | cut -d '-' -f 6`
	f7=`echo $f | cut -d '-' -f 7`
	f8=`echo $f | cut -d '-' -f 8`
	f9=`echo $f | cut -d '-' -f 9`
	java -Djava.compiler=NONE -Xint -jar fd.jar ~/mupdata/fd-$f-0.5.json $f1 $f2 $f3 $f4 $f5 $f6 $f7 $f8 $f9 0.5 3000 3750 525
done
 
java -jar modeltraces.jar util ~/mupdata/fd-1-1-1-1-1-1-1-1-1-0.5.json ~/mupdata/fd-1-1-1-1-1-1-1-1-1-0.5.util.csv
java -jar modeltraces.jar rt ~/mupdata/fd-1-1-1-1-1-1-1-1-1-0.5.json ~/mupdata/fd-1-1-1-1-1-1-1-1-1-0.5.rt.csv
java -jar modeltraces.jar make ~/mupdata/fd-1-1-1-1-1-1-1-1-1-0.5.json ../FaceDetect ~/mupdata/fd-1-1-1-1-1-1-1-1-1-0.5.lqnx
java -jar lqnexec.jar ~/mupdata/fd-1-1-1-1-1-1-1-1-1-0.5.lqnx 1200 ~/mupdata/fd-1-1-1-1-1-1-1-1-1-0.5.rtsim.csv ~/mupdata/fd-1-1-1-1-1-1-1-1-1-0.5.utilsim.csv

for f in 2-1-1-1-1-1-1-1-1 4-1-1-1-1-1-1-1-1 8-1-1-1-1-1-1-1-1 16-1-1-1-1-1-1-1-1 32-1-1-1-1-1-1-1-1 64-1-1-1-1-1-1-1-1 128-1-1-1-1-1-1-1-1 256-1-1-1-1-1-1-1-1 512-1-1-1-1-1-1-1-1 32-2-1-1-1-1-1-1-1 64-2-1-1-1-1-1-1-1 64-4-1-1-1-1-1-1-1 128-4-1-1-1-1-1-1-1 128-8-1-1-1-1-1-1-1 128-16-1-1-1-1-1-1-1 128-16-1-1-1-2-1-1-1 128-16-1-2-1-2-1-1-1 256-16-1-2-1-2-1-1-1 256-32-1-2-1-2-1-1-1 256-64-1-2-1-2-1-1-1 256-128-1-2-1-2-1-1-1 256-128-1-2-1-4-1-1-1 256-128-1-4-1-4-1-2-1 32-1-2-1-1-1-1-1-1 64-1-2-1-1-1-1-1-1 64-1-4-1-1-1-1-1-1 128-1-4-1-1-1-1-1-1 128-1-8-1-1-1-1-1-1 128-1-8-1-1-1-2-1-1 128-1-8-1-2-1-2-1-1 256-1-8-1-2-1-2-1-1 256-1-16-1-2-1-2-1-1 256-1-32-1-2-1-2-1-1 256-1-32-1-2-1-4-1-1 256-1-32-1-4-1-4-1-1 256-1-32-1-4-1-4-1-2
do
	f1=`echo $f | cut -d '-' -f 1`
	f2=`echo $f | cut -d '-' -f 2`
	f3=`echo $f | cut -d '-' -f 3`
	f4=`echo $f | cut -d '-' -f 4`
	f5=`echo $f | cut -d '-' -f 5`
	f6=`echo $f | cut -d '-' -f 6`
	f7=`echo $f | cut -d '-' -f 7`
	f8=`echo $f | cut -d '-' -f 8`
	f9=`echo $f | cut -d '-' -f 9`
	java -jar modeltraces.jar util ~/mupdata/fd-$f-0.5.json ~/mupdata/fd-$f-0.5.util.csv
	java -jar modeltraces.jar rt ~/mupdata/fd-$f-0.5.json ~/mupdata/fd-$f-0.5.rt.csv
	java -jar whatif.jar ~/mupdata/fd-1-1-1-1-1-1-1-1-1-0.5.lqnx c $f1 v frontend $f2 h frontend $f3 v backend0 $f4 h backend0 $f5 v backend1 $f6 h backend1 $f7 v storage $f8 h storage $f9 ~/mupdata/fd-$f-0.5.lqnx
	java -jar lqnexec.jar ~/mupdata/fd-$f-0.5.lqnx 1200 ~/mupdata/fd-$f-0.5.rtsim.csv ~/mupdata/fd-$f-0.5.utilsim.csv
done