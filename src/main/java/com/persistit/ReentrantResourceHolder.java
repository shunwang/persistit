/**
 * Copyright © 2011-2012 Akiban Technologies, Inc.  All rights reserved.
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

import com.persistit.exception.PersistitInterruptedException;
import com.persistit.util.Debug;

/**
 * A container for a SharedResource that experiences reentrant claims (locks).
 * {@link Exchange} uses this to hold a Tree object, which experiences this
 * pattern. The purpose is to keep a count of so that when an Exchange has
 * finished using the Tree, it can verify that all claims were released. This is
 * primarily for debugging; correct code will always release each claim.
 */

class ReentrantResourceHolder {

    private final SharedResource _resource;
    private int _claimCount;

    ReentrantResourceHolder(final SharedResource r) {
        _resource = r;
    }

    void verifyReleased() {
        Debug.$assert0.t(_claimCount == 0);
        _claimCount = 0;
    }

    boolean claim(final boolean writer) throws PersistitInterruptedException {
        return claim(writer, SharedResource.DEFAULT_MAX_WAIT_TIME);
    }

    boolean claim(final boolean writer, final long timeout) throws PersistitInterruptedException {
        if (_claimCount == 0) {
            if (!_resource.claim(writer, timeout)) {
                return false;
            } else {
                _claimCount++;
                return true;
            }
        } else {
            if (writer && !_resource.isWriter()) {
                if (!_resource.upgradeClaim()) {
                    return false;
                }
            }
            _claimCount++;
            return true;
        }
    }

    void release() {
        if (_claimCount <= 0) {
            throw new IllegalStateException("This thread holds no claims");
        }
        if (--_claimCount == 0) {
            _resource.release();
        }
    }

    boolean upgradeClaim() {
        return _resource.upgradeClaim();
    }
}
