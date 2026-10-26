package com.crystalwink.crystalscommon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

import com.crystalwink.crystalscommon.LockManager.LockManager;
import com.crystalwink.crystalscommon.Segment.Segment;
import com.crystalwink.crystalscommon.Wire.Wire;

public class RandomSelectionTest {
    @Test
    public void emptyLockManagerReturnsNoLock() {
        assertEquals(-1, new LockManager(1).getRandomLockId());
    }

    @Test
    public void randomLockSelectionStaysWithinOneEntry() {
        LockManager lockManager = new LockManager(1);
        lockManager.updateLock(42, 1);

        for (int attempt = 0; attempt < 100; attempt++) {
            assertEquals(42, lockManager.getRandomLockId());
        }
    }

    @Test
    public void randomWireSelectionStaysWithinOneEntry() {
        Segment segment = new Segment();
        Wire onlyWire = new Wire();
        segment.addWireConnect(onlyWire);

        for (int attempt = 0; attempt < 100; attempt++) {
            assertSame(onlyWire, segment.getRandomWire());
        }
    }
}