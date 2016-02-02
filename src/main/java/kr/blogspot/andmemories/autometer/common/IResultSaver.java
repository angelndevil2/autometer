package kr.blogspot.andmemories.autometer.common;

/**
 *
 * sampling result saver interface
 *
 * @author k, Created on 16. 2. 2.
 */
public interface IResultSaver {

    void save(StatisticSample r);

    void close() throws AutoMeterException;
}
