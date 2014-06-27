Spearal Java
============

## What is Spearal?

Spearal is a compact binary format for exchanging arbitrary complex data between various endpoints such as Java EE, JavaScript / HTML, Android and iOS applications.

Spearal-Java is the common Java codebase used in all Spearal / Java specific implementations.

## How to get and build the project?

````sh
$ git clone https://github.com/spearal/spearal-java.git
$ cd spearal-java
$ ./gradlew build
````

The built library can then be found in the `build/libs/` directory.

## How to use the library?

First, you need to create a SpearalFactory:

````java
SpearalFactory factory = new SpearalFactory();
````

Encoding data is then a matter of creating a new encoder and call the writeAny method:

````java
ByteArrayOutputStream baos = new ByteArrayOutputStream();
SpearalEncoder encoder = factory.newEncoder(baos);
encoder.writeAny(obj);
````

Decoding is achieved the same way:

````java
ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
SpearalDecoder decoder = factory.newEncoder(bais);
Object copy = decoder.readAny();
````

## What is the Spearal Mime Type?

Data exchanged in the Spearal format should use the `application/spearal` mime type.
