package kr.blogspot.andmemories;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;

/**
 * One Thread Group & HttpRequest & local JMeter Tester
 *
 * @author k, Created on 16. 1. 26.
 */
public @Data class AutoMeter {

    private JMeterEngine jmeter;
    private Sampler sampler;
    @Setter(AccessLevel.NONE)
    private String baseDir;
    @Setter(AccessLevel.NONE)
    private String confDir;
    @Setter(AccessLevel.NONE)
    private String binDir;
    private HashTree testPlanTree;
    private LoopController loopController;
    private ThreadGroup threadGroup;
    private TestPlan testPlan;
    /**
     * ThreadGroup ramp up time in seconds
     */
    private int rampUpTime = 1;
    /**
     * Thread number in ThreadGroup
     */
    private int numOfThread = 1;

    /**
     * JMeter initialization (properties, log levels, locale, etc)
     */
    private void initJMeter() {

        JMeterUtils.setJMeterHome(baseDir);
        JMeterUtils.loadJMeterProperties(confDir+ File.separator +"jmeter.properties");
        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
        JMeterUtils.initLocale();

    }

    /**
     * HTTP Sampler
     */
    private void initHttpSampler() {
        HTTPSampler sampler = new HTTPSampler();
        sampler.setDomain("localhost");
        sampler.setPort(8080);
        sampler.setPath("/");
        sampler.setMethod("GET");
        setSampler(sampler);
    }

    /**
     * Loop Controller
     */
    private void initLoopController() {
        loopController = new LoopController();
        loopController.setLoops(1);
        loopController.addTestElement(getSampler());
        loopController.setFirst(true);
        loopController.initialize();
    }

    /**
     * Thread Group
     */
    private void initThreadGroup() {
        threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(getNumOfThread());
        threadGroup.setRampUp(getRampUpTime());
        threadGroup.setSamplerController(getLoopController());
    }

    /**
     * set application directories
     */
    public void setDirs(@NonNull final String baseDir) {
        this.baseDir = baseDir;
        this.confDir = baseDir + File.separator + "conf";
        this.binDir = baseDir + File.separator + "bin";
    }

    public final void doTest() {
        //JMeter Engine
        setJmeter(new StandardJMeterEngine());

        initJMeter();
        initHttpSampler();

        // JMeter Test Plan, basic all u JOrphan HashTree
        setTestPlanTree(new HashTree());


        // Loop Controller
        initLoopController();

        // Thread Group
        initThreadGroup();

        // Test Plan
        setTestPlan(new TestPlan("Create JMeter Script From Java Code"));

        // Construct Test Plan from previously initialized elements
        getTestPlanTree().add("testPlan", getTestPlan());
        getTestPlanTree().add("loopController", getLoopController());
        getTestPlanTree().add("httpSampler", getSampler());
        getTestPlanTree().add("threadGroup", getThreadGroup());

        //add Summarizer output to get test progress in stdout like:
        // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
        Summariser summer=null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }
        // Store execution results into a .jtl file
        String logFile = "example.jtl";
        ResultCollector result_collector = new ResultCollector(summer);
        result_collector.setFilename(logFile);
        getTestPlanTree().add(getTestPlanTree().getArray()[0], result_collector);
        //getTestPlanTree().add("httpSampler", result_collector);

        // Run Test Plan
        getJmeter().configure(getTestPlanTree());
        ((StandardJMeterEngine)getJmeter()).run();
    }

    public static void main(String[] args) {

        AutoMeter autoMeter = new AutoMeter();

        String baseDir = System.getenv("AUTOMETER_HOME");
        if (baseDir == null) {
            baseDir = System.getProperty("autometer.home");
        }
        if (baseDir == null) baseDir = ".";

        autoMeter.setDirs(baseDir);

        autoMeter.doTest();

    }
}
