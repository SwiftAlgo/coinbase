package coinbase;

import java.util.Arrays;

import coinbase.decoder.L2Snapshot;
import coinbase.decoder.L2Update;

public class OrderBookSide {

    private final boolean bid;
    /*
     * Parallel arrays. TODO Consider circular arrays to accommodate price moves
     * without array shifting.
     */
    private final double[] prices;
    private final double[] sizes;

    private int levelCount;

    public OrderBookSide(boolean buy, int levels) {
        this.bid = buy;
        prices = new double[levels];
        sizes = new double[levels];
    }

    public boolean isBid() {
        return bid;
    }

    public boolean isAsk() {
        return !bid;
    }

    /**
     * Number of levels for which data is stored. This allows sufficient data to be
     * retained for the displayed levels in case quotes are pulled.
     * 
     * @return
     */
    public int levels() {
        return prices.length;
    }

    public int currentLevels() {
        return levelCount;
    }

    public void onL2Snapshot(L2Snapshot l2Snapshot) {
        if (levelCount > 0) {
            Arrays.fill(prices, 0.0);
            Arrays.fill(sizes, 0.0);
            levelCount = 0;
        }
        if (bid) {
            l2Snapshot.copyBids(prices, sizes);
            levelCount = l2Snapshot.bidCount();
        } else {
            l2Snapshot.copyAsks(prices, sizes);
            levelCount = l2Snapshot.askCount();
        }
        assert invariants();
    }

    public boolean onL2Update(L2Update l2Update) {
        double[] priceUpdates = bid ? l2Update.bidPrices() : l2Update.askPrices();
        double[] sizeUpdates = bid ? l2Update.bidSizes() : l2Update.askSizes();
        if (priceUpdates == null) {
            // Update for the other side only.
            return false;
        }
        boolean changed = false;
        for (int updateIndex = 0; updateIndex < priceUpdates.length; ++updateIndex) {
            double newPrice = priceUpdates[updateIndex];
            double newSize = sizeUpdates[updateIndex];
            int foundIndex = bid? binarySearchDescending(prices, newPrice) : Arrays.binarySearch(prices, newPrice);
            if (foundIndex >= 0) {
                // Level was already in the order book.
                if (newSize > 0.0) {
                    sizes[foundIndex] = newSize;
                } else {
                    shiftAggressively(foundIndex);
                    --levelCount;
                }
                assert invariants();
                changed = true;
            } else {
                int insertionPoint = -(foundIndex + 1);
                if (insertionPoint < prices.length && newSize > 0.0) {
                    // Shift more passive levels and Insert new one within the book.
                    shiftPassively(insertionPoint);
                    prices[insertionPoint] = newPrice;
                    sizes[insertionPoint] = newSize;
                    levelCount = Math.min(levelCount + 1, prices.length);
                    assert invariants();
                    changed = true;
                }
            }
        }
        return changed;
    }

    private void shiftPassively(int fromIndexInclusive) {
        for (int shiftIndex = Math.min(levelCount - 1,
                prices.length - 2); shiftIndex >= fromIndexInclusive; --shiftIndex) {
            prices[shiftIndex + 1] = prices[shiftIndex];
            sizes[shiftIndex + 1] = sizes[shiftIndex];
        }
    }

    /**
     * Shift more passive prices more aggressively to remove a level.
     * 
     * @param fromIndexExclusive First index that will be overwritten but not
     *                           shifted.
     */
    private void shiftAggressively(int fromIndexExclusive) {
        for (int shiftIndex = fromIndexExclusive; shiftIndex < levelCount - 1; ++shiftIndex) {
            prices[shiftIndex] = prices[shiftIndex + 1];
            sizes[shiftIndex] = sizes[shiftIndex + 1];
        }
        prices[levelCount - 1] = 0.0;
        sizes[levelCount - 1] = 0.0;
    }

    private boolean invariants() {
        assert levelCount >= 0;
        assert levelCount <= prices.length;
        assert prices.length == sizes.length;
        for (int i = 0; i < levelCount; ++i) {
            assert prices[i] > 0.0;
            assert sizes[i] > 0.0;
            // Strictly decreasing price aggressiveness.
            if (i < levelCount - 1) {
                if (bid) {
                    assert prices[i] > prices[i + 1];
                } else {
                    assert prices[i] < prices[i + 1];
                }
            }
        }
        // Check empty levels are in fact empty.
        for (int i = levelCount; i < prices.length; ++i) {
            assert prices[i] == 0.0;
            assert sizes[i] == 0.0;
        }
        return true;
    }

    public double priceAtIndex(int index) {
        if (index > levelCount - 1) {
            throw new IndexOutOfBoundsException(
                    String.format("Index %d out of bounds for number of levels %d", index, levelCount));
        }
        return prices[index];
    }

    public double sizeAtIndex(int index) {
        if (index > levelCount - 1) {
            throw new IndexOutOfBoundsException(
                    String.format("Index %d out of bounds for number of levels %d", index, levelCount));
        }
        return sizes[index];
    }

    
    // TODO Find a lib to replace following code to binary search array sorted in descending order.
    // Code below adapted from java.util.Arrays by inverting the comparisons against the key (double values).
    // eg. <= changed to  >= etc.
    
    /**
     * Searches the specified array of doubles for the specified value using
     * the binary search algorithm.  The array must be in reverse sorted
     * (descending) order prior to making this call.
     * If it is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.  This method considers all NaN values to be
     * equivalent and equal.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    public static int binarySearchDescending(double[] a, double key) {
        return binarySearch0Descending(a, 0, a.length, key);
    }

// Like public version, but without range checks.
    private static int binarySearch0Descending(double[] a, int fromIndex, int toIndex, double key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midVal = a[mid];

            if (midVal > key) // Gordon: java.util.Arrays is <
                low = mid + 1; // Neither val is NaN, thisVal is smaller
            else if (midVal < key) // Gordon: java.util.Arrays is >
                high = mid - 1; // Neither val is NaN, thisVal is larger
            else {
                long midBits = Double.doubleToLongBits(midVal);
                long keyBits = Double.doubleToLongBits(key);
                if (midBits == keyBits) // Values are equal
                    return mid; // Key found
                else if (midBits > keyBits) // (-0.0, 0.0) or (!NaN, NaN) // Gordon: java.util.Arrays is <
                    low = mid + 1;
                else // (0.0, -0.0) or (NaN, !NaN)
                    high = mid - 1;
            }
        }
        return -(low + 1); // key not found.
    }

}
