/**
 * Copyright © 2005-2012 Akiban Technologies, Inc.  All rights reserved.
 * 
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program may also be available under different license terms.
 * For more information, see www.akiban.com or contact licensing@akiban.com.
 * 
 * Contributors:
 * Akiban Technologies, Inc.
 */

package com.persistit.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.persistit.Key;
import com.persistit.KeyState;
import com.persistit.Persistit;
import com.persistit.Value;
import com.persistit.ValueState;

/**
 * @version 1.0
 */
public class ManagementTableModel extends AbstractTableModel {

    protected final static Class[] NO_ARG_TEMPLATE = new Class[0];
    protected final static Object[] NO_ARG_LIST = new Object[0];

    protected AdminUI _adminUI;
    protected int _columnCount;
    protected int _displayedColumnCount;
    protected boolean[] _displayedColumns;
    protected int[] _displayedColumnIndex;
    protected String[] _flags;
    protected Method[] _methods;
    protected int[] _widths;
    protected String[] _headers;
    protected TableCellRenderer[] _renderers;
    protected int _totalWidth;
    protected Object[] _infoArray;

    /**
     * Construct a TableModel to display one of the management info array
     * structures such as BufferInfo[]. Each column is described by a property
     * defined in the <code>ResourceBundle</code> associated with this class.
     * The property name is constructed from the last "." piece of the name of
     * the supplied class and the column index as follows: <code><pre>
     * property Name ::= 
     *      <i>classNameTail</i>.column.<i>columnIndex</i>
     * </pre></code> For example, the specification for the first column of the
     * {@link com.persistit.Management.BufferInfo} class is <code><pre>
     *      BufferInfo.column.0
     * </pre></code> The value associated with this property has the following
     * structure: <code><pre>
     * property Value ::=
     *      <i>accessorMethodName</i>:<i>width</i>:<i>flags</i>:<i>header</i>
     * </code></pre> For example <code><pre>
     *      getRightSiblingAddress:10:A:Right Pointer
     * </code></pre> where the <i>accessorMethodName</code> is simply a method
     * name in the supplied class (the method must take no arguments), the
     * <i>width</i> is a percentage of the total table width, and
     * <i>justification</i> is L, C or R.
     * 
     * @param clazz
     */

    public ManagementTableModel(Class clazz, String className, AdminUI ui) throws NoSuchMethodException {
        _adminUI = ui;

        int p = className.lastIndexOf('.');
        if (p >= 0)
            className = className.substring(p + 1);
        ArrayList specList = new ArrayList();
        for (int index = 0;; index++) {
            String propName = className + ".column." + index;
            String value = ui.getProperty(propName);
            if (value == null || value.startsWith("."))
                break;
            specList.add(value);
        }
        String[] columnSpecs = (String[]) specList.toArray(new String[0]);
        setup(clazz, columnSpecs);
    }

    protected void setup(Class clazz, String[] columnSpecs) throws NoSuchMethodException {
        _columnCount = columnSpecs.length;
        _displayedColumnCount = 0;
        _displayedColumns = new boolean[_columnCount];
        _displayedColumnIndex = new int[_columnCount];
        _widths = new int[_columnCount];
        _flags = new String[_columnCount];
        _headers = new String[_columnCount];
        _methods = new Method[_columnCount];
        _renderers = new TableCellRenderer[_columnCount];

        for (int index = 0; index < columnSpecs.length; index++) {
            StringTokenizer st = new StringTokenizer(columnSpecs[index], ":");
            String methodName = st.nextToken();
            int width = Integer.parseInt(st.nextToken());
            String flags = st.nextToken();
            String header = st.nextToken();
            String rendererName = null;
            int minWidth = width / 2;
            if (st.hasMoreTokens()) {
                minWidth = Integer.parseInt(st.nextToken());
            }
            if (st.hasMoreTokens()) {
                rendererName = st.nextToken();
            }

            _totalWidth += width;
            _widths[index] = width;
            _flags[index] = flags;
            _methods[index] = clazz.getMethod(methodName, NO_ARG_TEMPLATE);
            if (rendererName != null)
                _renderers[index] = constructRenderer(rendererName, clazz, columnSpecs[index]);
            _headers[index] = header;
        }
    }

    public void setInfoArray(Object[] array) {
        // int oldLength = _infoArray == null ? 0 : _infoArray.length;
        // int newLength = array == null ? 0 : array.length;
        // if (oldLength != newLength && oldLength != 0)
        // {
        // fireTableRowsDeleted(0, oldLength);
        // }
        // Object[] oldArray = _infoArray;
        // _infoArray = array;
        // if (oldLength != newLength)
        // {
        // fireTableRowsInserted(0, newLength);
        // }
        // else if (newLength > 0)
        // {
        // fireTableRowsUpdated(0, newLength);
        // }
        _infoArray = array;
        fireTableDataChanged();
    }

    public Object[] getInfoArray() {
        return _infoArray;
    }

    @Override
    public int getColumnCount() {
        return _displayedColumnCount;
    }

    @Override
    public int getRowCount() {
        if (_infoArray == null)
            return 0;
        return _infoArray.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (_infoArray == null || row < 0 || row >= _infoArray.length) {
            return null;
        }
        if (col == -1)
            return _infoArray[row];

        try {
            Object struct = _infoArray[row];
            int index = _displayedColumnIndex[col];
            if (index < 0)
                return null;
            Object value = _methods[index].invoke(struct, NO_ARG_LIST);
            return value;
        } catch (Exception e) {
            return e;
        }
    }

