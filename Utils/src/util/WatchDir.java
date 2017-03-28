package util;


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
     * Filandansdjaijioasdiasidmaosfmaoifmaoismfoiasmfoiamfoiamsfoie queues
     */
    private final List<Row> dataQueue= Collections.synchronizedList(new LinkedList<Row>());
    private final List<Row> commandQueue= Collections.synchronizedList(new LinkedList<Row>());
    private final Timer timer= new Timer();
    private Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
    private Row latestAverageRow = null;
    private Row previousRow = null;
    private Row currentCommand = null;

    private static HashMap<Integer, String> LABELS_MAP = new HashMap<>();
    static {
        LABELS_MAP.put(0, "Signal Quality");
        LABELS_MAP.put(1, "Attention");
        LABELS_MAP.put(2, "Meditation");
        LABELS_MAP.put(3, "Delta");
        LABELS_MAP.put(4, "Theta");
        LABELS_MAP.put(5, "Low Alpha");
        LABELS_MAP.put(6, "High Alpha");
        LABELS_MAP.put(7, "Low Beta");
        LABELS_MAP.put(8, "High Beta");
        LABELS_MAP.put(9, "Low Gamma");
        LABELS_MAP.put(10, "Mid Gamma");
    }



    private class QueueWorker extends TimerTask {
        @Override
        public void run(){
            synchronized(queueSync) {
                int i = 0;
                Row averageRow = new Row();
                averageRow.data = new Double[11];

                Row differenceRow = new Row();
                differenceRow.data = new Double[11];


                int processingSize = dataQueue.size();
                if (processingSize > 20) {
                    processingSize = 20;
                }

                while (i < processingSize) {
                    Row row = dataQueue.remove(0);
                    if (commandQueue.size() > 0) {
                        currentCommand = commandQueue.remove(0);
                    }
                    if (previousRow != null) {
                        averageRow.timeStamp = (row.timeStamp + averageRow.timeStamp);
                        for (int j = 0; j < row.data.length; j++) {
                            averageRow.data[j] =  (previousRow.data[j]  + row.data[j]) / 2;

                        }
                        boolean isSiginificantChange = false;
                        // check if noise 0 and one of the values changed more than 10% (absolute value)
                        if (row.data.length > 1 && row.data[0] == 0) {
                            for (int j = 0; j < row.data.length; j++) {
                                if (averageRow.data[j] != 0) {
                                    differenceRow.data[j] =  (row.data[j] - averageRow.data[j]) / averageRow.data[j] * 100;
                                    if (differenceRow.data[j] > 10 || differenceRow.data[j] <- 10 ) {
                                        isSiginificantChange = true;
                                        //       System.out.println("Significant change for position " + LABELS_MAP.get(j) + ": " + differenceRow.data[j] + "%");
                                    }
                                }
                            }
                        }
                        // if above yes, then log
                        if (isSiginificantChange) {
                            Date date = new Date(differenceRow.timeStamp);
                            String differenceDataString = "";
                            if (currentCommand != null) {
                                for (int j = 0; j < currentCommand.data.length; j++) {
                                    differenceDataString += currentCommand.data[j].toString() + ", ";
                                }

                            } else {
                                differenceDataString = "-1, -1, ";
                            }
                            for (int j = 0; j < differenceRow.data.length; j++) {
                                differenceDataString +=  ((j ==0) ? row.data[j] : differenceRow.data[j].toString()) + ", ";
                            }

                            System.out.println(/*format.format(date) + ", " + differenceRow.timeStamp  + ", " + */differenceDataString);

                        }
                    }


                    previousRow = row;
                    i++;
                }
              /*  if (processingSize > 0) {
                    averageRow.timeStamp = averageRow.timeStamp / processingSize;
                    for (int j = 0; j < averageRow.data.length; j++) {
                        averageRow.data[j] =  averageRow.data[j] / processingSize;

                    }
                    Date date = new Date(averageRow.timeStamp);
                    String avDataString = "";
                    for (int j = 0; j < averageRow.data.length; j++) {
                        avDataString += averageRow.data[j].toString() + ", ";
                    }

                    System.out.println(format.format(date) + ", " + averageRow.timeStamp  + ", " + avDataString);
                    latestAverageRow = averageRow;
                }*/
            }
        }
    }

    /**
     * Process all events for keys queued to the watcher
     */

    long fileLength = 0;
    long fileLengthCommand = 0;
    public void processEvents() throws SAXException, TransformerException, ParserConfigurationException, IOException {
        LinkedList<Row> dataQ= new LinkedList<Row>();
        LinkedList<Row> commandQ= new LinkedList<Row>();
        while (opened) {
            dataQ.clear();
            commandQ.clear();
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized asdf!!");
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
                   /* while(true){
                       readFile(file);
                    }*/

                }

            }
            synchronized(queueSync){
                dataQueue.addAll(dataQ);
                commandQueue.addAll(commandQ);
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
        long actualFileLength = -1;
        switch (file.getName()) {
            case "command.txt":
                actualFileLength = fileLengthCommand;
                fileLengthCommand = file.length();
                break;
            case "data.txt":
                actualFileLength = fileLength;
                fileLength = file.length();
                break;
        }
        if(actualFileLength < file.length()){
            String line = null;
            // 0,67,44,593634,40191,7155,5066,5082,5325,7873,7402
            BufferedReader in = new BufferedReader(new java.io.FileReader(file));
            in.skip(actualFileLength);
            while((line = in.readLine()) != null) {
                String[] split = null;
                if (line.contains(",")) {
                    split = line.split(",");
                    if (split.length == 11 || split.length == 2) {
                        Double[] response = new Double[split.length];
                        for (int i = 0; i < split.length; i++) {
                            response[i] = Double.valueOf(split[i]);
                        }
                        Row row = new Row();
                        row.timeStamp = System.currentTimeMillis();
                        row.data = response;
                        switch (file.getName()) {
                            case "command.txt":
                                commandQueue.add(row);
                                break;
                            case "data.txt":
                                dataQueue.add(row);
                                break;
                        }

                    }

                } else {
                    split = new String[1];
                    split[0] = line;
                }
             //   System.out.println(line);
            }
            in.close();

        }

    }


    public static void main(String[] args) throws IOException, TransformerException, SAXException, ParserConfigurationException, InterruptedException {


        // register directory and process its events
        Path dir = Paths.get(args[0]);
        new WatchDir(dir).processEvents();
        //

    }

    @Override
    public void close() throws IOException {
        opened = false;
    }
}
