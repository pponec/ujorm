#!/bin/bash

ORDER_COUNT=2000
RELEASE=1.43-SNAPSHOT

java -version > out.txt

echo 00
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar 10 > out_beg.txt
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar 10 >> out_beg.txt
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar 10 >> out_beg.txt

echo 01
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar $ORDER_COUNT >> out.txt
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar $ORDER_COUNT >> out.txt
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar $ORDER_COUNT >> out.txt

echo 02
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar $ORDER_COUNT >> out.txt
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar $ORDER_COUNT >> out.txt
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar $ORDER_COUNT >> out.txt

echo 03
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar $ORDER_COUNT >> out.txt
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar $ORDER_COUNT >> out.txt
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar $ORDER_COUNT >> out.txt

echo 04
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar $ORDER_COUNT >> out.txt
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar $ORDER_COUNT >> out.txt
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar $ORDER_COUNT >> out.txt

echo 05
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar $ORDER_COUNT >> out.txt
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar $ORDER_COUNT >> out.txt
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar $ORDER_COUNT >> out.txt

echo THE END


