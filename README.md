This is a reproduction of the deadlocking problem which can occur using virtual threads
in Java 21. It was originally documented by Netflix engineering in this blog post:

https://netflixtechblog.com/java-21-virtual-threads-dude-wheres-my-lock-3052540e231d

However, this version uses logging (via logback) to trigger the deadlock scenario.

To see the proof of concept in action, execute the following:

    ./gradlew run
