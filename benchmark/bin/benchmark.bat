java -version  >> out.txt

set ORDER_COUNT=2000

echo 00
java -jar ..\ujo-orm\target\benchmark.ujorm-1.23-SNAPSHOT.jar 10 >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.23-SNAPSHOT.jar 10 >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.23-SNAPSHOT.jar 10 >> out.txt

echo 01
java -jar ..\ujo-orm\target\benchmark.ujorm-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt

echo 02
java -jar ..\ujo-orm\target\benchmark.ujorm-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt

echo 03
java -jar ..\ujo-orm\target\benchmark.ujorm-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt

echo 04
java -jar ..\ujo-orm\target\benchmark.ujorm-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt

echo 05
java -jar ..\ujo-orm\target\benchmark.ujorm-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.23-SNAPSHOT.jar %ORDER_COUNT% >> out.txt


echo SHUTDOWN -s -t 01


