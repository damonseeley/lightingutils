package com.damonseeley.lightingutilsdemo.core;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

public class ClipPlayerGUI extends JFrame implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private ClipPlayer player;
    private JComboBox clipMenu;
    private JButton play;

    public ClipPlayerGUI(ClipPlayer player){

        this.player = player;

        // get the playable global clips (global = no Fixture passed in)
        // and creat combo box
        Collection<Method> clips = player.getGlobalClips(false);
        String[] clipNames = new String[clips.size()];
        int i = 0;
        for (Method clip : clips){
            clipNames[i++] = clip.getName();
        }
        clipMenu = new JComboBox(clipNames);
        clipMenu.setMaximumRowCount(40);

        // play button
        play = new JButton("play");
        play.addActionListener(this);

        this.setLayout(new FlowLayout());
        this.add(clipMenu);
        this.add(play);
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == play){
            player.play((String)clipMenu.getSelectedItem());
        }
    }
}