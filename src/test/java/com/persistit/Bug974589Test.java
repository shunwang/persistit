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

package com.persistit;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

import com.persistit.exception.PersistitException;
import com.persistit.unit.PersistitUnitTestCase;
import com.persistit.unit.UnitTestProperties;

public class Bug974589Test extends PersistitUnitTestCase {
    /*
     * https://bugs.launchpad.net/akiban-persistit/+bug/974589
     * IllegalStateException Thrown from BucketSampler.add During analyze table
     * 
     * Distilled: database is left with row count Accumulator holding a value of
     * 0 when there is actually a row.
     */

    private static final String TREE_NAME = "Bug974589Test";

    private static Exchange getExchange(Persistit persistit) throws PersistitException {
        return persistit.getExchange(UnitTestProperties.VOLUME_NAME, TREE_NAME, true);
    }

    @Test
    public void testBug980292() throws Exception {
        for (int loop = 1; loop < 5; loop++) {
            _persistit.checkpoint();
            Exchange ex1 = getExchange(_persistit);
            Transaction txn1 = ex1.getTransaction();
            /*
             * Touch an Accumulator, then immediately checkpoint.
             */
            txn1.begin();
            Accumulator acc1 = ex1.getTree().getAccumulator(Accumulator.Type.SUM, 1);
            acc1.update(1, txn1);
            txn1.commit();
            txn1.end();
            ex1 = null;
            txn1 = null;
            acc1 = null;
            _persistit.checkpoint();
            _persistit.close();

            Properties properties = _persistit.getProperties();
            _persistit = new Persistit();
            _persistit.initialize(properties);

            Exchange ex2 = getExchange(_persistit);
            Transaction txn2 = ex2.getTransaction();
            txn2.begin();
            Accumulator acc2 = ex2.getTree().getAccumulator(Accumulator.Type.SUM, 1);
            assertEquals(loop, acc2.getSnapshotValue(txn2));
            txn2.commit();
            txn2.end();
            System.out.printf("Iteration %,d completed\n", loop);
            ex2 = null;
            txn2 = null;
            acc2 = null;
        }
    }
}
