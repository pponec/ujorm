java -version  >> out.txt

echo 00
java -jar ..\ujo-orm\target\benchmark.ujorm-1.00.jar >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.00.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.00.jar >> out.txt

echo 01
java -jar ..\ujo-orm\target\benchmark.ujorm-1.00.jar >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.00.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.00.jar >> out.txt

echo 02
java -jar ..\ujo-orm\target\benchmark.ujorm-1.00.jar >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.00.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.00.jar >> out.txt

echo 03
java -jar ..\ujo-orm\target\benchmark.ujorm-1.00.jar >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.00.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.00.jar >> out.txt

echo 04
java -jar ..\ujo-orm\target\benchmark.ujorm-1.00.jar >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.00.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.00.jar >> out.txt

echo 05
java -jar ..\ujo-orm\target\benchmark.ujorm-1.00.jar >> out.txt
java -jar ..\hibernate\target\benchmark.hibernate.pojo-1.00.jar >> out.txt
java -jar ..\hibernate-ujo\target\benchmark.hibernate.ujo-1.00.jar >> out.txt


SHUTDOWN -s -t 01


