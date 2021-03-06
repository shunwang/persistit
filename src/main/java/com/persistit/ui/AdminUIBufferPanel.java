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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.persistit.Management;

public class AdminUIBufferPanel extends AdminPanel implements AdminCommand {
    AdminUI _adminUI;

    private ManagementTableModel _bufferPoolInfoArrayModel;
    private ManagementTableModel _bufferInfoArrayModel;

    private Map _menuMap = new HashMap();

    private int _bufferPoolIndex = -1;
    private int _selectedTraversalType = 0;
    private String _selectedIncludeMask = null;
    private String _selectedExcludeMask = null;
    private String _detailMask = "";

    // private JComboBox _modeCombo;
    private List _toggleList;
    private boolean _refreshing;

    private TitledBorder _poolDetailBorder;
    private String _poolDetailBorderPattern;

    private JPanel _poolInfoPanel;
    private JPanel _poolDetailPanel;

    @Override
    protected void setup(AdminUI ui) throws NoSuchMethodException, RemoteException {
        _adminUI = ui;
        _poolInfoPanel = new JPanel(new BorderLayout());
        _poolDetailPanel = new JPanel(new BorderLayout());

        _bufferPoolInfoArrayModel = new ManagementTableModel(Management.BufferPoolInfo.class, "BufferPoolInfo", ui);

        _bufferInfoArrayModel = new ManagementTableModel(Management.BufferInfo.class, "BufferInfo", ui);

        final JTable table1 = new JTable(_bufferPoolInfoArrayModel);
        table1.setAutoCreateRowSorter(true);

        table1.setPreferredScrollableViewportSize(new Dimension(800, 80));
        table1.setAutoCreateColumnsFromModel(false);
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _bufferPoolInfoArrayModel.formatColumns(table1, _detailMask);

        final JTable table2 = new JTable(_bufferInfoArrayModel);
        table2.setAutoCreateRowSorter(true);
        table2.setPreferredScrollableViewportSize(new Dimension(800, 400));
        table2.setAutoCreateColumnsFromModel(false);
        table2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _bufferInfoArrayModel.formatColumns(table2, _detailMask);

        _poolInfoPanel.setBorder(_adminUI.createTitledBorder("BufferPanel.summaryCaption"));

        _poolInfoPanel.add(new JScrollPane(table1), BorderLayout.CENTER);

        _poolDetailBorderPattern = _adminUI.getProperty("BufferPanel.detailCaption");

        _poolDetailBorder = _adminUI.createTitledBorder("BufferPanel.detailCaptionEmpty");

        _poolDetailPanel.setBorder(_poolDetailBorder);
        _poolDetailPanel.add(new JScrollPane(table2), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.add(_poolInfoPanel);
        splitPane.add(_poolDetailPanel);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);

        add(createBufferSelectorPanel(), BorderLayout.SOUTH);

        table1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                int index = table1.getSelectedRow();
                if (!_refreshing && !lse.getValueIsAdjusting() && index >= 0) {
                    _bufferPoolIndex = index;
                    _adminUI.scheduleRefresh(-1);
                }
            }
        });

        _adminUI.scheduleRefresh(-1);
    }

    private JPanel createBufferSelectorPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        // AdminUI.AdminAction mainModeAction = _adminUI.createAction(
        // this,
        // _adminUI.getProperty("BufferPanel.mode"));
        //
        // JMenu modeMenu = new JMenu(mainModeAction);
        // mainModeAction.addButton(modeMenu);
        //
        // List modeList = new ArrayList();
        //
        // for (int index = 0; ; index++)
        // {
        // String s = _adminUI.getProperty("BufferPanel.mode." + index);
        // if (s == null) break;
        // Action modeAction = _adminUI.createAction(this, s);
        // modeMenu.add(new JMenuItem(modeAction));
        // modeList.add(modeAction);
        // }
        // Action modeAction =
        // _adminUI.createAction(this,
        // _adminUI.getProperty("BufferPanel.mode"));
        //
        // _modeCombo = new JComboBox(modeList.toArray());
        // _modeCombo.addActionListener(modeAction);

        _toggleList = new ArrayList();
        // panel.add(_modeCombo);

        for (int index = 0;; index++) {
            String s = _adminUI.getProperty("BufferPanel.toggle." + index);
            if (s == null)
                break;
            AdminUI.AdminAction action = _adminUI.createAction(this, s);
            JToggleButton toggle = new JToggleButton(action);
            action.addButton(toggle);
            panel.add(toggle);
            _toggleList.add(toggle);
        }

        // JMenu[] menus = new JMenu[] {modeMenu};
        // _menuMap.put("VIEW", menus);
        return panel;
    }

    @Override
    public void actionPerformed(AdminUI.AdminAction action, ActionEvent ae) {
        String name = action.getName();
        Object source = ae.getSource();
        // if ("BUFFER_TABLE_MODE".equals(name))
        // {
        // int selected = ((JComboBox)source).getSelectedIndex();
        // _selectedTraversalType = selected;
        // _adminUI.scheduleRefresh(-1);
        // }
        // else if (name.startsWith("BUFFER_TABLE_MODE_"))
        // {
        // int mode =
        // Integer.parseInt(name.substring("BUFFER_TABLE_MODE_".length()));
        // _modeCombo.setSelectedIndex(mode);
        // }
        // else
        if ("VALID".equals(name) || "DIRTY".equals(name) || "READER".equals(name) || "WRITER".equals(name)) {
            boolean selected = ((JToggleButton) source).isSelected();
            char selectionChar = name.toLowerCase().charAt(0);
            int p = _selectedIncludeMask == null ? -1 : _selectedIncludeMask.indexOf(selectionChar);
            if (selected && p < 0) {
                if (_selectedIncludeMask == null)
                    _selectedIncludeMask = "";
                _selectedIncludeMask += selectionChar;
            }
            if (!selected && p >= 0) {
                _selectedIncludeMask = new StringBuilder(_selectedIncludeMask).deleteCharAt(p).toString();
                if (_selectedIncludeMask.length() == 0) {
                    _selectedIncludeMask = null;
                }
            }
            _adminUI.scheduleRefresh(-1);
        }
    }

    @Override
    protected void refresh(boolean reset) {
        synchronized (this) {
            if (_refreshing)
                return;
            _refreshing = true;
        }
        try {
            Management management = _adminUI.getManagement();
            if (reset) {
                _bufferPoolIndex = -1;
                _selectedExcludeMask = null;
                _selectedIncludeMask = null;
                _selectedTraversalType = 0;
                if (_toggleList != null) {
                    for (int index = 0; index < _toggleList.size(); index++) {
                        JToggleButton toggle = (JToggleButton) _toggleList.get(index);
                        toggle.setSelected(false);
                    }
                }
                // if (_modeCombo != null)
                // {
                // _modeCombo.setSelectedIndex(0);
                // }
            }

            Management.BufferPoolInfo[] bufferPoolInfoArray = management == null ? null : management
                    .getBufferPoolInfoArray();

            _bufferPoolInfoArrayModel.setInfoArray(bufferPoolInfoArray);

            if (_bufferPoolIndex < 0)
                _bufferPoolIndex = 0;
            if (_bufferPoolIndex >= 0 && bufferPoolInfoArray != null && _bufferPoolIndex < bufferPoolInfoArray.length) {
                int bufferSize = bufferPoolInfoArray[_bufferPoolIndex].getBufferSize();
                _bufferInfoArrayModel.setInfoArray(management.getBufferInfoArray(bufferSize, _selectedTraversalType,
                        _selectedIncludeMask, _selectedExcludeMask));

                _poolDetailBorder.setTitle(MessageFormat.format(_poolDetailBorderPattern, new Object[] { _adminUI
                        .formatInteger(bufferSize) }));
                _poolDetailPanel.repaint(0, 0, 1000, 30);
            } else {
                _bufferInfoArrayModel.setInfoArray(null);
                _poolDetailBorder.setTitle(_adminUI.getProperty("BufferPanel.detailCaptionEmpty"));
            }
        } catch (RemoteException re) {
            _adminUI.postException(re);
        } finally {
            synchronized (this) {
                _refreshing = false;
            }
        }
    }

    @Override
    protected Map getMenuMap() {
        return _menuMap;
    }

    @Override
    protected void setDefaultButton() {
        getRootPane().setDefaultButton(null);
    }
}
