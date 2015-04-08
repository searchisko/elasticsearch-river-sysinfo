#!/bin/sh

## ======================================================
## Stop system info river, rotate indices, update index aliases and restart system into river.
##
## Command line options:
##   - river_name (default: "sysinfo_river")
##   - es_rootURL (default: "http://localhost:9200") URL of node where the sysinfo river is running
##   - custom_key (empty by default) can be used to include cluster name into names
##   - delete_data (empty by default) set day offset to detemine which data to delete (must contain plus symbol for possitive values)
##
## Example: ./rotate.sh sysinfo_river http://localhost:9200 elasticsearch -2
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

old_data_timestamp=`date +"%y-%m-%d"`
if [ -n "$4" ]; then
  unamestr=`uname`
  if [[ "$unamestr" == 'Darwin' ]]; then
    old_data_timestamp=`date -v${4}d +"%Y-%m-%d"` # Mac
  else
    old_data_timestamp=`date --date="${4} days" +"%Y-%m-%d"` # Assume Linux
  fi
fi

## ======================================================
echo "Stop system info river:"
echo "-----------------------"
## ======================================================

curl -XPOST ${es_rootURL}/_river/${river_name}/_mgm_sr/stop

## ======================================================
echo "\n"
echo "Creating new indices and updating aliases:"
echo "----------------------------------------"
## ======================================================

## TODO: use $custom_key
index_names=`curl -s -XGET ${es_rootURL}/_cat/aliases | grep sysinfo_ | grep _index | awk '{print $2}'`

for INFO_TYPE in "${into_types[@]}"
do

  index_base=sysinfo_${INFO_TYPE}
  index_name=${index_base}${custom_key}
  ## TODO: use $custom_key in searching and indexing alias name
  index_alias_search=${index_base}_search
  index_alias_index=${index_base}_index

  for old_index_name in $index_names
  do
    if [[ "$old_index_name" =~ "${INFO_TYPE}" ]]; then
      echo "\nRemove alias '$index_alias_index' from '$old_index_name'"
      curl -XPOST ${es_rootURL}/_aliases -d "{
        \"actions\": [
          { \"remove\": { \"index\": \"${old_index_name}\", \"alias\": \"${index_alias_index}\" }}
        ]
      }"

      echo "Optimize index: ${es_rootURL}/${old_index_name}/_optimize"
      curl -XPOST ${es_rootURL}/${old_index_name}/_optimize

      echo "Create index '${index_name}'"
      #echo ${es_rootURL}/${index_name}
      curl -XPUT ${es_rootURL}/${index_name}

      echo "Setup aliases for index '${index_name}'"
      #echo ${es_rootURL}/_aliases -d "{ \"actions\": [\"${index_name}\", \"${index_alias_index}\", \"${index_alias_search}\"] }"
      curl -XPOST ${es_rootURL}/_aliases -d "{
        \"actions\": [
          { \"add\": { \"index\": \"${index_name}\", \"alias\": \"${index_alias_search}\" }},
          { \"add\": { \"index\": \"${index_name}\", \"alias\": \"${index_alias_index}\" }}
        ]
      }"

    fi
  done
done

## ======================================================
echo "\n"
echo "Delete indices than haven't been update since ${old_data_timestamp}:"
echo "---------------------------------------------------------"
## ======================================================

if [ -n "$4" ]; then
  old_indices_query=`sed s/_timestamp_/${old_data_timestamp}/g queries/not_gte.json`
  indices_to_delete=`curl -s -XGET "${es_rootURL}/_search?pretty&search_type=count" -d "${old_indices_query}" \
    | python -c '
import json,sys
obj=json.load(sys.stdin)
buckets=obj["aggregations"]["indices"]["buckets"]
for x in buckets:
  print x["key"]
'`
  for index in $indices_to_delete
  do
    echo "Delete index '$index'"
    curl -XDELETE ${es_rootURL}/${index}
  done
fi

## ======================================================
echo "\n"
echo "Start system info river:"
echo "------------------------"
## ======================================================

curl -XPOST ${es_rootURL}/_river/${river_name}/_mgm_sr/restart

## ======================================================
echo "\n"
echo "Finished"
## ======================================================