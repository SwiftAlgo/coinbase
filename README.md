# coinbase
Level2 OrderBook consuming Coinbase Level 2 data over Websocket

To run using Gradle, run task orderBook and pass the product of interest as a parameter like BTC-USD in the following example:

./gradlew orderBook --args="BTC-USD"

The top 10 price levels are displayed although 100 price levels are maintained to allow for quotes to be pulled.

Hit Ctrl-C to stop.
