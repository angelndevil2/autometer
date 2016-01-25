package kr.blogspot.andmemories;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

/**
 * @author k, Created on 16. 1. 26.
 */
public class AutoMeter {

    /**
     * JMeter initialization (properties, log levels, locale, etc)
     */
    public static void initJMeter() {

        String base_dir = System.getenv("AUTOMETER_HOME");
        String conf_dir = base_dir+System.getProperty("file.separator")+"conf";
        JMeterUtils.setJMeterHome(base_dir);
        JMeterUtils.loadJMeterProperties(conf_dir+System.getProperty("file.separator")+"jmeter.properties");
        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
        JMeterUtils.initLocale();
    }

    public static void main(String[] args) {

        //JMeter Engine
        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        initJMeter();

        // JMeter Test Plan, basic all u JOrphan HashTree
        HashTree testPlanTree = new HashTree();

        // HTTP Sampler
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8080);
        httpSampler.setPath("/");
        httpSampler.setMethod("GET");

        // Loop Controller
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        loopController.addTestElement(httpSampler);
        loopController.setFirst(true);
        loopController.initialize();

        // Thread Group
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController(loopController);

        // Test Plan
        TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");

        // Construct Test Plan from previously initialized elements
        testPlanTree.add("testPlan", testPlan);
        testPlanTree.add("loopController", loopController);
        testPlanTree.add("threadGroup", threadGroup);
        testPlanTree.add("httpSampler", httpSampler);

        //add Summarizer output to get test progress in stdout like:
        // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }
        // Store execution results into a .jtl file
        String logFile = "example.jtl";
        ResultCollector result_collector = new ResultCollector(summer);
        result_collector.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], result_collector);


        // Run Test Plan
        jmeter.configure(testPlanTree);
        jmeter.run();
    }
}
