### Quick Reference

This document serves as a quick start guide to working with the Morpheus library. It provides various code examples
of how to affect commonly used operations on Morpheus `Arrays` and `DataFrames`. For the most part, the examples
involve the `cars93.csv` dataset which can be accessed [here](http://zavtech.com/data/samples/cars93.csv), and for which
the first 10 rows are displayed below.

<pre class="frame">
 Index  |  Manufacturer  |    Model     |   Type    |  Min.Price  |   Price   |  Max.Price  |  MPG.city  |  MPG.highway  |       AirBags        |  DriveTrain  |  Cylinders  |  EngineSize  |  Horsepower  |  RPM   |  Rev.per.mile  |  Man.trans.avail  |  Fuel.tank.capacity  |  Passengers  |  Length  |  Wheelbase  |  Width  |  Turn.circle  |  Rear.seat.room  |  Luggage.room  |  Weight  |  Origin   |        Make        |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     0  |         Acura  |     Integra  |    Small  |    12.9000  |  15.9000  |    18.8000  |        25  |           31  |                None  |       Front  |          4  |      1.8000  |         140  |  6300  |          2890  |              Yes  |             13.2000  |           5  |     177  |        102  |     68  |           37  |            26.5  |            11  |    2705  |  non-USA  |     Acura Integra  |
     1  |         Acura  |      Legend  |  Midsize  |    29.2000  |  33.9000  |    38.7000  |        18  |           25  |  Driver & Passenger  |       Front  |          6  |      3.2000  |         200  |  5500  |          2335  |              Yes  |             18.0000  |           5  |     195  |        115  |     71  |           38  |              30  |            15  |    3560  |  non-USA  |      Acura Legend  |
     2  |          Audi  |          90  |  Compact  |    25.9000  |  29.1000  |    32.3000  |        20  |           26  |         Driver only  |       Front  |          6  |      2.8000  |         172  |  5500  |          2280  |              Yes  |             16.9000  |           5  |     180  |        102  |     67  |           37  |              28  |            14  |    3375  |  non-USA  |           Audi 90  |
     3  |          Audi  |         100  |  Midsize  |    30.8000  |  37.7000  |    44.6000  |        19  |           26  |  Driver & Passenger  |       Front  |          6  |      2.8000  |         172  |  5500  |          2535  |              Yes  |             21.1000  |           6  |     193  |        106  |     70  |           37  |              31  |            17  |    3405  |  non-USA  |          Audi 100  |
     4  |           BMW  |        535i  |  Midsize  |    23.7000  |  30.0000  |    36.2000  |        22  |           30  |         Driver only  |        Rear  |          4  |      3.5000  |         208  |  5700  |          2545  |              Yes  |             21.1000  |           4  |     186  |        109  |     69  |           39  |              27  |            13  |    3640  |  non-USA  |          BMW 535i  |
     5  |         Buick  |     Century  |  Midsize  |    14.2000  |  15.7000  |    17.3000  |        22  |           31  |         Driver only  |       Front  |          4  |      2.2000  |         110  |  5200  |          2565  |               No  |             16.4000  |           6  |     189  |        105  |     69  |           41  |              28  |            16  |    2880  |      USA  |     Buick Century  |
     6  |         Buick  |     LeSabre  |    Large  |    19.9000  |  20.8000  |    21.7000  |        19  |           28  |         Driver only  |       Front  |          6  |      3.8000  |         170  |  4800  |          1570  |               No  |             18.0000  |           6  |     200  |        111  |     74  |           42  |            30.5  |            17  |    3470  |      USA  |     Buick LeSabre  |
     7  |         Buick  |  Roadmaster  |    Large  |    22.6000  |  23.7000  |    24.9000  |        16  |           25  |         Driver only  |        Rear  |          6  |      5.7000  |         180  |  4000  |          1320  |               No  |             23.0000  |           6  |     216  |        116  |     78  |           45  |            30.5  |            21  |    4105  |      USA  |  Buick Roadmaster  |
     8  |         Buick  |     Riviera  |  Midsize  |    26.3000  |  26.3000  |    26.3000  |        19  |           27  |         Driver only  |       Front  |          6  |      3.8000  |         170  |  4800  |          1690  |               No  |             18.8000  |           5  |     198  |        108  |     73  |           41  |            26.5  |            14  |    3495  |      USA  |     Buick Riviera  |
     9  |      Cadillac  |     DeVille  |    Large  |    33.0000  |  34.7000  |    36.3000  |        16  |           25  |         Driver only  |       Front  |          8  |      4.9000  |         200  |  4100  |          1510  |               No  |             18.0000  |           6  |     206  |        114  |     73  |           43  |              35  |            18  |    3620  |      USA  |  Cadillac DeVille  |
</pre>

The Morpheus library is available on **Maven Central**, so can easily be included in your build tool of choice.

```xml
<dependency>
    <groupId>com.zavtech</groupId>
    <artifactId>morpheus-core</artifactId>
    <version>${VERSION}</version>
</dependency>
```

If you want to leverage the Morpheus visualization library, use the following dependency which will pull in `morpheus-core`.

```xml
<dependency>
    <groupId>com.zavtech</groupId>
    <artifactId>morpheus-viz</artifactId>
    <version>${VERSION}</version>
</dependency>
```

### Create

#### From CSV

Morpheus ships with a versatile reader to initialize a `DataFrame` from a CSV file as shown by the following examples.

<?prettify?>
```java
//Load from a URL and initialise the row axis based on an auto incrementing integer.
DataFrame<Integer,String> frame = DataFrame.read().csv(options -> {
    options.setResource("http://zavtech.com/data/samples/cars93.csv");
});

//Exclude the first column from the frame, and instead use it to initialize the row axis.
DataFrame<Integer,String> frame = DataFrame.read().<Integer>csv(options -> {
    options.setResource("http://zavtech.com/data/samples/cars93.csv");
    options.setRowKeyParser(Integer.class, tokens -> Integer.parseInt(tokens[0]));
    options.setExcludeColumnIndexes(0);
});

//Only select the rows that represent a specific make of car, namely **Buick** in this example.
DataFrame<Integer,String> frame = DataFrame.read().csv(options -> {
    options.setResource("http://zavtech.com/data/samples/cars93.csv");
    options.setRowKeyParser(Integer.class, tokens -> Integer.parseInt(tokens[0]));
    options.setExcludeColumnIndexes(0);
    options.setRowPredicate(tokens -> tokens[1].equalsIgnoreCase("Buick"));
});

//Select a subset of columns based on their names (the CSV file must include a header for this to work).
DataFrame<Integer,String> frame = DataFrame.read().csv(options -> {
    options.setResource("http://zavtech.com/data/samples/cars93.csv");
    options.setRowKeyParser(Integer.class, tokens -> Integer.parseInt(tokens[0]));
    options.setExcludeColumnIndexes(0);
    options.setRowPredicate(tokens -> tokens[1].equalsIgnoreCase("Buick"));
    options.setIncludeColumns("Manufacturer", "Model", "Min.Price", "Price", "Max.Price");
});

//Rename columns by providing a column name mapping function
DataFrame<Integer,String> frame = DataFrame.read().csv(options -> {
    options.setResource("http://zavtech.com/data/samples/cars93.csv");
    options.setRowKeyParser(Integer.class, tokens -> Integer.parseInt(tokens[0]));
    options.setExcludeColumnIndexes(0);
    options.setRowPredicate(tokens -> tokens[1].equalsIgnoreCase("Buick"));
    options.setIncludeColumns("Manufacturer", "Model", "Min.Price", "Price", "Max.Price");
    options.setColumnNameMapping((name, ordinal) -> {
        switch (name) {
            case "Turn.circle":     return "TurnCircle";
            case "Rear.seat.room":  return "RearRoom";
            case "Luggage.room":    return "LuggageRoom";
            default:                return name;
        }
    });
});
```

#### From Database

Morpheus ships with a versatile reader to initialize a `DataFrame` from a SQL Database as shown by the following examples.

A JDBC connection can be injected via a `javax.sql.DataSource` or by presenting the JDBC URL and credentials directly.

<?prettify?>
```java
//Initialise a driver for examples
Class.forName("org.sqlite.JDBC");

//Load from a SQL Lite Database
DataFrame<Integer,String> frame = DataFrame.read().db(options -> {
    options.withConnection("jdbc:sqlite:/Users/witdxav/tmp/testdb", "", "");
    options.withSql("select * from Cars_93");
});

//Use the ID column from the table to initialize the row keys
DataFrame<Integer,String> frame = DataFrame.read().db(options -> {
    options.withConnection("jdbc:sqlite:/Users/witdxav/tmp/testdb", "", "");
    options.withSql("select * from Cars_93");
    options.withExcludeColumns("ID");
    options.withRowKeyFunction(rs -> Try.call(() -> rs.getInt("ID")));
});

//Exclude specific columns from the construction of the DataFrame
DataFrame<Integer,String> frame = DataFrame.read().db(options -> {
    options.withConnection("jdbc:sqlite:/Users/witdxav/tmp/testdb", "", "");
    options.withSql("select * from Cars_93");
    options.withExcludeColumns("ID");
    options.withRowKeyFunction(rs -> Try.call(() -> rs.getInt("ID")));
    options.withExcludeColumns("AirBags", "DriveTrain");
});

//Use a parameterize SQL expression and specify the SQL parameters
DataFrame<Integer,String> frame = DataFrame.read().db(options -> {
    options.withConnection("jdbc:sqlite:/Users/witdxav/tmp/testdb", "", "");
    options.withSql("select * from Cars_93 where `Min.Price` > ? and Type = ?");
    options.withExcludeColumns("ID");
    options.withRowKeyFunction(rs -> Try.call(() -> rs.getInt("ID")));
    options.withExcludeColumns("AirBags", "DriveTrain");
    options.withParameters(10, "Midsize");
});

//Use a custom extractor for a column to transform the value
DataFrame<Integer,String> frame = DataFrame.read().db(options -> {
    options.withConnection("jdbc:sqlite:/Users/witdxav/tmp/testdb", "", "");
    options.withSql("select * from Cars_93 where `Min.Price` > ? and Type = ?");
    options.withExcludeColumns("ID");
    options.withRowKeyFunction(rs -> Try.call(() -> rs.getInt("ID")));
    options.withExcludeColumns("AirBags", "DriveTrain");
    options.withParameters(10, "Midsize");
    options.withExtractor("Price", SQLExtractor.with(Double.class, (rs, colIndex) -> {
        return rs.getDouble(colIndex) * 1000;
    }));
});
```


#### Programmatically

<?prettify?>
```java
Random random = new Random();
Range<Integer> rowKeys = Range.of(0, 10);
Array<String> columns = Array.of("Column-0", "Column-2", "Column-3", "Column-4");

//Create a DataFrame of boolean values
DataFrame<Integer,String> frame1 = DataFrame.ofBooleans(rowKeys, columns, v -> random.nextBoolean());
//Create a DataFrame of int values
DataFrame<Integer,String> frame2 = DataFrame.ofInts(rowKeys, columns, v -> random.nextInt());
//Create a DataFrame of long values
DataFrame<Integer,String> frame3 = DataFrame.ofLongs(rowKeys, columns, v -> random.nextLong());
//Create a DataFrame of double values
DataFrame<Integer,String> frame4 = DataFrame.ofDoubles(rowKeys, columns, v -> random.nextDouble());
//Create a DataFrame of double values
DataFrame<Integer,String> frame5 = DataFrame.ofObjects(rowKeys, columns, v -> random.nextDouble());

//Create a single Row DataFrame of boolean values
DataFrame<String,String> frame6 = DataFrame.ofBooleans("Row-0", columns);
//Create a single Row DataFrame of int values
DataFrame<String,String> frame7 = DataFrame.ofInts("Row-0", columns);
//Create a single Row DataFrame of long values
DataFrame<String,String> frame8 = DataFrame.ofLongs("Row-0", columns);
//Create a single Row DataFrame of double values
DataFrame<String,String> frame9 = DataFrame.ofDoubles("Row-0", columns);
//Create a single Row DataFrame of object values
DataFrame<String,String> frame10 = DataFrame.ofObjects("Row-0", columns);

//Create a single Column DataFrame of boolean values
DataFrame<Integer,String> frame11 = DataFrame.ofBooleans(rowKeys, "Column-5");
//Create a single Column DataFrame of int values
DataFrame<Integer,String> frame12 = DataFrame.ofInts(rowKeys, "Column-5");
//Create a single Column DataFrame of long values
DataFrame<Integer,String> frame13 = DataFrame.ofLongs(rowKeys, "Column-5");
//Create a single Column DataFrame of double values
DataFrame<Integer,String> frame14 = DataFrame.ofDoubles(rowKeys, "Column-5");
//Create a single Column DataFrame of object values
DataFrame<Integer,String> frame15 = DataFrame.ofObjects(rowKeys, "Column-5");
```

The above examples are convenience methods to create `DataFrames` containing homogeneous data types, but it is
often necessary to create frames with mixed column types. The example below illustrates a strategy for doing this,
and demonstrates how column data can either be initialized via a lambda or by proving the values directly.

<?prettify?>
```java
Random random = new Random();
Range<Integer> rowKeys = Range.of(0, 10);
DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
    columns.add("Column-1", Boolean.class);
    columns.add("Column-2", Integer.class, v -> random.nextInt());
    columns.add("Column-3", Double.class, v -> random.nextDouble());
    columns.add("Column-4", Array.of("X", "Y", "Z"));
});
```

### Random Access

Random access to `DataFrame` rows, columns or values is achieved through a versatile API as demonstrated in this section. The
examples below continue to leverage the `cars93.csv` dataset, however we **re-index** the row keys to be String values rather
than integers (to make it easier to distinguish between ordinal and key access in the examples).

<?prettify?>
```java
DataFrame<String,String> frame = DataFrame.read().<Integer>csv(options -> {
    options.setResource("http://zavtech.com/data/samples/cars93.csv");
    options.setRowKeyParser(Integer.class, tokens -> Integer.parseInt(tokens[0]));
    options.setExcludeColumnIndexes(0);
}).rows().mapKeys(row -> {
    return "Row-" + row.key();
});
```

<pre class="frame">
 Index   |  Manufacturer  |    Model     |   Type    |  Min.Price  |   Price   |  Max.Price  |  MPG.city  |  MPG.highway  |       AirBags        |  DriveTrain  |  Cylinders  |  EngineSize  |  Horsepower  |  RPM   |  Rev.per.mile  |  Man.trans.avail  |  Fuel.tank.capacity  |  Passengers  |  Length  |  Wheelbase  |  Width  |  Turn.circle  |  Rear.seat.room  |  Luggage.room  |  Weight  |  Origin   |        Make        |
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Row-1  |         Acura  |     Integra  |    Small  |    12.9000  |  15.9000  |    18.8000  |        25  |           31  |                None  |       Front  |          4  |      1.8000  |         140  |  6300  |          2890  |              Yes  |             13.2000  |           5  |     177  |        102  |     68  |           37  |            26.5  |            11  |    2705  |  non-USA  |     Acura Integra  |
  Row-2  |         Acura  |      Legend  |  Midsize  |    29.2000  |  33.9000  |    38.7000  |        18  |           25  |  Driver & Passenger  |       Front  |          6  |      3.2000  |         200  |  5500  |          2335  |              Yes  |             18.0000  |           5  |     195  |        115  |     71  |           38  |              30  |            15  |    3560  |  non-USA  |      Acura Legend  |
  Row-3  |          Audi  |          90  |  Compact  |    25.9000  |  29.1000  |    32.3000  |        20  |           26  |         Driver only  |       Front  |          6  |      2.8000  |         172  |  5500  |          2280  |              Yes  |             16.9000  |           5  |     180  |        102  |     67  |           37  |              28  |            14  |    3375  |  non-USA  |           Audi 90  |
  Row-4  |          Audi  |         100  |  Midsize  |    30.8000  |  37.7000  |    44.6000  |        19  |           26  |  Driver & Passenger  |       Front  |          6  |      2.8000  |         172  |  5500  |          2535  |              Yes  |             21.1000  |           6  |     193  |        106  |     70  |           37  |              31  |            17  |    3405  |  non-USA  |          Audi 100  |
  Row-5  |           BMW  |        535i  |  Midsize  |    23.7000  |  30.0000  |    36.2000  |        22  |           30  |         Driver only  |        Rear  |          4  |      3.5000  |         208  |  5700  |          2545  |              Yes  |             21.1000  |           4  |     186  |        109  |     69  |           39  |              27  |            13  |    3640  |  non-USA  |          BMW 535i  |
  Row-6  |         Buick  |     Century  |  Midsize  |    14.2000  |  15.7000  |    17.3000  |        22  |           31  |         Driver only  |       Front  |          4  |      2.2000  |         110  |  5200  |          2565  |               No  |             16.4000  |           6  |     189  |        105  |     69  |           41  |              28  |            16  |    2880  |      USA  |     Buick Century  |
  Row-7  |         Buick  |     LeSabre  |    Large  |    19.9000  |  20.8000  |    21.7000  |        19  |           28  |         Driver only  |       Front  |          6  |      3.8000  |         170  |  4800  |          1570  |               No  |             18.0000  |           6  |     200  |        111  |     74  |           42  |            30.5  |            17  |    3470  |      USA  |     Buick LeSabre  |
  Row-8  |         Buick  |  Roadmaster  |    Large  |    22.6000  |  23.7000  |    24.9000  |        16  |           25  |         Driver only  |        Rear  |          6  |      5.7000  |         180  |  4000  |          1320  |               No  |             23.0000  |           6  |     216  |        116  |     78  |           45  |            30.5  |            21  |    4105  |      USA  |  Buick Roadmaster  |
  Row-9  |         Buick  |     Riviera  |  Midsize  |    26.3000  |  26.3000  |    26.3000  |        19  |           27  |         Driver only  |       Front  |          6  |      3.8000  |         170  |  4800  |          1690  |               No  |             18.8000  |           5  |     198  |        108  |     73  |           41  |            26.5  |            14  |    3495  |      USA  |     Buick Riviera  |
 Row-10  |      Cadillac  |     DeVille  |    Large  |    33.0000  |  34.7000  |    36.3000  |        16  |           25  |         Driver only  |       Front  |          8  |      4.9000  |         200  |  4100  |          1510  |               No  |             18.0000  |           6  |     206  |        114  |     73  |           43  |              35  |            18  |    3620  |      USA  |  Cadillac DeVille  |
</pre>

#### Row Random Access

Random access to `DataFrame` rows can be achieved by either row ordinal or key using `rowAt()` or `row()` respectively.

<?prettify?>
```java
//Access the 3rd row by ordinal using rowAt()
frame.rowAt(2).toDataFrame().out().print();
//Access the 3rd row by key using row()
frame.row("Row-3").toDataFrame().out().print();
```

#### Column Random Access

Random access to `DataFrame` columns can be achieved by either column ordinal or key using `colAt()` or `col()` respectively.

<?prettify?>
```java
//Access the 5th column by ordinal using colAt()
frame.colAt(4).toDataFrame().out().print();
//Access the 5th column by key using col()
frame.col("Price").toDataFrame().out().print();
```

#### Value Random Access

Random access to `DataFrame` values can be achieved by ordinals, keys or a combination of the two. Type specific getters and
setters exist to read & write data as shown in the folowing examples.

<?prettify?>
```java
//Access by row / column ordinals using getXXXAt()
Assert.assertEquals(frame.ra().getDoubleAt(2, 4), 29.1d);
//Access by row / column keys using getXXX()
Assert.assertEquals(frame.ra().getDouble("Row-3", "Price"), 29.1d);
//Access by row ordinal and column key using getXXXAt()
Assert.assertEquals(frame.rows().getDoubleAt(2, "Price"), 29.1d);
//Access by row key and column ordinal using getXXX()
Assert.assertEquals(frame.rows().getDouble("Row-3", 4), 29.1d);
//Access by row ordinal and column key using getXXX()
Assert.assertEquals(frame.cols().getDouble("Price", 2), 29.1d);
//Access by row key and column ordinal using getXXXAt()
Assert.assertEquals(frame.cols().getDoubleAt(4, "Row-3"), 29.1d);
```

### Change Shape

Changing the shape of a `DataFrame` is a common requirement, such as to add derived columns or add rows to capture new data. Adding rows
and columns can be done **in-place**, which is to say that the underlying `DataFrame` is expanded to store the new rows and/or columns,
much as you would expect from a **mutable collection**. Removing rows and/or columns is not done in place, and in practice is achieved
by **slicing and them copying** the resulting frame.

The examples below continue to leverage the `cars93.csv` dataset, however we **re-index** the row keys to be String values rather than
integers (to make it easier to distinguish between ordinal and key access in the examples).

<?prettify?>
```java
DataFrame<String,String> frame = DataFrame.read().<Integer>csv(options -> {
    options.setResource("http://zavtech.com/data/samples/cars93.csv");
    options.setRowKeyParser(Integer.class, tokens -> Integer.parseInt(tokens[0]));
    options.setExcludeColumnIndexes(0);
}).rows().mapKeys(row -> {
    return "Row-" + row.key();
});
```

<pre class="frame">
 Index   |  Manufacturer  |    Model     |   Type    |  Min.Price  |   Price   |  Max.Price  |  MPG.city  |  MPG.highway  |       AirBags        |  DriveTrain  |  Cylinders  |  EngineSize  |  Horsepower  |  RPM   |  Rev.per.mile  |  Man.trans.avail  |  Fuel.tank.capacity  |  Passengers  |  Length  |  Wheelbase  |  Width  |  Turn.circle  |  Rear.seat.room  |  Luggage.room  |  Weight  |  Origin   |        Make        |
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Row-1  |         Acura  |     Integra  |    Small  |    12.9000  |  15.9000  |    18.8000  |        25  |           31  |                None  |       Front  |          4  |      1.8000  |         140  |  6300  |          2890  |              Yes  |             13.2000  |           5  |     177  |        102  |     68  |           37  |            26.5  |            11  |    2705  |  non-USA  |     Acura Integra  |
  Row-2  |         Acura  |      Legend  |  Midsize  |    29.2000  |  33.9000  |    38.7000  |        18  |           25  |  Driver & Passenger  |       Front  |          6  |      3.2000  |         200  |  5500  |          2335  |              Yes  |             18.0000  |           5  |     195  |        115  |     71  |           38  |              30  |            15  |    3560  |  non-USA  |      Acura Legend  |
  Row-3  |          Audi  |          90  |  Compact  |    25.9000  |  29.1000  |    32.3000  |        20  |           26  |         Driver only  |       Front  |          6  |      2.8000  |         172  |  5500  |          2280  |              Yes  |             16.9000  |           5  |     180  |        102  |     67  |           37  |              28  |            14  |    3375  |  non-USA  |           Audi 90  |
  Row-4  |          Audi  |         100  |  Midsize  |    30.8000  |  37.7000  |    44.6000  |        19  |           26  |  Driver & Passenger  |       Front  |          6  |      2.8000  |         172  |  5500  |          2535  |              Yes  |             21.1000  |           6  |     193  |        106  |     70  |           37  |              31  |            17  |    3405  |  non-USA  |          Audi 100  |
  Row-5  |           BMW  |        535i  |  Midsize  |    23.7000  |  30.0000  |    36.2000  |        22  |           30  |         Driver only  |        Rear  |          4  |      3.5000  |         208  |  5700  |          2545  |              Yes  |             21.1000  |           4  |     186  |        109  |     69  |           39  |              27  |            13  |    3640  |  non-USA  |          BMW 535i  |
  Row-6  |         Buick  |     Century  |  Midsize  |    14.2000  |  15.7000  |    17.3000  |        22  |           31  |         Driver only  |       Front  |          4  |      2.2000  |         110  |  5200  |          2565  |               No  |             16.4000  |           6  |     189  |        105  |     69  |           41  |              28  |            16  |    2880  |      USA  |     Buick Century  |
  Row-7  |         Buick  |     LeSabre  |    Large  |    19.9000  |  20.8000  |    21.7000  |        19  |           28  |         Driver only  |       Front  |          6  |      3.8000  |         170  |  4800  |          1570  |               No  |             18.0000  |           6  |     200  |        111  |     74  |           42  |            30.5  |            17  |    3470  |      USA  |     Buick LeSabre  |
  Row-8  |         Buick  |  Roadmaster  |    Large  |    22.6000  |  23.7000  |    24.9000  |        16  |           25  |         Driver only  |        Rear  |          6  |      5.7000  |         180  |  4000  |          1320  |               No  |             23.0000  |           6  |     216  |        116  |     78  |           45  |            30.5  |            21  |    4105  |      USA  |  Buick Roadmaster  |
  Row-9  |         Buick  |     Riviera  |  Midsize  |    26.3000  |  26.3000  |    26.3000  |        19  |           27  |         Driver only  |       Front  |          6  |      3.8000  |         170  |  4800  |          1690  |               No  |             18.8000  |           5  |     198  |        108  |     73  |           41  |            26.5  |            14  |    3495  |      USA  |     Buick Riviera  |
 Row-10  |      Cadillac  |     DeVille  |    Large  |    33.0000  |  34.7000  |    36.3000  |        16  |           25  |         Driver only  |       Front  |          8  |      4.9000  |         200  |  4100  |          1510  |               No  |             18.0000  |           6  |     206  |        114  |     73  |           43  |              35  |            18  |    3620  |      USA  |  Cadillac DeVille  |
</pre>


#### Add Column(s)

Adding one or more columns while initializing the values for the new columns with a lambda is possible.

<?prettify?>
```java
//Add multiple columns for a given type
frame.cols().addAll(Array.of("A", "B", "C"), Double.class);

//Add multiple columns with explicit values
frame.cols().addAll(columns -> {
    columns.put("D", Array.of(1, 2, 3, 4, 5));
    columns.put("E", Array.of(true, false, true, true));
});

//Add calculated column by accessing row values by column key
frame.cols().add("MPG/Ratio_1", Double.class, v -> {
    double city = v.row().getDouble("MPG.city");
    double highway = v.row().getDouble("MPG.highway");
    return highway / city;
});

//Add calculated column by accessing row values by column ordinal
frame.cols().add("MPG/Ratio_2", Double.class, v -> {
    double city = v.row().getDoubleAt(6);
    double highway = v.row().getDoubleAt(7);
    return highway / city;
});
```

#### Add Row(s)

Adding one or more rows and initializing the values for the new rows with a lambda is also possible.

<?prettify?>
```java
//Add multiple rows given a set of new row keys
frame.rows().addAll(Array.of("Row-X", "Row-Y", "Row-Z"));

//Add a row with a new key, and compute average MPG for city / highway (all other values are null)
frame.rows().add("Row-Mean", v -> {
    switch (v.colKey()) {
        case "MPG.city":    return v.col().stats().mean().intValue();
        case "MPG.highway": return v.col().stats().mean().intValue();
        default:            return null;
    }
});
```

#### Remove Columns(s)

Removing columns cannot be performed in-place as with adding columns, so a new **shallow copy** of the `DataFrame` reference is returned.
When the original frame that contains the columns that were removed is no longer referenced, those columns will be **garbage collected**
along with the original frame.

<?prettify?>
```java
//Remove columns that end in "Price"
DataFrame<String,String> frame2 = frame.cols().remove(col -> {
    String colKey = col.key();
    return colKey.endsWith("Price");
});
```

<pre class="frame">
 Index   |  Manufacturer  |    Model     |   Type    |  MPG.city  |  MPG.highway  |       AirBags        |  DriveTrain  |  Cylinders  |  EngineSize  |  Horsepower  |  RPM   |  Rev.per.mile  |  Man.trans.avail  |  Fuel.tank.capacity  |  Passengers  |  Length  |  Wheelbase  |  Width  |  Turn.circle  |  Rear.seat.room  |  Luggage.room  |  Weight  |  Origin   |        Make        |
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Row-1  |         Acura  |     Integra  |    Small  |        25  |           31  |                None  |       Front  |          4  |      1.8000  |         140  |  6300  |          2890  |              Yes  |             13.2000  |           5  |     177  |        102  |     68  |           37  |            26.5  |            11  |    2705  |  non-USA  |     Acura Integra  |
  Row-2  |         Acura  |      Legend  |  Midsize  |        18  |           25  |  Driver & Passenger  |       Front  |          6  |      3.2000  |         200  |  5500  |          2335  |              Yes  |             18.0000  |           5  |     195  |        115  |     71  |           38  |              30  |            15  |    3560  |  non-USA  |      Acura Legend  |
  Row-3  |          Audi  |          90  |  Compact  |        20  |           26  |         Driver only  |       Front  |          6  |      2.8000  |         172  |  5500  |          2280  |              Yes  |             16.9000  |           5  |     180  |        102  |     67  |           37  |              28  |            14  |    3375  |  non-USA  |           Audi 90  |
  Row-4  |          Audi  |         100  |  Midsize  |        19  |           26  |  Driver & Passenger  |       Front  |          6  |      2.8000  |         172  |  5500  |          2535  |              Yes  |             21.1000  |           6  |     193  |        106  |     70  |           37  |              31  |            17  |    3405  |  non-USA  |          Audi 100  |
  Row-5  |           BMW  |        535i  |  Midsize  |        22  |           30  |         Driver only  |        Rear  |          4  |      3.5000  |         208  |  5700  |          2545  |              Yes  |             21.1000  |           4  |     186  |        109  |     69  |           39  |              27  |            13  |    3640  |  non-USA  |          BMW 535i  |
  Row-6  |         Buick  |     Century  |  Midsize  |        22  |           31  |         Driver only  |       Front  |          4  |      2.2000  |         110  |  5200  |          2565  |               No  |             16.4000  |           6  |     189  |        105  |     69  |           41  |              28  |            16  |    2880  |      USA  |     Buick Century  |
  Row-7  |         Buick  |     LeSabre  |    Large  |        19  |           28  |         Driver only  |       Front  |          6  |      3.8000  |         170  |  4800  |          1570  |               No  |             18.0000  |           6  |     200  |        111  |     74  |           42  |            30.5  |            17  |    3470  |      USA  |     Buick LeSabre  |
  Row-8  |         Buick  |  Roadmaster  |    Large  |        16  |           25  |         Driver only  |        Rear  |          6  |      5.7000  |         180  |  4000  |          1320  |               No  |             23.0000  |           6  |     216  |        116  |     78  |           45  |            30.5  |            21  |    4105  |      USA  |  Buick Roadmaster  |
  Row-9  |         Buick  |     Riviera  |  Midsize  |        19  |           27  |         Driver only  |       Front  |          6  |      3.8000  |         170  |  4800  |          1690  |               No  |             18.8000  |           5  |     198  |        108  |     73  |           41  |            26.5  |            14  |    3495  |      USA  |     Buick Riviera  |
 Row-10  |      Cadillac  |     DeVille  |    Large  |        16  |           25  |         Driver only  |       Front  |          8  |      4.9000  |         200  |  4100  |          1510  |               No  |             18.0000  |           6  |     206  |        114  |     73  |           43  |              35  |            18  |    3620  |      USA  |  Cadillac DeVille  |
</pre>

#### Remove Rows(s)

Removing rows cannot be performed in-place as with adding rows, so a new **deep copy** of the `DataFrame` reference is returned. There is
no option but to return a deep copy given the **column store** nature of the internal storage. As a result, one should be aware that
removing rows can be an expensive operation, in which case it may make more sense to create a filter by slicing in the row dimension.

<?prettify?>
```java
//Remove rows that include Buicks
DataFrame<String,String> result = frame.rows().remove(row -> {
    String value = row.getValue("Manufacturer");
    return !value.equals("Buick");
});
```

<pre class="frame">
 Index   |  Manufacturer  |   Model    |   Type    |  Min.Price  |   Price   |  Max.Price  |  MPG.city  |  MPG.highway  |       AirBags        |  DriveTrain  |  Cylinders  |  EngineSize  |  Horsepower  |  RPM   |  Rev.per.mile  |  Man.trans.avail  |  Fuel.tank.capacity  |  Passengers  |  Length  |  Wheelbase  |  Width  |  Turn.circle  |  Rear.seat.room  |  Luggage.room  |  Weight  |  Origin   |         Make         |
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Row-1  |         Acura  |   Integra  |    Small  |    12.9000  |  15.9000  |    18.8000  |        25  |           31  |                None  |       Front  |          4  |      1.8000  |         140  |  6300  |          2890  |              Yes  |             13.2000  |           5  |     177  |        102  |     68  |           37  |            26.5  |            11  |    2705  |  non-USA  |       Acura Integra  |
  Row-2  |         Acura  |    Legend  |  Midsize  |    29.2000  |  33.9000  |    38.7000  |        18  |           25  |  Driver & Passenger  |       Front  |          6  |      3.2000  |         200  |  5500  |          2335  |              Yes  |             18.0000  |           5  |     195  |        115  |     71  |           38  |              30  |            15  |    3560  |  non-USA  |        Acura Legend  |
  Row-3  |          Audi  |        90  |  Compact  |    25.9000  |  29.1000  |    32.3000  |        20  |           26  |         Driver only  |       Front  |          6  |      2.8000  |         172  |  5500  |          2280  |              Yes  |             16.9000  |           5  |     180  |        102  |     67  |           37  |              28  |            14  |    3375  |  non-USA  |             Audi 90  |
  Row-4  |          Audi  |       100  |  Midsize  |    30.8000  |  37.7000  |    44.6000  |        19  |           26  |  Driver & Passenger  |       Front  |          6  |      2.8000  |         172  |  5500  |          2535  |              Yes  |             21.1000  |           6  |     193  |        106  |     70  |           37  |              31  |            17  |    3405  |  non-USA  |            Audi 100  |
  Row-5  |           BMW  |      535i  |  Midsize  |    23.7000  |  30.0000  |    36.2000  |        22  |           30  |         Driver only  |        Rear  |          4  |      3.5000  |         208  |  5700  |          2545  |              Yes  |             21.1000  |           4  |     186  |        109  |     69  |           39  |              27  |            13  |    3640  |  non-USA  |            BMW 535i  |
 Row-10  |      Cadillac  |   DeVille  |    Large  |    33.0000  |  34.7000  |    36.3000  |        16  |           25  |         Driver only  |       Front  |          8  |      4.9000  |         200  |  4100  |          1510  |               No  |             18.0000  |           6  |     206  |        114  |     73  |           43  |              35  |            18  |    3620  |      USA  |    Cadillac DeVille  |
 Row-11  |      Cadillac  |   Seville  |  Midsize  |    37.5000  |  40.1000  |    42.7000  |        16  |           25  |  Driver & Passenger  |       Front  |          8  |      4.6000  |         295  |  6000  |          1985  |               No  |             20.0000  |           5  |     204  |        111  |     74  |           44  |              31  |            14  |    3935  |      USA  |    Cadillac Seville  |
 Row-12  |     Chevrolet  |  Cavalier  |  Compact  |     8.5000  |  13.4000  |    18.3000  |        25  |           36  |                None  |       Front  |          4  |      2.2000  |         110  |  5200  |          2380  |              Yes  |             15.2000  |           5  |     182  |        101  |     66  |           38  |              25  |            13  |    2490  |      USA  |  Chevrolet Cavalier  |
 Row-13  |     Chevrolet  |   Corsica  |  Compact  |    11.4000  |  11.4000  |    11.4000  |        25  |           34  |         Driver only  |       Front  |          4  |      2.2000  |         110  |  5200  |          2665  |              Yes  |             15.6000  |           5  |     184  |        103  |     68  |           39  |              26  |            14  |    2785  |      USA  |   Chevrolet Corsica  |
 Row-14  |     Chevrolet  |    Camaro  |   Sporty  |    13.4000  |  15.1000  |    16.8000  |        19  |           28  |  Driver & Passenger  |        Rear  |          6  |      3.4000  |         160  |  4600  |          1805  |              Yes  |             15.5000  |           4  |     193  |        101  |     74  |           43  |              25  |            13  |    3240  |      USA  |    Chevrolet Camaro  |
</pre>

### Slicing / Filtering

A `DataFrame` can be sliced or filtered in either the row or column dimension, which returns a new `DataFrame` reference that exposes
a subset of the row or column data of the original frame. Slices can be taken on an already sliced frame to create a yet further
restricted view on the data. The only API operation that is **not supported** on a sliced `DataFrame` is the addition of new rows. To
add rows to a sliced frame requires a copy to be created first, which can easily be achieved via a call to `copy()`.

#### Row Slicing

#### Column Slicing

#### Row & Column Slicing

### Updating

#### Values

#### Update Row(s)

#### Update Column(s)

### Mapping Values

### Writing

#### To CSV

#### To JSON

#### To SQL Database
