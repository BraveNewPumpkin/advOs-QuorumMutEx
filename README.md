# LeaderElectionPractice
a practice implementation using Spring WebSocket of Peleg's algorithm for leader election and asynchronously building a Breadth First Search Tree of with the leader as the root node.

parameters are:

**this.hostName** - the hostname to use when finding this nodes information in the config file

**this.isLocal** - set this to true if you wish to run all of your nodes on localhost

**nodeConfigUri** - the URI of the config file. example: "nodeConfigUri=file:/home/BraveNewPumpkin/config.txt"


References:
David Peleg,
Time-optimal leader election in general networks,
Journal of Parallel and Distributed Computing,
Volume 8, Issue 1,
1990,
Pages 96-99,
ISSN 0743-7315,
https://doi.org/10.1016/0743-7315(90)90074-Y.
(http://www.sciencedirect.com/science/article/pii/074373159090074Y)
