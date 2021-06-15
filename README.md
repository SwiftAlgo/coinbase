# Coinbase
### Level2 OrderBook consuming Coinbase Level 2 data over Websocket

See [Coinbase Pro Websocket API](https://docs.pro.coinbase.com/#websocket-feed)

To build simply invoke gradle:

`./gradlew`

To run using Gradle, run task orderBook and pass the product as a parameter.  For example 'BTC-USD' in the following:

`./gradlew orderBook --args="BTC-USD"`


Alternatively after building, run using the Gradle classpath like this on Mac or Linux:

```
java -cp `./gradlew -q runtimeClasspath` coinbase.Level2OrderBookConsole BTC-USD
```

The top 10 price levels are displayed although 100 price levels are maintained to allow for quotes to be pulled.

Hit `Ctrl-C` to stop.
