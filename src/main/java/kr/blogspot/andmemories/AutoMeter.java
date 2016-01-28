package kr.blogspot.andmemories;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SummaryReport;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>One Thread Group & HttpRequest & local JMeter Tester</h2>
 *
 * <h3>usage</h3>
 * <ol>
 *     <li>set env "AUTOMETER_HOME" or set property autometer.home</li>
 *     <li>{@link #setNumOfThread(int)}</li>
 *     <li>{@link #setRampUpTime(int)}</li>
 *     <li>{@link #setLoopCount(int)}</li>
 *     <li>{@link #addHttpSampler(HTTPSampler)} or {@link #addHttpSampler(String, int, String, String)}</li>
 *     <li>{@link #doTest()}</li>
 * </ol>
 *
 * @author k, Created on 16. 1. 26.
 */
public @Data class AutoMeter {

    private final static String DEFAULT_TEST_PLAN_NAME = "test plan form java code";
    private final static String DEFAULT_THREAD_GROUP_NAME = "default thread group";
    private final static String DEFAULT_LOOP_CONTROLLER_NAME = "default loop controller";
    private final static String DEFAULT_RESULT_COLLECTOR_NAME = "default result collector";

    private JMeterEngine jmeter;
    private final List<Sampler> sampler = new ArrayList<Sampler>();
    @Setter(AccessLevel.NONE)
    private String baseDir;
    @Setter(AccessLevel.NONE)
    private String confDir;
    @Setter(AccessLevel.NONE)
    private String binDir;
    private final HashTree testPlanTree = new HashTree();
    private LoopController loopController;
    private ThreadGroup threadGroup;
    private final TestPlan testPlan = new TestPlan();
    private boolean loopForever;
    private Arguments userDefinedArguments;
    private ResultCollector resultCollector;

    /**
     * ThreadGroup ramp up time in seconds
     */
    private int rampUpTime = 1;
    /**
     * Thread number in ThreadGroup
     */
    private int numOfThread = 1;
    /**
     * {@link LoopController}'s test loop count
     */
    private int loopCount  = 1;
    /**
     * set directory's from env or properties
     */
    private void prepareTest() {
        if (baseDir == null) {
            String baseDir = System.getenv("AUTOMETER_HOME");
            if (baseDir == null) {
                baseDir = System.getProperty("autometer.home");
            }
            if (baseDir == null) baseDir = ".";

            setBaseDir(baseDir);
        }
    }

    /**
     * JMeter initialization (properties, log levels, locale, etc)
     */
    private void initJMeter() {

        JMeterUtils.setJMeterHome(baseDir);
        JMeterUtils.loadJMeterProperties(confDir+ File.separator +"jmeter.properties");
        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
        JMeterUtils.initLocale();
        userDefinedArguments = (Arguments) new ArgumentsPanel().createTestElement();
    }

    /**
     * Loop Controller
     */
    private void initDefaultLoopController() {
        loopController = new LoopController();
        loopController.initialize();
        loopController.setLoops(loopCount);
        loopController.setFirst(true);
        loopController.setName(DEFAULT_LOOP_CONTROLLER_NAME);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
    }

    /**
     * Thread Group
     */
    private void initDefaultThreadGroup() {

        if (threadGroup == null) {
            threadGroup = new ThreadGroup();
            threadGroup.setName(DEFAULT_THREAD_GROUP_NAME);
            threadGroup.setNumThreads(numOfThread);
            threadGroup.setRampUp(rampUpTime);

            if (loopController == null) initDefaultLoopController();
            threadGroup.setSamplerController(loopController);

            threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
            threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        }
    }

    private void initTestPlan() {
        if (testPlan.getName() == null) {
            testPlan.setName(DEFAULT_TEST_PLAN_NAME);
        }
        testPlan.setUserDefinedVariables(userDefinedArguments);
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
    }

    /**
     * set application directories
     */
    public void setBaseDir(@NonNull final String baseDir) {
        this.baseDir = baseDir;
        this.confDir = baseDir + File.separator + "conf";
        this.binDir = baseDir + File.separator + "bin";
    }

    /**
     * add {@link HTTPSampler}
     * @param sampler
     */
    public void addHttpSampler(HTTPSampler sampler) {
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        this.sampler.add(sampler);
    }

    /**
     * add {@link HTTPSampler}
     * @param domain service domain
     * @param port service port
     * @param path content path
     * @param method http request method
     */
    public void addHttpSampler(String domain, int port, String path, String method) {
        HTTPSampler sampler = new HTTPSampler();
        sampler.setDomain(domain);
        sampler.setPort(port);
        sampler.setPath(path);
        sampler.setMethod(method);
        addHttpSampler(sampler);
    }

    public void setTestPlanName(String name) { testPlan.setName(name); }
    /**
     * Construct Test Plan from previously initialized elements
     *
     * <ol>
     *     <li>prepare test</li>
     *     <li>init jmeter</li>
     *     <li>init test plan</li>
     *     <li>init ThreadGroup - init LoopController</li>
     *     <li>init ResultCollector</li>
     *     <li>add user defined arguments</li>
     * </ol>
     *
     * {@link #addHttpSampler(HTTPSampler)} or {@link #addHttpSampler(String, int, String, String)} should be called before this method called
     */
    private void constructTestPlan() {

        prepareTest();

        //JMeter Engine
        setJmeter(new StandardJMeterEngine());

        initJMeter();

        initTestPlan();

        // Thread Group
        initDefaultThreadGroup();

        initDefaultResultCollector();

        // add test plan
        testPlanTree.add(testPlan);

        // add threadGroup under test plan
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(sampler);

        if (loopForever) loopController.setContinueForever(true); // must be latter initThreadGroup

        testPlanTree.add(testPlanTree.getArray()[0], resultCollector);
    }

    private void initDefaultResultCollector() {
        //add Summarizer output to get test progress in stdout like:
        // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
        Summariser summer=null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }
        // Store execution results into a .jtl file
        //String logFile = "example.jtl";
        resultCollector = new ResultCollector(summer);
        resultCollector.setName(DEFAULT_RESULT_COLLECTOR_NAME);
        resultCollector.setProperty(TestElement.TEST_CLASS, ResultCollector.class.getName());
        resultCollector.setProperty(TestElement.GUI_CLASS, SummaryReport.class.getName());
        //resultCollector.setFilename(logFile);
    }

    public void addArgument(String name, String value) {
        userDefinedArguments.addArgument(new Argument(name, value, (String)null));
    }

    public void addArgument(Argument arg) {
        userDefinedArguments.addArgument(arg);
    }

    public void addArgument(String name, String value, String metadata) {
        userDefinedArguments.addArgument(new Argument(name, value, metadata));
    }

    public final void printTestPlanJmx() throws IOException {
        constructTestPlan();

        SaveService.saveTree(testPlanTree, System.out);
    }

    /**
     * <ol>
     *     <li>construct TestPlan</li>
     *     <li>run test</li>
     * </ol>
     */
    public final void doTest() {

        constructTestPlan();

        // Run Test Plan
        getJmeter().configure(testPlanTree);
        ((StandardJMeterEngine)jmeter).run();
    }

    public static void main(String[] args) {

        AutoMeter autoMeter = new AutoMeter();
        autoMeter.doTest();

    }
}
