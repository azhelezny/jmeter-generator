#!/usr/bin/env bash

current=`pwd`

templateRun=""
if [ -f "$2" ]; then
    template="$2"
    if [ -f "${current}/${template}" ]; then
       template="${current}/${template}"
    fi

    echo "Using template $template"

    templateRun="-t $template"
fi

dir="${current}"
if [ -d "$1" ]; then
  cd "$1" || exit 1
  dir="$1"
fi

echo
pwd
echo ${current}
FILES=*.sql
for f in ${FILES}
do
  echo "PISH: $f"
  if [ -f "$f" ]; then
    echo "Processing $f file..."
    file="${f}"
    if [ -f "${current}/${dir}/${f}" ]; then
       file="${current}/${dir}/${f}"
    elif [ -f "${dir}/${f}" ]; then
        file="${dir}/${f}"
    fi

    echo ">> ${current}/target/jmeter-test-generator.jar -s ${file} -j ${file}.jmx ${templateRun}"
    ${current}/target/jmeter-test-generator.jar -s "${file}" -j "${file}.jmx" ${templateRun}
    echo
  fi
done