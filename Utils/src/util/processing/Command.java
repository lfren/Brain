package util.processing;

import java.util.HashMap;

/**
 * Created by lilia on 5/4/2017.
 */
public class Command {
    static HashMap<Integer, String> commands = new HashMap<>();
    static {
        commands.put(-1, "Default");
        commands.put(0, "Prepare");
        commands.put(1, "Close Eyes");
        commands.put(2, "Focus on Red");
        commands.put(3, "Focus on White");
        commands.put(4, "Focus on Black");
    }
    private Integer currentCommand = -1;

    public Integer getCurrentCommand() {
        return currentCommand;
    }

    public void setCurrentCommand(Integer currentCommand) {
        this.currentCommand = currentCommand;
    }

    public void incrementCurrentCommand() {
        if (currentCommand == 4) {
            currentCommand = 0;
        }
        currentCommand = currentCommand + 1;
    }

    public String getCurrentCommandDescription() {
        return commands.get(currentCommand);
    }
}
