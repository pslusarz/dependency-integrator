Dependency Integrator
=========

### Continuous Delivery in the real world

When you make a change to code in the jar, you usually increment the version and generate a new jar. This jar is then consumed by some downstream dependency that needed the new feature to deliver some functionality in production. Since one of the main reason for existence of jars is to share functionality, what happens to other components that also depend on the changed jar? Do they get the new version? How do you ensure that the new version works for them? At the heart of Jez Humble's Continuous Delivery system is an algorithm for integrating changes into a system of dependent components.<sup>[1]</sup> It is vaguely described and, to date, no open source implementation has been published. This project explores what it takes to build such a system aimed at integrating your jar network.

Below you can see an example of a dependency tree that dependency-integrator can automatically keep up-to-date. Each vertex represents a jar and each red edge represents a dependency that is not up to date. Additionally, blue edges indicate a cyclic dependency.

![dependency hierarchy screenshot](/docs/dependencies-cropped.png "Jar dependency integration")

---------
[1] Chapter 13, Dependency Management. Humble, Jez. Continuous Delivery: Reliable Software Releases through Build, Test, and Deployment Automation. Addison-Wesley Signature Series. 2010