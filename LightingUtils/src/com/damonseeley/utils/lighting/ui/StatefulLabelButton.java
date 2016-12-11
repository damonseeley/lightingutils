package com.damonseeley.utils.lighting.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;

/**
 * NEEDS SERIOUS WORK.
 * @author bradley
 *
 */
public class StatefulLabelButton extends JButton implements ActionListener{

    private static final long serialVersionUID = 9109315371948848914L;
    private String onLabel, offLabel;
    private boolean isOn;
    private List <ButtonStateListener>listeners;

    public StatefulLabelButton(String offLabel, String onLabel){

        this.isOn = false;
        this.onLabel = onLabel;
        this.offLabel = offLabel;
        this.addActionListener(this);
        this.configureListeners();

        setState(isOn);
        setText();
    }

    public boolean isOn(){
        return isOn;
    }

    public void addListener(ButtonStateListener listener){
        listeners.add(listener);
    }

    public void removeListener(ButtonStateListener listener){
        listeners.remove(listener);
    }

    private void configureListeners(){
        listeners = new Vector<ButtonStateListener>();
    }

    private void notifyListeners(){
        for (ButtonStateListener l : listeners){
            l.buttonStateChanged(this);
        }
    }

    // TODO: this won't work.  Might fire before an event who is listening on us.
    // need to understand action event model and refactor.
    public void actionPerformed(ActionEvent e) {
        isOn = !isOn;
        setText();
        notifyListeners();
    }

    public void setText(){
        super.setText(isOn ? onLabel : offLabel);
    }

    public void setState(boolean isOn){
        this.isOn = isOn;
        setText();
    }

    final public void setText(String text){
        throw new RuntimeException("Attempt to change label from outside StatefulLabelButton");
    }
}