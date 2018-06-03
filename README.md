# MAP Snapshot
a practice implementation using Spring WebSocket of the MAP protocol and using Chandy and Lamport’s protocol for recording a consistent global snapshot to check the state.

parameters are:

**this.hostName** - the hostname to use when finding this nodes information in the config file

**this.isLocal** - set this to true if you wish to run all of your nodes on localhost

**nodeConfigUri** - the URI of the config file. example: "nodeConfigUri=file:/home/BraveNewPumpkin/config.txt"


References:
Leslie Lamport, K. Mani Chandy: Distributed Snapshots: Determining Global States of a Distributed System. In: ACM Transactions on Computer Systems 3. Nr. 1, February 1985.
