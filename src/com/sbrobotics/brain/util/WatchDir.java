package com.sbrobotics.brain.util;

/**
 * Created by KaDon on 3/15/2017.
 */
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDir implements AutoCloseable {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private boolean trace = false;

    private boolean opened = true;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }


    /**
     * Creates a WatchService and registers the given directory
     */
    public WatchDir(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        register(dir);

        // enable trace after initial registration
        this.trace = false;
        timer.scheduleAtFixedRate(new QueueWorker(), 1000, 1000);
    }


    /**
     * Object to synchronize on when manipulating
     * any of the file queues
     */
    private final Object queueSync= new Object();
    /**
     * File queues
     */
    private final List<Row> dataQueue= Collections.synchronizedList(new LinkedList<Row>());
    private final Timer timer= new Timer();
    private Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
    private class QueueWorker extends TimerTask {
        @Override
        public void run(){
            synchronized(queueSync) {
                int i = 0;
                Row averageRow = new Row();
                averageRow.data = new Double[11];
                while (i < 20) {
                    if (!dataQueue.isEmpty()) {
                        Row row = dataQueue.remove(0);
                        if (averageRow.timeStamp > 0) {
                            averageRow.timeStamp = (row.timeStamp + averageRow.timeStamp) / 2;
                            for (int j = 0; j < row.data.length; j++) {
                                if (averageRow.data.length > j) {
                                    averageRow.data[j] =  (averageRow.data[j]  + row.data[j])/2;
                                } else {
                                    averageRow.data[j] = row.data[j];
                                }

                            }
                            averageRow.data = row.data;
                        }  else {
                            averageRow.timeStamp = row.timeStamp;
                            averageRow.data = row.data;
                        }
                    }

                    i++;
                }
                if (averageRow.timeStamp > 0) {
                    Date date = new Date(averageRow.timeStamp);
                    String avDataString = "";
                    for (int j = 0; j < averageRow.data.length; j++) {
                        avDataString += averageRow.data[j].toString() + ", ";
                    }

                    System.out.println(format.format(date) + ", " + averageRow.timeStamp  + ", " + avDataString);
                }
            }
        }
    }

    /**
     * Process all events for keys queued to the watcher
     */

    long fileLength = 0;
    public void processEvents() throws SAXException, TransformerException, ParserConfigurationException, IOException {
        LinkedList<Row> dataQ= new LinkedList<Row>();
        while (opened) {
            dataQ.clear();
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized at all!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);


               /*
                * it is possible for multiple events to fire for a single action.
                * For example:
                * A create and modify when a file is created (file entry added and then file stats set)
                * Multiple modify events (file length set and then data written; multiple file states updated individually)
                */
                if(kind == StandardWatchEventKinds.ENTRY_MODIFY){
                    File file = child.toFile();
                    readFile(file);
                    while(true){
                        if(fileLength < file.length()){
                            readFile(file);
                        }
                    }

                }

            }
            synchronized(queueSync){
                dataQueue.addAll(dataQ);
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    timer.cancel();
                    break;
                }
            }
        }
    }


    public synchronized void readFile(File file) throws IOException {
        String line = null;
        // 0,67,44,593634,40191,7155,5066,5082,5325,7873,7402
        BufferedReader in = new BufferedReader(new java.io.FileReader(file));
        in.skip(fileLength);
        while((line = in.readLine()) != null) {
            String[] split = null;
            if (line.contains(",")) {
                split = line.split(",");
                if (split.length == 11) {
                    Double[] response = new Double[split.length];
                    for (int i = 0; i < split.length; i++) {
                        response[i] = Double.valueOf(split[i]);
                    }
                    Row row = new Row();
                    row.timeStamp = System.currentTimeMillis();
                    row.data = response;
                    dataQueue.add(row);
                }

            } else {
                split = new String[1];
                split[0] = line;
            }
            System.out.println(line);
        }
        in.close();
        fileLength = file.length();
    }


    public static void main(String[] args) throws IOException, TransformerException, SAXException, ParserConfigurationException, InterruptedException {


        // register directory and process its events
        Path dir = Paths.get("C:\\Projects\\Brain\\kitschpatrol-Brain-6c69266\\BrainGrapher");
        new WatchDir(dir).processEvents();
        //

    }

    @Override
    public void close() throws IOException {
        opened = false;
    }
}
