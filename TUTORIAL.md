# Practical sysinfo river setup and indices management

## Abstract

This tutorial explains sysinfo river **setup** and **index management** best practices.
By the end of this document we will have a simple system that collects information from
Elasticsearch cluster and allows analysis of trends and investigation of historical data.

Particular topics we will discuss in details:

- How to collect and store most of the system information from Elasticsearch cluster
- Fine tuning of mappings for stored data to make it more aggregation friendly
- Removing old data (index rotation)

## Content

- [Requirements](#requirements)
- [Index naming conventions](#index-naming-conventions)
- [Mappings and Transformations](#mappings-and-transformations)
    - [`sysinfo_`](#sysinfo_)
    - [`{info_type}`](#info_type)
    - [`{custom_key}`](#custom_key)
- [Index alias naming conventions](#index-alias-naming-conventions)
    - [Index alias for search](#index-alias-for-search)
    - [Index alias for indexing](#index-alias-for-indexing)
- [Index rotation](#index-rotation)
    - [Adding new indices](#adding-new-indices)
    - [Remove old indices](#remove-old-indices)
  

## Requirements

First we need Elasticsearch cluster to host sysinfo river plugin. We will use
Elasticsearch `1.4.1`. It is recommended to use fresh dedicated cluster (otherwise
index names and mappings can conflict with existing data). All nodes of this
cluster need the following configuration setup:

- Enabled dynamic scripting: `script.disable_dynamic: false`
- Installed river plugin: `elasticsearch-river-sysinfo 1.5.1` 

## Index naming conventions

In this tutorial we will be storing each `info_type` content into **separate index**
and the data will be **always** stored under index type called `data`.

Synopsis:

<table>
  <tr>
    <th>index name</th><th>index type</th>
  </tr>
  <tr>
    <td><code>sysinfo_{info_type}_{custom_key}</code></td><td><code>data</code></td>
  </tr>
</table>

- All indices share common name prefix `sysinfo_` followed by `{info_type}` code and `{custom_key}` value
- All indices have `type` index type

## Mappings and Transformations

JSON data returned by Elasticsearch Admin APIs is not always convenient for direct indexing.
It is not an exception to see data values being used as a JSON object keys. Indexing such
data brings two downsides: a) frequent mapping updates, which can be costly and b) difficult
querying and aggregation of the data.

In case important data is used as an object key we try to move/copy it to object value
under arbitrary key. This usually also means that objects are converted into arrays
and mapped as nested type.

For example, if index names are used as keys:
 
````
  {
    "indices": {
      "index_01": { "number_of_shards": 5, "number_of_replicas": 3 },
      "index_02": { "number_of_shards": 4, "number_of_replicas": 2 }
    }
  }
````

we transform it to the following format:

````
  {
    "indices": [
      {
        "index_name": "index_01",
        "number_of_shards": 5,
        "number_of_replicas": 3
      },
      {
        "index_name": "index_02",
        "number_of_shards": 4,
        "number_of_replicas": 2
      }
    ]
  }
````

### `sysinfo_`

Common prefix is used by the top most level index template to ensure all indices have:

  - Common mapping for `data` index type:
    - Enabled `_timestamp` field and make it `stored`
    - Enabled `_index` field and make it `stored`
    - Disabled `include_in_all`
    - Disabled `_all` field
  - **Template mapping:** [sysinfo_.json](setup/templates/sysinfo_.json)

### `{info_type}`

Used by more detailed index templates to set individual `info_type`s settings. If the `_source`
field contains top level field `_all` then `transform` script is used to move it to `_all_copy`
field.

It seems that storing nested object types into top level field called `_all`
in Elasticsearch is problematic even if `_all` field is disabled or explicit mapping
is configured for it. To workaround this issue we "rename" `_all` field to `_all_copy`
using `transform` script.

The following `info_type`s are available:

#### `cluster_health`

  - Object `_source.indices` contains index names as keys. We transform it to nested type.
  - **Template mapping:** [sysinfo_cluster_health_.json](setup/templates/sysinfo_cluster_health_.json)

#### `cluster_state`

  - Object `_source.nodes` contains node names as keys. We transform it to nested type.
  - Object `_source.routing_table.indices` contains index names as keys. We transform it to nested type. (TODO: nested
    shard info needs to be transformed to nested type as well)
  - Object `_source.routing_nodes.unassigned` is mapped as nested type
  - Object `_source.routing_nodes.nodes` contains node names as keys. We transform it to nested type as follows:

Original format:

    {
      "routing_nodes": {
        "nodes": {
          "oXUWLJfJQ3ilAvJ4Unoa9g": [
            { <shard_01> },
            { <shard_02> }
          ]
        }
      }
    }
    
is transformed to:

    {
      "routing_nodes": {
        "nodes": [                            ## <- nested type
          {
            "node": "oXUWLJfJQ3ilAvJ4Unoa9g", ## <- artificial key/value
            "shards": [                       ## <- nested type
              { <shard_01> },
              { <shard_02> }
            ]
          }
        ]
      }
    }
    
- **Template mapping:** [sysinfo_cluster_state_.json](setup/templates/sysinfo_cluster_state_.json)

#### `cluster_stats`

  - Object inside `_source.nodes.plugins` are mapped as nested type.
  - **Template mapping:** [sysinfo_cluster_stats_.json](setup/templates/sysinfo_cluster_stats_.json)

#### `pending_cluster_tasks`

  - TODO
  - **Template mapping:** [sysinfo_pending_cluster_tasks_.json](setup/templates/sysinfo_pending_cluster_tasks_.json)

#### `cluster_nodes_info`

  - Object `_source.nodes` contains node names as keys. We transform it to nested type.
  - Object inside `_source.nodes.plugins` are mapped as nested type.
  - **Template mapping:** [sysinfo_cluster_nodes_info_.json](setup/templates/sysinfo_cluster_nodes_info_.json)

#### `cluster_nodes_stats`

  - Object `_source.nodes` contains node names as keys. We transform it to nested type.
  - TODO: `node.fs.data` ?
  - **Template mapping:** [sysinfo_cluster_nodes_stats_.json](setup/templates/sysinfo_cluster_nodes_stats_.json)
  
#### `indices_status`

Deprecated API, not used.

#### `indices_stats`

  - Object `_source.indices` contains index names as a keys. We convert it into nested type.
  - **Template mapping:** [sysinfo_indices_stats_.json](setup/templates/sysinfo_indices_stats_.json)

#### `indices_segments`

  - Not configured for now...

#### `indices_recovery`

  - TODO
  - **Template mapping:** [sysinfo_indices_recovery_.json](setup/templates/sysinfo_indices_recovery_.json)
  

### `{custom_key}`

Is important to enable index rotation. For instance `{custom_key}` can be defined
as `{index_create_timestamp}` or it can include also the cluster name
`{cluster_name}_{index_create_timestamp}` if we know we will be pulling data from more
clusters.

#### Index name examples

    sysinfo_cluster_health_2015-03-01
    sysinfo_cluster_health_cluster01_2015-03-01
    
    
## Index alias naming conventions

### Index alias for search

    sysinfo_{info_type}_search
    
This alias is shared by all indices having the same `sysinfo_{info_type}` prefix.
This enables simple processing (e.g. searching and aggregating) of data from all
relevant indices.

### Index alias for indexing

    sysinfo_{info_type}_index
    
There needs to be only a **single** index having this alias per `info_type`.
This is the index that is currently being used by sysinfo river.

#### Index aliases example

<table>
  <tr>
    <th>index name</th>
    <th>alias for search</th>
    <th>alias for indexing</th>
  </tr>
  <tr>
    <td><code>sysinfo_cluster_health_2015-03-03</code>
    </td><td><code>sysinfo_cluster_health_search</code></td>
    </td><td><code>sysinfo_cluster_health_index</code></td>
  </tr>
  <tr>
    <td><code>sysinfo_cluster_health_2015-03-02</code>
    </td><td><code>sysinfo_cluster_health_search</code></td>
    </td><td></td>
  </tr>
  <tr>
    <td><code>sysinfo_cluster_health_2015-03-01</code>
    </td><td><code>sysinfo_cluster_health_search</code></td>
    </td><td></td>
  </tr>
</table>

Similarly to `{custom_key}` both aliases for search and indexing must contain
cluster name if we pull data from more clusters.

## Index rotation

Index rotation is used to control amount of persisted data.

### Adding new indices

The core idea is to periodically create a new index (for every `info_type`) and update
index aliases in such a way that only the new index is used for indexing but all indices
are used for search.

This concept can be illustrated using the following flow:

````
  # create new index
  curl -XPUT 'localhost:9200/index_1/'
  
  # add aliases
  curl -XPOST 'localhost:9200/_aliases' -d '{
    "actions": [
      { "add": { "index": "index_1", "alias": "search_alias" }},
      { "add": { "index": "index_1", "alias": "index_alias" }}
    ]
  }'

  # index documents
  curl -XPUT 'localhost:9200/index_alias/type/1' -d '{"foo": "bar1"}'
  curl -XPUT 'localhost:9200/index_alias/type/2' -d '{"foo": "bar2"}'

  # create another new index
  curl -XPUT 'localhost:9200/index_2/'
 
  # update aliases
  curl -XPOST 'localhost:9200/_aliases' -d '{
    "actions": [
      { "remove": { "index": "index_1", "alias": "index_alias" }},
      { "add": { "index": "index_2", "alias": "index_alias" }},
      { "add": { "index": "index_2", "alias": "search_alias" }}
    ]
  }'
  
  # optimise previous index - reduce disk space and speedup search
  curl -XPOST 'localhost:9200/index_1/_optimize'

  # index more documents - these documents go to the new index
  curl -XPUT 'localhost:9200/index_alias/type/3' -d '{"foo": "bar3"}'
  curl -XPUT 'localhost:9200/index_alias/type/4' -d '{"foo": "bar4"}'
  
  # search goes to all relevant indices
  curl -XGET 'localhost:9200/index_search/_search'
````

### Remove old indices

There can be several strategies how to determine which indices to drop.

#### Keep only the most recent indices

Probably the most trivial strategy would be to keep around only certain number
of indices with the most recent data. For example if we know that we rotate indices
once a day (this means creating one new index per `info_type` a day) and we want
to keep two weeks of historical data then we can simply list all indices for given
`info_type`, order it by `{custom_key}` descending and drop all indices after 14th
member in this list.

For example if we collect `indices_stats` data then we can do query like this:

````
  curl 'localhost:9200/sysinfo_indices_stats*/_search?search_type=count' -d '{
    "aggs": {
      "indices": {
        "terms": {
          "field": "_index",
          "order": { "_term": "desc" }
  }}}}'
````

In case the index name timestamp format is not convenient for proper ordering we
can order indies by selecting `max(_timestamp)` value of contained documents:

````
  curl 'localhost:9200/sysinfo_indices_stats*/_search?search_type=count' -d '{
    "aggs": {
      "indices": {
        "terms": {
          "field": "_index",
          "order": { "max_timestamp": "desc" }
        },
        "aggs": {
          "max_timestamp": {
            "max": { "field": "_timestamp" }
  }}}}}'
````

#### Drop indices with old data

Another strategy is to do not bother about number of indices as long as we make sure
they do not contain "too old" data.

For example we want to list all index names (for `indices_stats`) that contain data
created in year 2013 or older then we can use query like this:

````
  curl 'localhost:9200/sysinfo_indices_stats*/_search?search_type=count' -d '{
    "query": {
      "filtered": {
        "filter": {
          "not": {
            "range": {
              "_timestamp": { "gte": "2013" }
     }}}}
    },
    "aggs": {
      "indices": {
        "terms": {
          "field": "_index"
  }}}}'
````
However, before deleting the index you should make sure it is not the only index for
given `info_type`. You probably do not want to delete the only index that the sysinfo
river can index to.