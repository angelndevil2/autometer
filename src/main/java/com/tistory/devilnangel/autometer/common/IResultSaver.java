package com.tistory.devilnangel.autometer.common;

/**
 *
 * sampling result saver interface
 *
 * @author k, Created on 16. 2. 2.
 */
public interface IResultSaver {

    void save(StatisticSample r);

    void writeHeader(String header);

    void close() throws AutoMeterException;
}
