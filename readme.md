Dependency Integrator
=========

### Continuous Delivery in the real world

When you make a change to code in the jar, you usually increment the version and generate a new jar. This jar is then consumed by some downstream dependency that needed the new feature to deliver some functionality in production. Since one of the main reason for existence of jars is to share functionality, what happens to other components that also depend on the changed jar? Do they get the new version? How do you ensure that the new version works for them? At the heart of Jez Humble's Continuous Delivery system (Chapter 13, Dependency Management) is an algorithm for integrating changes into a system of dependent components. It is vaguely described and, to date, no open source implementation has been published. This project explores what it takes to build such a system aimed at integrating your jar network.

![dependency hierarchy screenshot](/docs/dependencies-levels.png "Jar dependency integration")