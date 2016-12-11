package net.electroland.utils.process.io;

import java.util.Date;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.electroland.utils.process.MonitoredProcess;
import net.electroland.utils.process.TemplatedDateTimeScheduler;

@SuppressWarnings("serial")
public class ProcessList extends JFrame implements Runnable{

    JTable processList;
    Map <String, MonitoredProcess> processes;
    TemplatedDateTimeScheduler scheduler;
    NoEditModel model;

    public ProcessList(Map <String, MonitoredProcess> processes, TemplatedDateTimeScheduler scheduler){

        this.processes = processes;
        this.scheduler = scheduler;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // TBD: add restart tasks (requires a lot of extra work)
        // TBD: add "kill" buttons per process.
        model = new NoEditModel(getProcessTable(), 
                    new Object[]{"type","name","last started"});
        processList = new JTable(model); 
        processList.setRowHeight(32);
        this.add(new JScrollPane(processList));
        this.setTitle("Process List");
        this.pack();
        this.setVisible( true );
        (new Thread(this)).start();
    }

    public Object[][] getProcessTable() {
        Object[][] list = new Object[processes.size()][3];
        int i = 0;
        for (Object name : processes.keySet()){
            list[i][0] = "process";
            list[i][1] = name;
            list[i][2] = null;
            i++;
        }
        return list;
    }


    @Override
    public void run() {
        while(true){
            try {

                Thread.sleep(2000);

                // update last start times
                for (int row = 0; row < model.getRowCount(); row++){
                    Object name = model.getValueAt(row, 1);
                    Date lastStart = processes.get(name).getStartTime();
                    model.setValueAt(lastStart == null ? "NOT RUNNING" : lastStart, row, 2);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

@SuppressWarnings("serial")
class NoEditModel extends DefaultTableModel{
    public NoEditModel(Object[][] data, Object[] headers){
        super(data, headers);
    }
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}