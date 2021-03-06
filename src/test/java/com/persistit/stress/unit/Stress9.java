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

package com.persistit.stress.unit;

import java.util.ArrayList;

import com.persistit.Exchange;
import com.persistit.Key;
import com.persistit.Value;
import com.persistit.stress.TestResult;
import com.persistit.util.ArgParser;

public class Stress9 extends StressBase {

    private final static String[] ARGS_TEMPLATE = { "op|String:wrtd|Operations to perform",
            "repeat|int:1:0:1000000000|Repetitions", "count|int:1000:0:1000000000|Number of nodes to populate",
            "size|int:4000:1:2000000|Approximate size of each data value", "seed|int:1:1:20000|Random seed",
            "splay|int:1:1:1000|Splay", };

    int _size;
    int _splay;
    int _seed;
    String _opflags;

    ArrayList _testValue = new ArrayList();

    public Stress9(String argsString) {
        super(argsString);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        _ap = new ArgParser("com.persistit.Stress9", _args, ARGS_TEMPLATE).strict();
        _splay = _ap.getIntValue("splay");
        _opflags = _ap.getStringValue("op");
        _size = _ap.getIntValue("size");
        _seed = _ap.getIntValue("seed");
        _repeatTotal = _ap.getIntValue("repeat");
        _total = _ap.getIntValue("count");

        try {
            // Exchange with Thread-private Tree
            _ex = getPersistit().getExchange("persistit", _rootName + _threadIndex, true);
            _exs = getPersistit().getExchange("persistit", "shared", true);
        } catch (final Exception ex) {
            handleThrowable(ex);
        }
    }

    @Override
    public void executeTest() {
        final Value value1 = _exs.getValue();
        final Value value2 = new Value(getPersistit());

        setPhase("@");
        try {
            _ex.clear().remove(Key.GTEQ);
            _exs.clear().append("Stress9").append(Key.BEFORE);
            while (_exs.next()) {
                _exs.append(_threadIndex);
                _exs.remove(Key.GTEQ);
                _exs.cut();
                addWork(1);

            }
        } catch (final Exception e) {
            handleThrowable(e);
        }

        for (_repeat = 0; (_repeat < _repeatTotal || isUntilStopped()) && !isStopped(); _repeat++) {

            if (_opflags.indexOf('w') >= 0) {
                setPhase("w");
                _random.setSeed(_seed);
                for (_count = 0; (_count < _total) && !isStopped(); _count++) {
                    final int keyInteger = keyInteger(_count);

                    _exs.clear().append("Stress9").append(keyInteger).append(_threadIndex);
                    setupTestValue(_exs, keyInteger, random(2000, _size));

                    _ex.clear().append(keyInteger);
                    _ex.getValue().put(_exs.getValue().getEncodedSize());

                    try {
                        _exs.fetchAndStore();
                        _ex.store();
                        addWork(2);

                        if (_exs.getValue().isDefined()) {
                            final Object obj = _exs.getValue().get();
                            if ((obj != null) && !(obj instanceof ArrayList)) {
                                throw new RuntimeException("Object should be ArrayList");
                            }
                        }
                    } catch (final Exception e) {
                        handleThrowable(e);

                        break;
                    }
                }
            }

            if (_opflags.indexOf('r') >= 0) {
                setPhase("r");
                _random.setSeed(_seed);
                for (_count = 0; (_count < _total) && !isStopped(); _count++) {
                    final int keyInteger = keyInteger(_count);
                    _exs.clear().append("Stress9").append(keyInteger).append(_threadIndex);
                    setupTestValue(_exs, keyInteger, random(20, _size));
                    _ex.clear().append(keyInteger);
                    try {
                        _ex.fetch();
                        addWork(1);

                        int size1 = 0;
                        if (_ex.getValue().isDefined() && !_ex.getValue().isNull()) {
                            size1 = _ex.getValue().getInt();
                        }
                        _exs.fetch(value2);
                        addWork(1);

                        final int size2 = value2.getEncodedSize();
                        if (size2 != size1) {
                            fail("Value is size " + size2 + ", should be " + size1 + " key="
                                    + _ex.getKey());
                        }
                    } catch (final Exception e) {
                        handleThrowable(e);
                    }
                }
            }

            if (_opflags.indexOf('t') >= 0) {
                setPhase("t");

                _exs.clear().append("Stress9").append(Key.BEFORE);
                int count1 = 0;
                int count2 = 0;
                for (_count = 0; (_count < (_total * 10)) && !isStopped(); _count++) {
                    try {
                        addWork(1);

                        if (!_exs.next()) {
                            break;
                        }
                        if (_exs.append(_threadIndex).fetch().getValue().isDefined()) {
                            count1++;
                        }
                        addWork(1);

                        _exs.cut();
                    } catch (final Exception e) {
                        handleThrowable(e);
                    }
                }

                setPhase("T");
                _ex.clear().append(Key.BEFORE);
                for (_count = 0; (_count < (_total * 10)) && !isStopped(); _count++) {
                    try {
                        addWork(1);

                        if (!_ex.next()) {
                            break;
                        }
                        count2++;
                    } catch (final Exception e) {
                        handleThrowable(e);
                    }
                }
                if (count1 != count2 && !isStopped()) {
                    fail("Traverse count is " + count1 + " but should be " + count2
                            + " on repetition=" + _repeat + " in thread=" + _threadIndex);
                    break;
                }
            }

            if (_opflags.indexOf('d') >= 0) {
                setPhase("d");
                _random.setSeed(_seed);

                for (_count = 0; (_count < _total) && !isStopped(); _count++) {
                    final int keyInteger = keyInteger(_count);
                    _exs.clear().append("Stress9").append(keyInteger).append(_threadIndex);
                    _ex.clear().append(keyInteger);
                    try {
                        _exs.fetchAndRemove();
                        _ex.remove();
                        addWork(2);

                        if (_exs.getValue().isDefined()) {
                            final Object obj = _exs.getValue().get();
                            if ((obj != null) && !(obj instanceof ArrayList)) {
                                throw new RuntimeException("expected an ArrayList");
                            }
                        }
                    } catch (final Exception e) {
                        handleThrowable(e);
                    }
                }
            }

            if ((_opflags.indexOf('h') > 0) && !isStopped()) {
                setPhase("h");
                try {
                    Thread.sleep(random(1000, 5000));
                } catch (final Exception e) {
                }
            }
        }
    }

    int keyInteger(final int counter) {
        final int keyInteger = random(0, _total);
        return keyInteger;
    }

    @Override
    protected void setupTestValue(final Exchange ex, final int counter, final int length) {
        final int elements = length / 8; // ? size of a Integer?
        _testValue.clear();
        final Integer anInteger = new Integer(counter);
        for (int i = 0; i < elements; i++) {
            _testValue.add(anInteger);
        }
        ex.getValue().put(_testValue);
    }
}
