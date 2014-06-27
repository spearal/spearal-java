Spearal Java
============

## What is Spearal?

Spearal is a compact binary format for exchanging arbitrary complex data between various endpoints such as Java EE, JavaScript / HTML, Android and iOS applications.

Spearal-Java is the common codebase used in all Spearal / Java specific extensions.

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

## Working with partial encoding:

Spearal lets you encode only some properties of an object. Let's say you have bean defined by this class:

````java
class Person implements Serializable {

    private String firstName;
    private String lastName;
    private List<String> phones;
    
    // getters / setters...
}
````

If you are retrieving a collection of Persons and just need their first and last names, it is useless to serialize their phones. Spearal lets you encode just the firstName and lastName properties:

````java
SpearalFactory factory = new SpearalFactory();
ByteArrayOutputStream baos = new ByteArrayOutputStream();
SpearalEncoder encoder = factory.newEncoder(baos);

// Only firstName and lastName:
encoder.getPropertyFilter().add(Person.class, "firstName", "lastName");

encoder.writeAny(obj);
````

When decoding the result, you will get a proxy for each Person, that will throw a `UndefinedPropertyException` if you try to access the phones collection:

````java
ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
SpearalDecoder decoder = factory.newEncoder(bais);
Person copy = decoder.readAny(Person.class);

System.out.println(copy.getFirstName());
System.out.println(copy.getLastName());

// This line throws a UndefinedPropertyException:
System.out.println(copy.getPhones());
````

## What is the Spearal Mime Type?

Data exchanged in the Spearal format should use the `application/spearal` mime type.
