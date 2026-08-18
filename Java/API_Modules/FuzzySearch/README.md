# Parallel Fuzzy Search

A Java API Module project demonstrating parallel fuzzy search against `UserProfile` SOSS objects.

This fuzzy search API module is designed as a general-purpose sample for searching person/profile data. It demonstrates the parallel operation `Reduce` which evaluates all entries in a cache namespace, combines the results, and returns a final result to the client.

The `Reduce` operation's evaluation method uses Jaro-Winkler similarity -- fuzzy matching across properties in a "UserProfile" object. A `UserProfile` object abstractly represents customer records, employee directories, membership databases, public records, or other datasets where identifying information may be incomplete or slightly inconsistent.

## UserProfile

Each record is represented by a `UserProfile`.

A profile contains common identity and contact information such as:

- ID
- First name
- Last name
- Aliases
- Email address
- Organization
- Tags
- Notes
- etc.

Example:

```json
{
  "id": "USER-0000123",
  "firstName": "Jonathan",
  "middleName": "Robert",
  "lastName": "Smith",
  "aliases": ["Bumble Bee"],
  "dateOfBirth": "1984-05-17",
  "streetAddress": "123 Main Street",
  "city": "Seattle",
  "state": "WA",
  "postalCode": "98101",
  "phoneNumber": "555-555-1234",
  "emailAddress": "jonathan.smith@example.com",
  "occupation": "Software Engineer",
  "organization": "Example Corp",
  "tags": [],
  "notes": ""
}
```

Some fields may be empty. This allows the sample dataset to represent incomplete records commonly found in real-world systems.

## Reduce Operation Usage

Search query criteria are provided as JSON encoded as a UTF-8 `byte[]` to the `Reduce` operation as a parameter object.

For example:

```json
{
  "firstName": "Jonathon",
  "lastName": "Smyth"
}
```

The query is evaluated against a `UserProfile` using:

```java
boolean match = UserProfileSearcher.fuzzySearch(profile, searchOptions);
```

### Jaro-Winkler Similarity

Text fields are compared using `JaroWinklerSimilarity`. Jaro-Winkler produces a similarity score between `0.0` and `1.0`.

A score of:

```text
1.0
```

represents an exact match, while lower values represent increasingly different strings.

This project currently considers two strings a fuzzy match when their similarity is:

```text
>= 0.85
```

This allows common spelling differences, phonetic spelling, and typographical errors to match.

For example:

```text
Smith    - Smyth
Jonathan - Jonathon
Seattle  - Seatle
```

> [!NOTE]
> You can change the minimum similarity from `0.85` by editing the `_minimumSimilarity` field in `UserProfileSearcher`.

### Multiple Search Properties

A query may contain a single property:

```json
{
  "lastName": "Smyth"
}
```

...or multiple properties:

```json
{
  "firstName": "Jonathon",
  "lastName": "Smyth",
  "city": "Seatle"
}
```

When multiple properties are supplied, each property must exceed the .85 fuzzy match for the `UserProfile` to be considered a match.

### Exact vs. Fuzzy Matching

Not every property is appropriate for fuzzy matching.

Fields such as names, addresses, cities, occupations, organizations, aliases, and notes are compared using Jaro-Winkler similarity.

Fields where approximate matching could produce misleading results use normalized exact matching instead, including:

- ID
- Postal code
- Phone number

For list properties such as `aliases` or `tags`, the search value is compared against each entry. The field is considered a match if any entry satisfies the similarity threshold.

## Updating Profiles

`UserProfile` objects can also be updated using partial JSON documents.

For example:

```json
{
  "city": "Seattle"
}
```

or:

```json
{
  "firstName": "Jonathan",
  "city": "Seattle",
  "state": "WA"
}
```

Only properties included in the update are modified. Properties that are not supplied remain unchanged.

Updates are applied using:

```java
UserProfileUpdater.updateUserProfile(
        profile,
        incomingUserProfileChanges);
```

## Synthetic Data

The project includes support for generating large synthetic `UserProfile` datasets for testing.

For example:

```java
generateData(
        200_000,
        new File("user-profiles.jsonl"));
```

Generated profiles are written as JSON Lines, with one `UserProfile` per line.

```text
{"id":"USER-0000001", ...}
{"id":"USER-0000002", ...}
{"id":"USER-0000003", ...}
```

The generator intentionally varies which fields are populated so that search experiments can be performed against incomplete and heterogeneous profile data.

## Building

This project requires Maven.

From the project root:

```bash
mvn package
```

The compiled artifact will be written to the Maven `target` directory.

## Generating Data

You can generate larger data sets using the sub-project `DataGenerator`. The `DataGenerator` project creates Java `UserProfile` objects, serializes them UTF-8 JSON and writes them to local files.

## Hydrating the ScaleOut StateServer Cache

The `UserProfileHydrater` is used to load `UserProfile` objects from a file into a ScaleOut cache. The API module's unit test demonstrates hydrating the cache from the commited JSON file.
