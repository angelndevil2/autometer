package kr.blogspot.andmemories.common;

import lombok.Data;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author k, Created on 16. 1. 31.
 */
@Data
public abstract class AbstractQ {

    public static final int EQ_SIZE = 10000;
    private final ArrayBlockingQueue<Object> queue;

    public AbstractQ() { this(EQ_SIZE); }
    public AbstractQ(int size) {
        queue = new ArrayBlockingQueue<Object>(size);
    }

    public Object take() throws InterruptedException { return queue.take(); }
    public boolean offer(Object sr) { return queue.offer(sr); }
    public void clear() { queue.clear(); }
}