    public void formatColumns(JTable table, String flags) {
        int trueCount = 0;
        boolean changed = false;
        for (int i = 0; i < _columnCount; i++) {
            boolean value = _flags[i].indexOf('A') >= 0;
            if (flags != null) {
                for (int j = flags.length(); !value && --j > 0;) {
                    if (_flags[i].indexOf(flags.charAt(j)) >= 0)
                        value = true;
                }
            }
            if (value)
                trueCount++;
            if (value != _displayedColumns[i]) {
                _displayedColumns[i] = value;
                changed = true;
            }
        }
        if (_displayedColumnCount != trueCount)
            changed = true;
        _displayedColumnCount = trueCount;
        if (changed) {
            TableColumnModel tcm = table.getColumnModel();
            int count = tcm.getColumnCount();
            if (count > _displayedColumnCount) {
                for (int i = _displayedColumnCount; --i >= count;) {
                    tcm.removeColumn(tcm.getColumn(i));
                }
            }

            int index = 0;
            for (int i = 0; i < _columnCount; i++) {
                if (_displayedColumns[i]) {
                    TableColumn tc;
                    if (index >= count) {
                        tc = new TableColumn();
                        tcm.addColumn(tc);
                    } else {
                        tc = tcm.getColumn(index);
                    }
                    tc.setHeaderValue(_headers[i]);
                    tc.setPreferredWidth(_widths[i] % 10000);
                    tc.setMinWidth(_widths[i] / 10000);
                    _displayedColumnIndex[index] = i;
                    tc.setModelIndex(index);
                    if (_renderers[i] != null) {
                        tc.setCellRenderer(_renderers[i]);
                    }

                    index++;
                }
            }
            table.setDefaultRenderer(Double.class, new DoubleRenderer());
            table.setDefaultRenderer(Long.class, new LongRenderer());
            table.setDefaultRenderer(Integer.class, new IntegerRenderer());
            table.setDefaultRenderer(KeyState.class, new KeyStateRenderer());
            table.setDefaultRenderer(ValueState.class, new ValueStateRenderer());
            fireTableStructureChanged();
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    @Override
    public Class getColumnClass(int col) {
        int index = _displayedColumnIndex[col];
        Class clazz = _methods[index].getReturnType();
        if (clazz.isPrimitive()) {
            if (clazz == boolean.class)
                return Boolean.class;
            if (clazz == int.class)
                return Integer.class;
            if (clazz == long.class)
                return Long.class;
            if (clazz == float.class)
                return Float.class;
            if (clazz == double.class)
                return Double.class;
        }
        return clazz;
    }

    /**
     * Default Renderers
     **/

    class AlignedCellRenderer extends DefaultTableCellRenderer {
        AlignedCellRenderer(int alignment) {
            super();
            setHorizontalAlignment(alignment);
        }
    }

    class LongRenderer extends AlignedCellRenderer {

        public LongRenderer() {
            super(SwingConstants.RIGHT);
        }

        @Override
        public void setValue(Object value) {
            setText((value == null) ? "" : value instanceof Long ? _adminUI.formatLong(((Long) value).longValue())
                    : value.toString());
        }
    }

    class IntegerRenderer extends AlignedCellRenderer {

        public IntegerRenderer() {
            super(SwingConstants.RIGHT);
        }

        @Override
        public void setValue(Object value) {
            setText((value == null) ? "" : value instanceof Integer ? _adminUI.formatInteger(((Integer) value)
                    .intValue()) : value.toString());
        }
    }

    class DoubleRenderer extends AlignedCellRenderer {

        public DoubleRenderer() {
            super(SwingConstants.RIGHT);
        }

        @Override
        public void setValue(Object value) {
            setText((value == null) ? "" : value instanceof Double ? _adminUI.formatPercent(((Double) value)
                    .doubleValue()) : value.toString());
        }
    }

    class KeyStateRenderer extends AlignedCellRenderer {
        private Key _key;

        public KeyStateRenderer() {
            super(SwingConstants.LEFT);
            _key = new Key((Persistit) null);
        }

        @Override
        public void setValue(Object value) {
            if (value instanceof KeyState) {
                ((KeyState) value).copyTo(_key);
                setText(_key.toString());
            } else
                setText(value.toString());
        }
    }

    class ValueStateRenderer extends AlignedCellRenderer {
        private Value _value;

        public ValueStateRenderer() {
            super(SwingConstants.LEFT);
            _value = new Value((Persistit) null);
        }

        @Override
        public void setValue(Object value) {
            if (value instanceof ValueState) {
                ((ValueState) value).copyTo(_value);
                setText(_value.toString());
            } else
                setText(value.toString());
        }
    }

    public static abstract class AbstractCustomTableCellRenderer extends DefaultTableCellRenderer {

        protected abstract void setup(AdminUI ui, Class infClass, String columnSpec);
    }

    protected TableCellRenderer constructRenderer(String rendererName, Class infoClass, String columnSpec) {
        String className = getClass().getName();
        className = className.substring(0, className.lastIndexOf('.')) + ".renderers." + rendererName;
        try {
            Class clazz = Class.forName(className);
            Object object = clazz.newInstance();
            if (object instanceof AbstractCustomTableCellRenderer) {
                ((AbstractCustomTableCellRenderer) object).setup(_adminUI, infoClass, columnSpec);
                return (TableCellRenderer) object;
            }
        } catch (Exception e) {
            _adminUI.postException(e);
        }
        return null;
    }
}
