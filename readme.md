# FedUp - a local delivery service

FedUp is a hot last mile delivery startup. The business model is to offer an amazing customer
experience for local delivery to people fed up with FedEx/UPS/USPS. 

They plan to fund it by collecting from their users a monthly membership and a reasonable 
per-delivery fee.

They think they can tap into the same driver market that Uber and Caviar uses. They plan to
generously pay their freelance drivers for precise real-time deliveries.

## How they are going to do it

To transport a package between a shipper and a receiver, FedUp uses or more drivers. The package 
is first scanned by the driver doing pickup. If delivery takes several drivers, each successive
driver scans the package again on hand-off. 

The receiver is notified just after the pickup to arrange the time and place of delivery. 
On delivery, the driver collects receiver's signature, which concludes the transaction.    

They decided to start Bay Area, offering same day, and sometimes same hour, delivery.

## Technical needs
This is what they need to get started:
- a mobile app for the prospective users (both shippers and receivers) to enable them to
    - order a pickup
    - track package progress
    - be notified when they need to take action
    - make their location available between pickup request and pickup / delivery notification and delivery
- a mobile app for the concierges to enable them to
    - be notified when their action is requested
    - scan the package on receipt
    - collect the receiver's signature upon delivery
    - make their location available while they are working to optimize routes

Additionally, the need a centralized way to:    
- track locations of drivers, shippers and receivers to optimize routes and coordinate package hand-offs
- track handling events for every package to provide real-time status updates
- support the mobile apps by exposing a set of APIs they need
