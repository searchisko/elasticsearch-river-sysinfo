#!/bin/sh

## ======================================================
## Prepare initial structure of index templates, indices, index aliases and configure and start system info river.
##
## Command line options:
##   - river_name (default: "sysinfo_river")
##   - es_rootURL (default: "http://localhost:9200") URL of node where the sysinfo river is running
##   - custom_key (empty by default) can be used to include cluster name into names
##
## Example: ./init.sh sysinfo_river http://localhost:9200 elasticsearch
## ======================================================

into_types=( cluster_health cluster_state cluster_stats pending_cluster_tasks cluster_nodes_info cluster_nodes_stats indices_stats indices_recovery )
timestamp=`date +"%y-%m-%d-%s"`

river_name=sysinfo_river
if [ -n "$1" ]; then
  river_name=$1
fi

es_rootURL=http://localhost:9200
if [ -n "$2" ]; then
  es_rootURL=$2
fi

custom_key_index=
custom_key=_${timestamp}
if [ -n "$3" ]; then
  custom_key_index=_$3
  custom_key=_$3${custom_key}
fi

## ======================================================
echo "Pushing index templates:"
echo "------------------------"
## ======================================================

for filename in index_templates/*.json
do
  template="${filename#index_templates/}"
  template="${template%.*}"
  echo "\nCreate template '${template}'"
  #echo ${es_rootURL}/_template/${template}/ -d@${filename}
  curl -XPUT ${es_rootURL}/_template/${template}/ -d@${filename}
done

## ======================================================
echo "\n"
echo "Creating initial indices and aliases:"
echo "-------------------------------------"
## ======================================================

for INFO_TYPE in "${into_types[@]}"
do
  index_base=sysinfo_${INFO_TYPE}
  index_name=${index_base}${custom_key}
  ## TODO: use $custom_key in searching and indexing alias name
  index_alias_search=${index_base}_search
  index_alias_index=${index_base}_index

  #echo ${es_rootURL}/${index_name}
  curl -XPUT ${es_rootURL}/${index_name}

  echo "\nSetup aliases for index '${index_name}'"
  #echo ${es_rootURL}/_aliases -d "{ \"actions\": [\"${index_name}\", \"${index_alias_index}\", \"${index_alias_search}\"] }"
  curl -XPOST ${es_rootURL}/_aliases -d "{
    \"actions\": [
      { \"add\": { \"index\": \"${index_name}\", \"alias\": \"${index_alias_search}\" }},
      { \"add\": { \"index\": \"${index_name}\", \"alias\": \"${index_alias_index}\" }}
    ]
  }"
done

## ======================================================
echo "\n"
echo "Pushing system info river configuration:"
echo "----------------------------------------"
## ======================================================

river_config=`sed s/_custom_key/${custom_key_index}/g river_config/sysinfo_river_config.json`
#echo ${es_rootURL}/_river/${river_name}/_meta -d "'${river_config}'"
curl -XPUT ${es_rootURL}/_river/${river_name}/_meta -d "${river_config}"

## ======================================================
echo "\n"
echo "Finished"
## ======================================================