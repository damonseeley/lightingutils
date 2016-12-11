package com.damonseeley.utils.lighting.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.damonseeley.utils.lighting.ELUManager;
import com.damonseeley.utils.lighting.TestSuite;
import com.damonseeley.utils.lighting.TestSuiteCompletionListener;

public class ELUControls extends JPanel implements ActionListener, ButtonStateListener, TestSuiteCompletionListener {

    private static final long serialVersionUID = -1589987975805310686L;
    private ELUManager lightingManager;
    private StatefulLabelButton startStop;
    private JButton allOn, allOff, runTest;
    private JComboBox tests;

    public ELUControls(ELUManager lightingManager){

        this.lightingManager = lightingManager;

        this.configureControls();
        this.layoutControls();
        for (TestSuite ts : lightingManager.getTestSuites()){
            ts.addCompletionListener(this);
        }
        if (lightingManager.isAutostarted()){
            runTest.setEnabled(false);
            startStop.setState(true);
        }
    }

    public void layoutControls(){
        this.add(new JLabel("ELU:"));
        this.add(startStop);
        this.add(allOn);
        this.add(allOff);
        this.add(new JLabel("|"));
        this.add(tests);
        this.add(runTest);
    }

    public void configureControls(){

        startStop = new StatefulLabelButton("start", "stop");
        allOn     = new JButton("all on");
        allOff    = new JButton("all off");
        runTest   = new JButton("run test");
        tests     = new JComboBox(lightingManager.getTestSuiteNames());

        allOn.addActionListener(this);
        allOff.addActionListener(this);
        runTest.addActionListener(this);
        startStop.addListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == allOn){
            lightingManager.allOn();
        } else if (e.getSource() == allOff) {
            lightingManager.allOff();
        } else if (e.getSource() == runTest) {
            if (!startStop.isOn()){
                startStop.setEnabled(false);
                runTest.setEnabled(false);
                lightingManager.runTest(tests.getSelectedItem().toString());
            }
        }
    }

    @Override
    public void buttonStateChanged(StatefulLabelButton button) {
        if (button == startStop && button.isOn()) {
            start();
        } else {
            stop();
        }
    }

    public void start(){
        runTest.setEnabled(false);
        lightingManager.start();
    }
    public void stop(){
        runTest.setEnabled(true);
        lightingManager.stop();
    }

    @Override
    public void testingComplete() {
        startStop.setEnabled(true);
        runTest.setEnabled(true);
    }
}