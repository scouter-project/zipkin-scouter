# zipkin-scouter
Zipkin Scouter UDP storage and other zipkin-scouter integrations.
* Supported scouter collector version : `v2.5+`

## Server integration
In order to integrate with zipkin-server, you need to use properties
launcher to load your collector (or sender) alongside the zipkin-server
process.

To integrate a module with a Zipkin server, you need to:
* add a module jar to the `loader.path`
* enable the profile associated with that module
* launch Zipkin with `PropertiesLauncher`

Each module will also have different minimum variables that need to be set.


## Example integrating the Scouter Collector by Zipkin-Scouter-Storage

Here's an example of integrating the scouter Collector.

### Step 1: Download zipkin-server jar
Download the [latest released server](https://search.maven.org/remote_content?g=io.zipkin.java&a=zipkin-server&v=LATEST&c=exec) as zipkin.jar:

```
cd /tmp
wget -O zipkin.jar 'https://search.maven.org/remote_content?g=io.zipkin.java&a=zipkin-server&v=LATEST&c=exec'
```

### Step 2: Download the latest zipkin-storage-scouter jar
Download the [latest released Scouter module](https://search.maven.org/remote_content?g=io.github.scouter-project&a=zipkin-autoconfigure-storage-scouter-udp&v=LATEST&c=module) as zipkin-storage-scouter.jar:

```
cd /tmp
wget -O zipkin-storage-scouter.jar 'https://search.maven.org/remote_content?g=io.github.scouter-project&a=zipkin-autoconfigure-storage-scouter-udp&v=LATEST&c=module'
```

### Step 3: Run the server with the "scouter" profile active
When you enable the "scouter" profile, you can configure scouter with
short environment variables similar to other [Zipkin integrations](https://github.com/openzipkin/zipkin/blob/master/zipkin-server/README.md#elasticsearch-storage).

``` bash
cd /tmp
SCOUTER_COLLECTOR_ADDR=127.0.0.1 \
SCOUTER_COLLECTOR_PORT=6100 \
STORAGE_TYPE=scouter \
java -Dloader.path='zipkin-storage-scouter.jar,zipkin-storage-scouter.jar!lib' -Dspring.profiles.active=scouter -cp zipkin.jar org.springframework.boot.loader.PropertiesLauncher
```
* **NOTE:** Make sure the parameters are defined in the same line or use environment variables **

* Configures
  * `STORAGE_TYPE=scouter` : **required**. All others are optional.
  * `SCOUTER_COLLECTOR_ADDR` (default: 127.0.0.1) : Scouter collector IP 
  * `SCOUTER_COLLECTOR_PORT` (default: 6100) : Scounter collector Port
  * `SCOUTER_UDP_PACKET_MAX_BYTES` (default: 60000) : should be smaller than OS UDP diagram size.  
  * `SCOUTER_TAG_MAP_TEXT1` : tag mapping to scouter xlog's predefined column `text1` (default: `spring.instance_id`) (comma separated multi-tags supported.)
  * `SCOUTER_TAG_MAP_TEXT2` : tag mapping to scouter xlog's predefined column `text2` (comma separated multi-tags supported.)
  * `SCOUTER_TAG_MAP_TEXT3` : tag mapping to scouter xlog's predefined column `text3` (comma separated multi-tags supported.)
  * `SCOUTER_TAG_MAP_TEXT4` : tag mapping to scouter xlog's predefined column `text4` (comma separated multi-tags supported.)
  * `SCOUTER_TAG_MAP_TEXT5` : tag mapping to scouter xlog's predefined column `text5` (comma separated multi-tags supported.)
  * `SCOUTER_TAG_MAP_LOGIN` : tag mapping to scouter xlog's predefined dictionary encoded column `login` (just single first matching tag of comma separated tags is used for the column.)
  * `SCOUTER_TAG_MAP_DESC` : tag mapping to scouter xlog's predefined dictionary encoded column `desc` (just single first matching tag of comma separated tags is used for the column.)
  * `SCOUTER_SERVICE_MAPS_OJB_TYPE` : map a zipkin's local endpoint service name to a scouter objType (ex: system1:OrderSystem,system2:CustomerSystem ...). It use the same name of service name with preceding $z.
  * `SCOUTER_DEBUG` (default: false) : Debug option  

### Limitation
This currently only supports sending to a Scouter collector, not reading back spans from the service.  
Spans can be shown in the Scouter's XLog view.

### Scouter Links
 - [Scouter GitHub](https://github.com/scouter-project/scouter)  

### Scouter Paper Links
 - [Scouter Paper Homepage](https://github.com/scouter-project/scouter)
 - **Scouter Paper showcase** : [scouter paper overview (youtube)](https://www.youtube.com/watch?v=NjJ0dGhdIbU)
 
