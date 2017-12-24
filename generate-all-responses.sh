#!/usr/bin/env bash

current=`pwd`

dir="${current}"
if [ -d "$1" ]; then
  cd "$1" || exit 1
  dir="${1%/}"
fi

if [ -d "$2" ]; then
       output="${2%/}"
    else
       echo "output dir ${2} doesn't exist"
       exit 1
fi

echo "**********************"
echo "input dir: $dir"
echo "output dir: $output"
echo "**********************"

FILES=*.jmx
for f in ${FILES}
do
  nameOfFile=$(basename "$f")
  nameOfFile="${nameOfFile%.*}"

  if [ -f "$f" ]; then
    echo "Processing $f file..."
    file="${f}"
    if [ -f "${current}/${dir}/${f}" ]; then
       file="${current}/${dir}/${f}"
    elif [ -f "${dir}/${f}" ]; then
        file="${dir}/${f}"
    fi

    if [ "$3" != "--nogen" ]; then
        if [ -f "${output}/${nameOfFile}.xml" ]; then
            rm "${output}/${nameOfFile}.xml"
        fi
    fi

    if [ "$3" != "--nogen" ]; then
        echo ">> generating output for file ${file} and storing it to output dir ${output}"
        jmeter -JclusterRegionserver=localhost -Jjmeter.save.saveservice.output_format=xml -Jjmeter.save.saveservice.data_type=true -Jjmeter.save.saveservice.latency=true -Jjmeter.save.saveservice.response_data=true -Jjmeter.save.saveservice.successful=true -Jjmeter.save.saveservice.bytes=true -Jjmeter.save.saveservice.response_code=true -Jjmeter.save.saveservice.thread_name=true -Jjmeter.save.saveservice.time=true -Jjmeter.save.saveservice.assertions=true -Jjmeter.save.saveservice.label=true -Jjmeter.save.saveservice.response_message=true -Jjmeter.save.saveservice.subresults=true -Jjmeter.save.saveservice.thread_counts=true -n -t ${file} -l ${output}/${nameOfFile}.xml
    fi


    echo ">> ${current}/target/add-assertions.jar -i ${file} -o ${output}/${nameOfFile}.jmx -e ${output}/${nameOfFile}.xml"
    ${current}/target/add-assertions.jar -i ${file} -o ${output}/${nameOfFile}.jmx -e ${output}/${nameOfFile}.xml
    echo
  fi
done