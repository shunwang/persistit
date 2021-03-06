/**
 * Copyright © 2012 Akiban Technologies, Inc.  All rights reserved.
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

package com.persistit.stress;

import com.persistit.Persistit;
import com.persistit.Transaction.CommitPolicy;
import com.persistit.stress.unit.Stress1;
import com.persistit.stress.unit.Stress2txn;
import com.persistit.stress.unit.Stress3;
import com.persistit.stress.unit.Stress3txn;
import com.persistit.stress.unit.Stress5;
import com.persistit.stress.unit.Stress6;
import com.persistit.stress.unit.Stress8txn;

public class MixtureTxn1 extends AbstractSuite {

    static String name() {
        return MixtureTxn1.class.getSimpleName();
    }

    public static void main(String[] args) throws Exception {
        new MixtureTxn1(args).runTest();
    }

    public MixtureTxn1(final String[] args) {
        super(name(), args);
    }

    @Override
    public void runTest() throws Exception {

        deleteFiles(substitute("$datapath$/persistit*"));

        add(new Stress1("repeat=10 count=25000"));
        add(new Stress1("repeat=10 count=25000"));
        add(new Stress2txn("repeat=10 count=2500 size=4000 seed=118"));
        add(new Stress2txn("repeat=2 count=25000 seed=119"));
        add(new Stress3("repeat=5 count=25000 seed=119"));
        add(new Stress3txn("repeat=5 count=25000 seed=120"));
        add(new Stress3txn("repeat=5 count=25000"));
        add(new Stress5("repeat=5 count=25000"));
        add(new Stress6("repeat=5 count=1000 size=250"));
        add(new Stress6("repeat=10 count=1000 size=250"));
        add(new Stress8txn("repeat=2 count=1000 size=1000 seed=1"));
        add(new Stress8txn("repeat=2 count=1000 size=1000 seed=2"));
        add(new Stress8txn("repeat=2 count=1000 size=1000 seed=3"));
        add(new Stress8txn("repeat=2 count=1000 size=1000 seed=4"));

        final Persistit persistit = makePersistit(16384, "10000", CommitPolicy.SOFT);

        try {
            execute(persistit);
        } finally {
            persistit.close();
        }
    }
}
