package com.d3x.morpheus.source;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Stream;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.Asserts;
import com.d3x.morpheus.util.Predicates;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * A unit test of the DataFrame Excel reader
 *
 * @author  Dwight Gunning
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class ExcelTests {

    @DataProvider(name="excelType")
    public Object[][] excelTypes() {
        return new Object[][] {
                {"xlsx"},
                {"xls"}
        };
    }


    @Test(dataProvider = "excelType")
    public void testBasicReadWithHeader(String excelType) throws IOException{
        DataFrame<Integer,String> frame = DataFrame.source(ExcelSource.class).read(options -> {
            options.setResource("/xls/cars93." + excelType);
            options.setHeader(true);
        });
        frame.out().print();
        assertNotNull(frame);
        assertEquals(frame.rowCount(), 93);
        assertEquals(frame.cols().count(), 29);
        assertEquals(frame.cols().type("Column-0"), Double.class);
        assertEquals(frame.cols().type("Date"), Date.class);
        assertEquals(frame.cols().type("Manufacturer"), String.class);
        assertEquals(frame.cols().type("Min.Price"), Double.class);
        assertEquals(frame.getDoubleAt(0, 0), 1d);
        assertEquals(frame.rows().getValueAt(0, "Manufacturer"), "Acura");
        assertEquals(frame.rows().getDoubleAt(0, "Min.Price"), 12.9d);
        assertEquals(frame.rows().getValueAt(7, "Manufacturer"), "Buick");
        assertEquals(frame.rows().getDoubleAt(7, "Min.Price"), 22.6);
        assertTrue(frame.cols().keys().allMatch(Predicates.in(
                "Column-0", "Date", "Manufacturer","Model","Type","Min.Price","Price","Max.Price","MPG.city","MPG.highway","AirBags","DriveTrain",
                "Cylinders","EngineSize","Horsepower","RPM","Rev.per.mile",	"Man.trans.avail",	"Fuel.tank.capacity",
                "Passengers","Length","Wheelbase","Width","Turn.circle","Rear.seat.room","Luggage.room","Weight","Origin","Make"
        )));
    }


    @Test(dataProvider = "excelType")
    public void testBasicReadWithoutHeader(String excelType) throws IOException{
        DataFrame<Integer,String> frame = DataFrame.source(ExcelSource.class).read(options -> {
            options.setResource("/xls/cars93." + excelType);
            options.setTopLeft(new ExcelSource.Coordinate(0, 1));
            options.setHeader(false);
        });
        frame.out().print();
        assertNotNull(frame);
        assertEquals(frame.rowCount(), 94);
        assertEquals(frame.cols().count(), 28);
        assertEquals(frame.cols().type("Column-0"), Object.class);
        assertEquals(frame.cols().type("Column-1"), String.class);
        assertEquals(frame.cols().type("Column-4"), Object.class);
        assertEquals(frame.rows().getValueAt(1, "Column-1"), "Acura");
        assertEquals(frame.rows().getDoubleAt(1, "Column-4"), 12.9d);
        assertEquals(frame.rows().getValueAt(8, "Column-1"), "Buick");
        assertEquals(frame.rows().getDoubleAt(8, "Column-4"), 22.6);
    }



    @Test(dataProvider = "excelType")
    public void testBasicReadWithHeaderAndCoordinates(String excelType) throws IOException{
        DataFrame<Integer,String> frame = DataFrame.source(ExcelSource.class).read(options -> {
            options.setResource("/xls/cars93." + excelType);
            options.setTopLeft(new ExcelSource.Coordinate(0, 1));
            options.setBottomRight(new ExcelSource.Coordinate(5, 11));
            options.setHeader(true);
        });
        frame.out().print();
        assertEquals(frame.cols().type("Date"), Date.class);
        assertEquals(frame.cols().type("Manufacturer"), String.class);
        assertEquals(frame.cols().type("Min.Price"), Double.class);
        assertNotNull(frame);
        assertEquals(frame.rowCount(), 5);
        assertEquals(frame.cols().count(), 11);
        assertTrue(frame.cols().keys().allMatch(Predicates.in(
                "Date", "Manufacturer","Model","Type","Min.Price","Price","Max.Price","MPG.city","MPG.highway","AirBags","DriveTrain", "Cylinders"
        )));
    }



    @Test()
    public void testAppleQuotes() throws Exception {
        DataFrame<Integer,String> frame = DataFrame.source(ExcelSource.class).read(options -> {
            options.setResource("/xls/aapl.xlsx");
            options.setHeader(true);
        }).mapToObjects("Date", LocalDate.class, v -> {
            final Date date = v.getValue();
            final Instant instant = date.toInstant();
            return LocalDateTime.ofInstant(instant, ZoneId.of("GMT")).toLocalDate();
        });
        assertNotNull(frame);
        assertEquals(frame.rowCount(), 8503);
        assertEquals(frame.cols().count() ,7);
        assertAppleFrame(frame);
    }




    @Test(dataProvider = "excelType")
    public void readAppleFromNamedSheet(String excelType){
        DataFrame<Integer,String> frame = DataFrame.source(ExcelSource.class).read(options -> {
            options.setResource("/xls/ApplesAndCars." + excelType);
            options.setHeader(true);
            options.setSheetName("Apple");
        }).mapToObjects("Date", LocalDate.class, v -> {
            final Date date = v.getValue();
            final Instant instant = date.toInstant();
            return LocalDateTime.ofInstant(instant, ZoneId.of("GMT")).toLocalDate();
        });
        assertEquals(frame.rowCount() ,43);
        assertEquals(frame.colCount() ,7);
        assertAppleFrame(frame);

        DataFrame<Integer,String> cars = ExcelSource.load(options -> {
            options.setResource("/xls/ApplesAndCars." + excelType);
            options.setSheetName("Cars");
        });
        assertEquals(frame.cols().count() ,7);
    }



    @Test(dataProvider = "excelType")
    public void readCarsFromNamedSheet(String excelType){
        DataFrame<Integer,String> frame = DataFrame.source(ExcelSource.class).read(options -> {
            options.setResource("/xls/ApplesAndCars." + excelType);
            options.setSheetName("Cars");
            options.setHeader(true);
        });

        assertEquals(frame.rowCount(), 11);
        assertEquals(frame.cols().count(), 6);
        assertEquals(frame.cols().type("Manufacturer"), String.class);
        assertEquals(frame.cols().type("Model"), Object.class);
        assertEquals(frame.cols().type("Type"), String.class);
        assertEquals(frame.cols().type("Min.Price"), Double.class);
        assertEquals(frame.cols().type("Price"), Double.class);
        assertEquals(frame.cols().type("Max.Price"), Double.class);
        assertEquals(frame.rows().getValueAt(0, "Manufacturer"), "Acura");
        assertEquals(frame.rows().getDoubleAt(0, "Min.Price"), 12.9d);
        assertEquals(frame.rows().getValueAt(7, "Manufacturer"), "Buick");
        assertEquals(frame.rows().getDoubleAt(7, "Min.Price"), 22.6);
    }



    /**
     * Makes assertions about the structure and content of the Apple worksheet
     * @param frame     the DataFrame
     */
    private void assertAppleFrame(DataFrame<Integer,String> frame) {
        Stream.of("Date","Open", "High", "Low", "Close", "Volume", "Adj Close").forEach(name -> {
            Asserts.assertTrue(frame.cols().contains(name), "Frame contains column named: " + name);
        });
        assertEquals(frame.cols().type("Date"), LocalDate.class);
        assertEquals(frame.cols().type("Open"), Double.class);
        assertEquals(frame.cols().type("High"), Double.class);
        assertEquals(frame.cols().type("Low"), Double.class);
        assertEquals(frame.cols().type("Close"), Double.class);
        assertEquals(frame.cols().type("Volume"), Double.class);
        assertEquals(frame.cols().type("Adj Close"), Double.class);
        assertEquals(frame.rows().getValueAt(0, "Date"), LocalDate.of(1980, 12, 12));
        assertEquals(frame.rows().getDoubleAt(0, "Open"), 28.74984, 0.00001);
        assertEquals(frame.rows().getDoubleAt(0, "High"), 28.87472, 0.00001);
        assertEquals(frame.rows().getDoubleAt(0, "Low"), 28.74984, 0.00001);
        assertEquals(frame.rows().getDoubleAt(0, "Close"), 28.74984, 0.00001);
        assertEquals(frame.rows().getDoubleAt(0, "Volume"), 117258400d, 0.00001);
        assertEquals(frame.rows().getDoubleAt(0, "Adj Close"), 0.44203, 0.00001);
        assertEquals(frame.rows().getValueAt(10, "Date"), LocalDate.of(1980, 12, 29));
        assertEquals(frame.rows().getDoubleAt(10, "Open"), 36.00016, 0.00001);
        assertEquals(frame.rows().getDoubleAt(10, "High"), 36.12504, 0.00001);
        assertEquals(frame.rows().getDoubleAt(10, "Low"), 36.00016, 0.00001);
        assertEquals(frame.rows().getDoubleAt(10, "Close"), 36.00016, 0.00001);
        assertEquals(frame.rows().getDoubleAt(10, "Volume"), 23290400d, 0.00001);
        assertEquals(frame.rows().getDoubleAt(10, "Adj Close"), 0.5535, 0.00001);
    }

}
