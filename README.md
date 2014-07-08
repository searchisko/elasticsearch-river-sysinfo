System info River for Elasticsearch
===================================

System info river component for [Elasticsearch](http://www.elasticsearch.org) 
collects in defined intervals system informations from Elasticsearch cluster, 
and store them into search indexes, so may be used for later analysis.
System info can be collected from local or remote ES cluster, in case of remote 
cluster REST protocol may be used too to decrease different ES versions impedance.

In order to install the plugin into Elasticsearch, simply run: 
`bin/plugin -url https://repository.jboss.org/nexus/content/groups/public-jboss/org/jboss/elasticsearch/elasticsearch-river-sysinfo/1.2.2/elasticsearch-river-sysinfo-1.2.2.zip -install elasticsearch-river-sysinfo`.


	------------------------------------------------------------------------------------------------------------------
	| Sysinfo River | Elasticsearch    | Release date | Upgrade notes                                                |
	|----------------------------------------------------------------------------------------------------------------|
	| master        | 1.2.0            |              |                                                              |
	|----------------------------------------------------------------------------------------------------------------|
	| 1.2.2         | 0.90.5           | 20.9.2013    |                                                              |
	|----------------------------------------------------------------------------------------------------------------|
	| 1.2.1         | 0.90.0           | 17.5.2013    | Management REST API url's changed                            |
	|----------------------------------------------------------------------------------------------------------------|
	| 1.1.0         | 0.19.11          | 23.11.2012   | river configuration format changed in `indexers` section     |
	|----------------------------------------------------------------------------------------------------------------|
	| 1.0.0         | 0.19.11          | 20.11.2012   |                                                              |
	------------------------------------------------------------------------------------------------------------------

For changelog, planned milestones/enhancements and known bugs see [github issue tracker](https://github.com/searchisko/elasticsearch-river-sysinfo/issues) please.

Creation of the System info river can be done using:

	curl -XPUT localhost:9200/_river/my_sysinfo_river/_meta -d '
	{
	    "type" : "sysinfo",
	    "es_connection" : {
	      "type" : "local"
	    },
	    "indexers" : {
	      "cluster_health" : {
	        "info_type"   : "cluster_health",
	        "index_name"  : "my_index_1",
	        "index_type"  : "my_type_1",
	        "period"      : "1m",
	        "params" : {
	          "level" : "shards"
	        }
	      },
	      "cluster_state" : {
	        "info_type"   : "cluster_state",
	        "index_name"  : "my_index_2",
	        "index_type"  : "my_type_2",
	        "period"      : "1m",
	        "params" : {
	          "metrics" : "nodes"
	        }
	      }
	    }
	}
	'

The example above lists basic configuration used to store two types of information about cluster where river runs. 
Detailed description of configuration follows in next chapters.
Other examples of configuration can be found in [test resources](https://github.com/searchisko/elasticsearch-river-sysinfo/tree/master/src/test/resources).

## Connection to the monitored ES cluster
Connection used to collect ES cluster system informations is configured using 
`es_connection` element. Content depends on type of connection. There are three types available.  

### local
Local mode is used to collect informations about ES cluster where river runs. 
Only `type` option is used here, no any additional configuration parameter necessary.

	"es_connection" : {
	  "type" : "local"
	},

### remote
Remote mode uses [Transport Client](http://www.elasticsearch.org/guide/reference/java-api/client.html) to collect 
system informations from remote ES cluster using internal [Transport](http://www.elasticsearch.org/guide/reference/modules/transport.html) 
mechanism.
You can use this connection if transport mechanism of remote ES cluster version is compatible with version of ES cluster where river runs.  
Configuration requires `address` element with list of remote cluster nodes (both `host` and `port` elements are mandatory). 
Optionally you can define other connection `settings` as described in the [Transport Client documentation](http://www.elasticsearch.org/guide/reference/java-api/client.html). 

	"es_connection" : {
	  "type" : "remote",
	  "addresses" : [
	    {"host": "host1", "port" : "9300"},
	    {"host": "host2", "port" : "9300"}
	  ],
	  "settings" : {
	    "cluster.name" : "myCluster",
	    "client.transport.ping_timeout" : "10"
	  }
 	}

### rest
REST mode uses Elasticsearch [HTTP REST API](http://www.elasticsearch.org/guide/reference/modules/http.html) 
to collect system informations from remote ES cluster.
You can use this connection mode in case of compatibility or networking problems with `remote` mode. 
Note that performance of REST API is commonly worse than binary transport mechanism behind `remote` mode.

	"es_connection" : {
	  "type"     : "rest",
	  "urlBase"  : "http://localhost:9200",
	  "timeout"  : "1s",
	  "username" : "myusername",
	  "pwd"      : "mypassword"
	 }

Configuration options:

* `urlBase` mandatory base URL of remote ES cluster to be used for http(s) REST API calls.
* `timeout` optional timeout for http(s) requests, default 5 second.
* `username` optional username for http basic authentication.
* `pwd` optional password for http basic authentication.

## Configuration of indexers
Second significant part of the river configuration is map of `indexers`. Each indexer defines what 
information will be collected in which interval, and where will be stored in ES indexes.
Each indexer has unique name defined as key in map of indexers.
Information is stored to the ES indexes in cluster where river runs. Structure of stored
information is exactly same as returned from ElasticSearch API call.
Indexer configuration is:

	"indexer_name" : {
	  "info_type"  : "cluster_health",
	  "index_name" : "my_index_1",
	  "index_type" : "my_type_1",
	  "period"     : "1m",
	  "params"     : {
	      "level" : "shards"
	  }
	}

Configuration options:
	
* `info_type` mandatory type of information collected by this indexer. See table below for list of all available types. You can create more indexers with same type.
* `index_name` mandatory name of index used to store information. Note that this river can produce big amount of data over time, so consider use of [rolling index](http://github.com/elasticsearch/elasticsearch/issues/1500) here.
* `index_type` mandatory [type](http://www.elasticsearch.org/guide/appendix/glossary.html#type) used to stored information into search index. You should define [Mapping](http://www.elasticsearch.org/guide/reference/mapping/) for this type. You should enable [Automatic Timestamp Field](http://www.elasticsearch.org/guide/reference/mapping/timestamp-field.html) in this mapping to have consistent timestamp available in stored data.
* `period` mandatory period of information collecting in milliseconds. You can use postfixes appended to the number to define units: `s` for seconds, `m` for minutes, `h` for hours, `d` for days and `w` for weeks. So for example value `5h` means five fours, `2w` means two weeks.
* `params` optional map of additional parameters to narrow down collected information. Available parameters depend on `info_type`, and can be found as 'Request parameters' in relevant ES API doc for each type. Some additional parameters (passed as URL parts in API doc) are described in note, see table below.

Available information types:

	--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	| info_type           | relevant ES API doc                                                                            | note                                                                                            |  
	|---------------------|------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------| 
	| cluster_health      | http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-health.html      | `index` param                                                                                   |
	|---------------------|------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
	| cluster_state       | http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-state.html       | `indices`, `metrics` param for ES 1.2, use of `metadata` metric may bring performance problems! |
	|---------------------|------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
	| cluster_nodes_info  | http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-nodes-info.html  | `nodeId` param. `metrics` param for ES 1.2                                                      |
	|---------------------|------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
	| cluster_nodes_stats | http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-nodes-stats.html | `nodeId` param. `metric`, `indexMetric`, `fields` params for ES 1.2                             |
	|---------------------|------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
	| indices_status      | http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices-status.html      | `index` param                                                                                   |
	|---------------------|------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
	| indices_stats       | http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices-stats.html       | `index` param. `metric`, `indexMetric` params for ES 1.2                                        |
	|---------------------|------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
	| indices_segments    | http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices-segments.html    | `index` param                                                                                   |
	--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  
Management REST API
-------------------
Sysinfo river supports next REST commands for management purposes. Note `my_sysinfo_river`
in examples is name of Sysinfo river you can call operation for, so replace it with real 
name for your calls.

Stop Sysinfo river indexing process. Process is stopped temporarily, so after complete 
elasticsearch cluster restart or river migration to another node it's started back.

	curl -XPOST localhost:9200/_river/my_sysinfo_river/_mgm_sr/stop

Restart Sysinfo river indexing process. Configuration of river is reloaded during restart. 
You can restart running indexing, or stopped indexing (see previous command).

	curl -XPOST localhost:9200/_river/my_sysinfo_river/_mgm_sr/restart
	
Change indexing period for named indexers (indexers are named in url and comma 
separated, see `cluster_health,cluster_state` in example below). Change is not 
persistent, it's back on value from river configuration file after river restart!
	
	curl -XPOST localhost:9200/_river/my_sysinfo_river/_mgm_sr/cluster_health,cluster_state/period/2s

List names of all Sysinfo Rivers running in ES cluster.

	curl -XGET localhost:9200/_sysinfo_river/list


License
-------

    This software is licensed under the Apache 2 license, quoted below.

    Copyright 2012-2014 Red Hat Inc. and/or its affiliates and other contributors as indicated by the @authors tag. 
    All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
	