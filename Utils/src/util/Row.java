package util;

import java.util.HashSet;

/**
 * Created by KaDon on 3/15/2017.
 */
public class Row {
    public long timeStamp;
    public Double[] data;

    public Double[] calculateAverage(HashSet<Row> rowSet) {
        Double[] averageRow = new Double[rowSet.size()];
        if (rowSet.size() > 0) {
            for (Row row: rowSet) {
                for (int i = 0; i < row.data.length; i++) {
                    averageRow[i] = averageRow[i] + row.data[i];
                }
            }
            for (int i = 0; i < averageRow.length; i++) {
                averageRow[i] = averageRow[i]/ averageRow.length;
            }
        }
        return averageRow;
    }
}
