#!/bin/bash

ORDER_COUNT=2000
RELEASE=1.45-SNAPSHOT
FLUSH_MODE=
BEG_FILE=out_beg.txt
OUT_FILE=out.txt

java -version > $OUT_FILE

echo 00
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar 10 > ${BEG_FILE}
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar 10 $FLUSH_MODE >> ${BEG_FILE}
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar 10 $FLUSH_MODE >> ${BEG_FILE}

echo 01
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar $ORDER_COUNT >> ${OUT_FILE}
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar $ORDER_COUNT $FLUSH_MODE >> ${OUT_FILE}
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar $ORDER_COUNT $FLUSH_MODE >> ${OUT_FILE}

echo 02
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar $ORDER_COUNT >> ${OUT_FILE}
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar $ORDER_COUNT $FLUSH_MODE >> ${OUT_FILE}
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar $ORDER_COUNT $FLUSH_MODE >> ${OUT_FILE}

echo 03
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar $ORDER_COUNT >> ${OUT_FILE}
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar $ORDER_COUNT $FLUSH_MODE >> ${OUT_FILE}
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar $ORDER_COUNT $FLUSH_MODE >> ${OUT_FILE}

echo 04
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar $ORDER_COUNT >> ${OUT_FILE}
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar $ORDER_COUNT $FLUSH_MODE >> ${OUT_FILE}
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar $ORDER_COUNT $FLUSH_MODE >> ${OUT_FILE}

echo 05
java -server -jar ../ujo-orm/target/benchmark.ujorm-${RELEASE}.jar $ORDER_COUNT >> ${OUT_FILE}
java -server -jar ../hibernate/target/benchmark.hibernate.pojo-${RELEASE}.jar $ORDER_COUNT $FLUSH_MODE >> ${OUT_FILE}
java -server -jar ../hibernate-ujo/target/benchmark.hibernate.ujo-${RELEASE}.jar $ORDER_COUNT $FLUSH_MODE >> ${OUT_FILE}

echo THE END


