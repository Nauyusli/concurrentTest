package tech.nauyus.list;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 比较Vector，Collections.synchronizedList，CopyOnWriteArrayList读操作，写操作，遍历操作性能
 *
 * @author nauyus
 * @date 2020年01月29日
 */
public class ListPerformanceTest {

    /**
     * 并发数
     */
    public final static int THREAD_COUNT = 64;
    /**
     * list大小
     */
    public final static int SIZE = 10000;

    /**
     * 测试读性能
     *
     * @throws Exception
     */
    @Test
    public void testGet() throws Exception {
        List<Integer> list = initList();
        List<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>(list);
        List<Integer> synchronizedList = Collections.synchronizedList(list);
        Vector vector = new Vector(list);

        int copyOnWriteArrayListTime = 0;
        int synchronizedListTime = 0;
        int vectorTime = 0;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            copyOnWriteArrayListTime += executor.submit(new GetTestTask(copyOnWriteArrayList, countDownLatch)).get();
        }
        System.out.println("CopyOnWriteArrayList get method cost time is " + copyOnWriteArrayListTime);

        for (int i = 0; i < THREAD_COUNT; i++) {
            synchronizedListTime += executor.submit(new GetTestTask(synchronizedList, countDownLatch)).get();
        }
        System.out.println("Collections.synchronizedList get method cost time is " + synchronizedListTime);

        for (int i = 0; i < THREAD_COUNT; i++) {
            vectorTime += executor.submit(new GetTestTask(vector, countDownLatch)).get();
        }
        System.out.println("vector get method cost time is " + vectorTime);
    }

    /**
     * 测试写性能
     *
     * @throws Exception
     */
    @Test
    public void testAdd() throws Exception {
        List<Integer> list = initList();
        List<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>(list);
        List<Integer> synchronizedList = Collections.synchronizedList(list);
        Vector vector = new Vector(list);

        int copyOnWriteArrayListTime = 0;
        int synchronizedListTime = 0;
        int vectorTime = 0;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            copyOnWriteArrayListTime += executor.submit(new AddTestTask(copyOnWriteArrayList, countDownLatch)).get();
        }
        System.out.println("CopyOnWriteArrayList add method cost time is " + copyOnWriteArrayListTime);

        for (int i = 0; i < THREAD_COUNT; i++) {
            synchronizedListTime += executor.submit(new AddTestTask(synchronizedList, countDownLatch)).get();
        }
        System.out.println("Collections.synchronizedList add method cost time is " + synchronizedListTime);

        for (int i = 0; i < THREAD_COUNT; i++) {
            vectorTime += executor.submit(new AddTestTask(vector, countDownLatch)).get();
        }
        System.out.println("vector add method cost time is " + vectorTime);
    }

    /**
     * 测试遍历性能
     *
     * @throws Exception
     */
    @Test
    public void testIterator() throws Exception {
        List<Integer> list = initList();
        List<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>(list);
        List<Integer> synchronizedList = Collections.synchronizedList(list);
        Vector vector = new Vector(list);

        int copyOnWriteArrayListTime = 0;
        int synchronizedListTime = 0;
        int vectorTime = 0;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            copyOnWriteArrayListTime += executor.submit(new IteratorTestTask(copyOnWriteArrayList, countDownLatch, false)).get();
        }
        System.out.println("CopyOnWriteArrayList iterator method cost time is " + copyOnWriteArrayListTime);

        for (int i = 0; i < THREAD_COUNT; i++) {
            synchronizedListTime += executor.submit(new IteratorTestTask(synchronizedList, countDownLatch, true)).get();
        }
        System.out.println("Collections.synchronizedList iterator method cost time is " + synchronizedListTime);

        for (int i = 0; i < THREAD_COUNT; i++) {
            vectorTime += executor.submit(new IteratorTestTask(vector, countDownLatch, false)).get();
        }
        System.out.println("vector iterator method cost time is " + vectorTime);
    }


    private List<Integer> initList() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < SIZE; i++) {
            list.add(new Random().nextInt(1000));
        }
        return list;
    }

    class GetTestTask implements Callable<Integer> {
        List<Integer> list;
        CountDownLatch countDownLatch;

        GetTestTask(List<Integer> list, CountDownLatch countDownLatch) {
            this.list = list;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public Integer call() {
            int pos = new Random().nextInt(SIZE);
            long start = System.currentTimeMillis();
            for (int i = 0; i < SIZE; i++) {
                list.get(pos);
            }
            long end = System.currentTimeMillis();
            countDownLatch.countDown();
            return (int) (end - start);
        }
    }

    class AddTestTask implements Callable<Integer> {
        List<Integer> list;
        CountDownLatch countDownLatch;

        AddTestTask(List<Integer> list, CountDownLatch countDownLatch) {
            this.list = list;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public Integer call() {
            int pos = new Random().nextInt(SIZE);
            long start = System.currentTimeMillis();
            for (int i = 0; i < SIZE; i++) {
                list.add(pos);
            }
            long end = System.currentTimeMillis();
            countDownLatch.countDown();
            return (int) (end - start);
        }
    }

    class IteratorTestTask implements Callable<Integer> {
        List<Integer> list;
        CountDownLatch countDownLatch;
        boolean isSynchronizedList;

        IteratorTestTask(List<Integer> list, CountDownLatch countDownLatch, Boolean isSynchronizedList) {
            this.list = list;
            this.countDownLatch = countDownLatch;
            this.isSynchronizedList = isSynchronizedList;
        }

        @Override
        public Integer call() {
            long start = System.currentTimeMillis();
            if (isSynchronizedList) {
                //Collections.synchronizedList() 未对遍历操作加锁，故遍历时需要手动加锁
                synchronized (list) {
                    iterator();
                }
            } else {
                iterator();
            }

            long end = System.currentTimeMillis();
            countDownLatch.countDown();
            return (int) (end - start);
        }

        private void iterator() {
            for (int j = 0; j < SIZE; j++) {
                Iterator i = list.iterator();
                while (i.hasNext()) {
                    i.next().hashCode();
                }
            }
        }
    }


}
