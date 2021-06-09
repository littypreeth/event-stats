# Event Stats Service

Stats generation service which you can use to record events and get the statistics on the events. 
The statistics retrieval is the fastest with a O(1) complexity.

* [`APIs`](#apis)
    * [`POST /event`](#post-event)
      * [Example Payload](#example-payload)
    * [`GET /stats`](#get-stats)
      * [Example Response](#example-response)
    
* [How to Use](#how-to-use)
    * [Building the service](#building-the-service)
    * [Starting Server](#starting-server)

## `APIs`

### `POST /event`

Post an event which has the following fields:
1. _timestamp_: An integer with the Unix timestamp in millisecond resolution when the
   event happened. The data need not be ordered by this timestamp
1. 𝑥: A real number with a fractional part of up to 10 digits, always in 0..1.
1. 𝑦: An integer in 1,073,741,823..2,147,483,647.

Any event older than the allowed age of 60 secs is ignored. 
Any event with future timestamp is ignored.
#### Example Payload

```csv
1607341341814,0.0442672968,1282509067
1607341339814,0.0473002568,1785397644
1607341331814,0.0899538547,1852154378
1607341271814,0.0586780608,111212767
```

#### _Failure_:
* [415](https://httpstatuses.com/415) For un-supported content type. 
  Supported content-type: application/csv, application/text, text/csv
* [400](https://httpstatuses.com/415) if csv parsing fails

#### _Success_:
* [202](https://httpstatuses.com/202) if the data was successfully processed.
Response body reports if any event was not as per the constraints above.
#### Example Response
```json
{
  "timestamp":"2021-06-05-15:14.07.325",
  "inputCount":13,
  "failureCount":3,
  "errors":[
    "Invalid y value 1 on line 1. Expected 1,073,741,823 <= x <= 2,147,483,647",
    "Event in future on line 12",
    "Event too old on line 13"
  ]
}
```

### `GET /stats`

Returns statistics about the data that was received so far. It returns
the data points that lie within the past 60 seconds separated by a comma (`,`):

1. Total
1. Sum 𝑥
1. Avg 𝑥
1. Sum 𝑦
1. Avg 𝑦

For 𝑥 a fractional part of up to 10 digits is returned. 
For Avg(𝑦) a fractional part of up to 3 digits is returned. 
If no data was recorded so far or in the past 60 secs then 
response will be with 0 values.

#### Example Response

```csv
7,1.1345444135,0.1620777734,11824011150,1689144450.000
```
## How to Use
### With Docker
#### Building the service
```
cd <git root>
docker build -t eventstats:latest . 
```
#### Starting Server
Starts the server at port 8080.
```
docker run -d --publish 8080:8080 eventstats
```
### Without Docker
#### Building the service
```
cd <git root>
gradle clean build test 
```
#### Starting Server
Starts the server at port 8080.
```
cd <git root>
java -jar build/libs/event-stats-0.0.1-SNAPSHOT.jar
```