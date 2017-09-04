#!/bin/bash

cd json

for f in $(find . -name '*.json')
do
    f=${f#./}
    ResourceType=$(dirname $f)
    ResourceId=$(basename $f .json)

    echo "$ResourceType/$ResourceId"
    curl -X PUT "https://sansara.health-samurai.io/$ResourceType/$ResourceId" \
         -H 'content-type: application/json' \
         -d @$f
    echo
    echo
done

cd ..
