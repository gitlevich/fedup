# Design Problems
##### (questions encountered during development)

DP1: given both the service producing to a topic and the service consuming from it needs to understand the contents,
how can we keep domain objects in the right service's bounded context while having both services understand them?
Possibilities:
- use Shared Kernel for the communicating services
- if a service is clearly downstream from another, conform to it
- use a common wire protocol, like Avro, and translate to/from it at the topic/service boundaries