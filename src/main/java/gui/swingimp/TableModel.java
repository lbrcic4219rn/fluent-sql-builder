package gui.swingimp;

import database.QueryResult;

import javax.swing.table.AbstractTableModel;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class TableModel extends AbstractTableModel {

    @Serial
    private static final long serialVersionUID = 1L;

    private transient QueryResult result = QueryResult.empty();
    private transient List<Class<?>> columnClasses = List.of();

    private static List<Class<?>> columnClassesOf(QueryResult result) {
        List<Class<?>> classes = new ArrayList<>();
        for (int column = 0; column < result.columns().size(); column++) {
            classes.add(columnClassOf(result, column));
        }
        return classes;
    }

    private static Class<?> columnClassOf(QueryResult result, int column) {
        Class<?> found = null;
        for (List<Object> row : result.rows()) {
            Object value = row.get(column);
            if (value == null) {
                continue;
            }
            if (found != null && found != value.getClass()) {
                return Object.class;
            }
            found = value.getClass();
        }
        return found == null ? Object.class : found;
    }

    public void setResult(QueryResult result) {
        this.result = result;
        this.columnClasses = columnClassesOf(result);
        fireTableStructureChanged();
    }

    public void clear() {
        setResult(QueryResult.empty());
    }

    @Override
    public int getRowCount() {
        return result.rows().size();
    }

    @Override
    public int getColumnCount() {
        return result.columns().size();
    }

    @Override
    public String getColumnName(int column) {
        return result.columns().get(column);
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnClasses.get(column);
    }

    @Override
    public Object getValueAt(int row, int column) {
        return result.rows().get(row).get(column);
    }
}
