# Quorum Based Distributed Mutual Exclusion
a practice implementation using Spring WebSocket of Maekawa's quorum based mutual exclusion algorithm.

**USE BRANCH controllerProducerAndServiceConsumer**

parameters are:

**this.hostName** - the hostname to use when finding this nodes information in the config file

**this.isLocal** - set this to true if you wish to run all of your nodes on localhost

**nodeConfigUri** - the URI of the config file. example: "nodeConfigUri=file:/home/BraveNewPumpkin/config.txt"


References:
M. Maekawa, "A √N algorithm for mutual exclusion in decentralized systems”, ACM Transactions in Computer Systems, vol. 3., no. 2., pp. 145-159, 1985.
