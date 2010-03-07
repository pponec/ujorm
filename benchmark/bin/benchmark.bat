java -version  >> out.txt

echo 00
java -jar ..\hibernate\target\benchmark.hibernate.pojo-0.93.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-0.93.jar >> out.txt
java -jar ..\ujo-orm\target\benchmark.ujorm-0.93.jar >> out.txt

echo 01
java -jar ..\hibernate\target\benchmark.hibernate.pojo-0.93.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-0.93.jar >> out.txt
java -jar ..\ujo-orm\target\benchmark.ujorm-0.93.jar >> out.txt

echo 02
java -jar ..\hibernate\target\benchmark.hibernate.pojo-0.93.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-0.93.jar >> out.txt
java -jar ..\ujo-orm\target\benchmark.ujorm-0.93.jar >> out.txt

echo 03
java -jar ..\hibernate\target\benchmark.hibernate.pojo-0.93.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-0.93.jar >> out.txt
java -jar ..\ujo-orm\target\benchmark.ujorm-0.93.jar >> out.txt

echo 04
java -jar ..\hibernate\target\benchmark.hibernate.pojo-0.93.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-0.93.jar >> out.txt
java -jar ..\ujo-orm\target\benchmark.ujorm-0.93.jar >> out.txt

echo 05
java -jar ..\hibernate\target\benchmark.hibernate.pojo-0.93.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-0.93.jar >> out.txt
java -jar ..\ujo-orm\target\benchmark.ujorm-0.93.jar >> out.txt


SHUTDOWN -s -t 01


