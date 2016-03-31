/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.crystalwink.crystalscommon.LockManager;



/**
 * Lock Manager used to optimize the selection of locked tiles
 * By adding each locked tileId to the LockManager, the action of getting
 * a random locked TileId is much quicker that randomly selecting tiles and
 * rejecting them if not locked.
 *
 *
 * @author Dean Clark deancl
 */
public class LockManager {

    /**
     *
     * @param maxLocks
     */
    public LockManager(int maxLocks) {
        lockLimitUserDefined = maxLocks;
        locks = new int[MAX_LOCKS][LOCK_PARAMETERS];
    }

    /**
     *
     * @param maxLocks
     */
    public void setMaxLocksAllowed(int maxLocks) {
        lockLimitUserDefined = maxLocks;
        System.out.printf("LockManager()->setMaxLocksAllowed() %d \n", lockLimitUserDefined);
    }

    /**
     *
     * @return lockLimitUserDefined
     */
    public int getMaxLocksAllowed() {
        return lockLimitUserDefined;
    }

    /**
     *
     * @return numberOfLocks
     */
    public int getLocksInUse() {
        return numberOfLocks;
    }

    /**
     *
     * @param lockIndex
     * @return lockId
     */
    public int getLockId(int lockIndex) {
        return locks[lockIndex][LOCK_ID];
    }

    /**
     *
     * @param lockId
     * @return lockedState
     */
    public boolean isLocked(int lockId) {
        int lockIndex = getPrivateLockIndex(lockId);

        return (lockIndex > 0) ? true : false;
    }

    /**
     *
     * @param lockId
     * @param excludingSemaphoreInc
     * @return lockState
     */
    public boolean isLocked(int lockId, int excludingSemaphoreInc) {
        int lockIndex = getPrivateLockIndex(lockId);

        if ((lockIndex > 0) && (locks[lockIndex][LOCK_SEMAPHORE] == excludingSemaphoreInc)) {
            return false;   // maybe locked but excluded
        }
        return isLocked(lockId);
    }

    private int addLock(int lockId, int lockSemaphoreValue) {
        if (numberOfLocks < lockLimitUserDefined) {
            locks[numberOfLocks][LOCK_ID] = lockId;
            locks[numberOfLocks][LOCK_SEMAPHORE] = lockSemaphoreValue;
            ++numberOfLocks;
        } else {
            System.out.printf("LockManager() - Exceeded User Defined Semaphore number\n");
        }

        return numberOfLocks;
    }

    // find and remove a lock by shifting the last lock to the location of the lock to be removed.
    /**
     *
     * @param lockId
     * @return numberOfLocks
     */
    public int removeLock(int lockId) {
        for (int eachLock = 0; eachLock < numberOfLocks; eachLock++) {
            if (locks[eachLock][LOCK_ID] == lockId) {
                locks[eachLock][LOCK_ID] = locks[numberOfLocks - 1][LOCK_ID];
                locks[eachLock][LOCK_SEMAPHORE] = locks[numberOfLocks - 1][LOCK_SEMAPHORE];
                --numberOfLocks;
            }
        }
        return numberOfLocks;
    }

    /**
     * @return real Id
     */
    public int getRandomLockId() {
        int aLock = (int) Math.round((numberOfLocks) * Math.random());
        //System.out.printf("LockManager()->getRandomLockId() - Random LockIndex %d\n", aLock);
        return locks[aLock][LOCK_ID];
    }

    /**
     *
     * @param lockId
     * @return private lock index
     */
    private int getPrivateLockIndex(int lockId) {
        for (int eachLock = 0; eachLock < numberOfLocks; eachLock++) {
            if (locks[eachLock][LOCK_ID] == lockId) {
                return eachLock;
            }
        }
        return -1;
    }

    /**
     *
     * @param lockId
     * @param lockSemaphoreValue
     * @return lockIndex
     */
    public int updateLock(int lockId, int lockSemaphoreValue) {
        int lockIndex = getPrivateLockIndex(lockId);

        // if lock does not exist, create one
        if (lockIndex == -1) {
            lockIndex = addLock(lockId, lockSemaphoreValue);
        } else // if a lock exists, update semaphore value
        {
            locks[lockIndex][LOCK_SEMAPHORE] = lockSemaphoreValue;
        }

        return lockIndex;
    }

    /**
     *
     * @param lockId
     * @return lockLevel
     */
    public int getLockLevel(int lockId) {
        int lockLevel = 0;
        int lockIndex = getPrivateLockIndex(lockId);

        if (lockIndex >= 0) {
            lockLevel = locks[lockIndex][LOCK_SEMAPHORE];
        }

        return lockLevel;
    }

    /**
     *
     * @param lockId
     * @param inncrement
     * @return lockIndex
     */
    public int increaseLock(int lockId, int inncrement) {
        int lockIndex = getPrivateLockIndex(lockId);

        // if lock does not exist, create one
        if (lockIndex == -1) {
            lockIndex = addLock(lockId, inncrement);
        }

        // if a lock exists, update semaphore value
        if (lockIndex != -1) {
            locks[lockIndex][LOCK_SEMAPHORE] += inncrement;
        } else {
            return -1;
        }

        return lockIndex;
    }

    /**
     *
     * @param lockId
     * @param decrement
     * @return lockLevel
     */
    public int decreaseLock(int lockId, int decrement) {
        int lockIndex = getPrivateLockIndex(lockId);

        // if lock exists, decrement
        if (lockIndex != -1) {
            locks[lockIndex][LOCK_SEMAPHORE] -= decrement;
        }

        // delete lock if semaphore released (i.e <= 0)
        if (locks[lockIndex][LOCK_SEMAPHORE] <= 0) {
            removeLock(lockId);
        } else {
            return locks[lockIndex][LOCK_SEMAPHORE];
        }

        return -1;  // lock deleted or does not exist
    }
    final int MAX_LOCKS = 256;
    final int LOCK_PARAMETERS = 2;
    final int LOCK_ID = 0;
    final int LOCK_SEMAPHORE = 1;
    private int numberOfLocks;
    private int locks[][];
    private int lockLimitUserDefined;
}
