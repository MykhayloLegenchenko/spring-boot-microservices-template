# Spring Boot MicroServices Template
A project to collect boilerplate code and proof of concept testing.

### How to Run
Prerequisites: Java 24 and Docker.
```bash
./docker/run.sh
```

### Native executables generation
Prerequisites: GraalVM 24.
```bash
./gradlew nativeCompile --no-configuration-cache
```

### Generate IntelliJ IDEA project settings
```bash
./gradlew generateIdeaSettings
```

### Check for outdated dependencies
```bash
./gradlew checkOutdatedDependencies
```
